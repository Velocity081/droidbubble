package com.droidbubble.app.service

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.os.Build
import android.util.Log
import java.io.File
import kotlin.math.abs
import kotlin.math.roundToInt

object BatteryUtils {

    private const val TAG = "BatteryUtils"
    private const val EXTRA_MAX_CHARGING_CURRENT = "max_charging_current"
    private const val EXTRA_MAX_CHARGING_VOLTAGE = "max_charging_voltage"

    private val SYSFS_CURRENT_PATHS = listOf(
        "/sys/class/power_supply/battery/current_now",
        "/sys/class/power_supply/usb/current_now",
        "/sys/class/power_supply/main/current_now",
        "/sys/class/power_supply/bms/current_now",
        "/sys/class/power_supply/ac/current_now"
    )

    private val SYSFS_VOLTAGE_PATHS = listOf(
        "/sys/class/power_supply/battery/voltage_now",
        "/sys/class/power_supply/usb/voltage_now",
        "/sys/class/power_supply/main/voltage_now",
        "/sys/class/power_supply/bms/voltage_now"
    )

    /**
     * Queries the actual charging wattage received by the device from BatteryManager, sysfs, or broadcast extras,
     * and returns it rounded to the nearest whole Watt (e.g. 18.0, 33.0, 90.0), or null if unavailable.
     */
    fun getChargingWattage(context: Context, batteryIntent: Intent? = null): Double? {
        return try {
            val intent = batteryIntent ?: getBatteryIntent(context)
            val bm = context.getSystemService(Context.BATTERY_SERVICE) as? BatteryManager

            // 1. Primary: Actual real-time current (BatteryManager CURRENT_NOW / CURRENT_AVERAGE) * battery/cell voltage
            if (bm != null) {
                var rawCurrent = bm.getIntProperty(BatteryManager.BATTERY_PROPERTY_CURRENT_NOW)
                if (rawCurrent == 0 || rawCurrent == Int.MIN_VALUE || abs(rawCurrent) < 15) {
                    rawCurrent = bm.getIntProperty(BatteryManager.BATTERY_PROPERTY_CURRENT_AVERAGE)
                }

                val rawVoltage = intent?.getIntExtra(BatteryManager.EXTRA_VOLTAGE, -1) ?: -1
                val cellVolts = normalizeVoltageVolts(rawVoltage) ?: readSysfsVoltageVolts() ?: 4.0
                val amps = normalizeCurrentAmps(abs(rawCurrent))

                if (amps != null && amps >= 0.1) {
                    // Dual-cell batteries often report single-cell voltage (~4.2V) while charging at 2S or higher,
                    // or report battery-side current (e.g. 6A-18A at 4.3V). Multiply by actual cell voltage.
                    val watts = amps * cellVolts
                    if (watts in 0.8..240.0) {
                        return watts.roundToInt().coerceAtLeast(1).toDouble()
                    }
                }
            }

            // 2. Secondary: Direct sysfs power_supply current_now & voltage_now reading
            val sysfsAmps = readSysfsCurrentAmps()
            if (sysfsAmps != null && sysfsAmps >= 0.1) {
                val rawVoltage = intent?.getIntExtra(BatteryManager.EXTRA_VOLTAGE, -1) ?: -1
                val volts = normalizeVoltageVolts(rawVoltage) ?: readSysfsVoltageVolts() ?: 4.0
                val watts = sysfsAmps * volts
                if (watts in 0.8..240.0) {
                    return watts.roundToInt().coerceAtLeast(1).toDouble()
                }
            }

            // 3. Fallback: Negotiated max_charging_current * max_charging_voltage from ACTION_BATTERY_CHANGED
            if (intent != null) {
                val rawCurrent = intent.getIntExtra(EXTRA_MAX_CHARGING_CURRENT, -1)
                val rawVoltage = intent.getIntExtra(EXTRA_MAX_CHARGING_VOLTAGE, -1)

                val amps = normalizeCurrentAmps(rawCurrent)
                val volts = normalizeVoltageVolts(rawVoltage)

                if (amps != null && volts != null) {
                    val watts = amps * volts
                    if (watts in 0.8..240.0) {
                        return watts.roundToInt().coerceAtLeast(1).toDouble()
                    }
                }

                if (amps != null) {
                    val rawCellVoltage = intent.getIntExtra(BatteryManager.EXTRA_VOLTAGE, -1)
                    val cellVolts = normalizeVoltageVolts(rawCellVoltage) ?: 5.0
                    val effectiveVolts = if (cellVolts in 3.0..4.6 && amps <= 1.5) 5.0 else cellVolts
                    val watts = amps * effectiveVolts
                    if (watts in 0.8..240.0) {
                        return watts.roundToInt().coerceAtLeast(1).toDouble()
                    }
                }

                // 4. Final fallback based on plugged source so a reasonable rounded wattage is shown
                val plugged = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1)
                val status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1)
                val isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                        status == BatteryManager.BATTERY_STATUS_FULL ||
                        plugged > 0
                if (isCharging) {
                    return when (plugged) {
                        BatteryManager.BATTERY_PLUGGED_AC -> 18.0
                        BatteryManager.BATTERY_PLUGGED_WIRELESS -> 10.0
                        BatteryManager.BATTERY_PLUGGED_USB -> 5.0
                        else -> null
                    }
                }
            }

            null
        } catch (e: Exception) {
            Log.w(TAG, "Failed to get charging wattage", e)
            null
        }
    }

    private fun readSysfsCurrentAmps(): Double? {
        for (path in SYSFS_CURRENT_PATHS) {
            try {
                val file = File(path)
                if (file.exists() && file.canRead()) {
                    val text = file.readText().trim()
                    val value = text.toLongOrNull() ?: continue
                    val absVal = abs(value)
                    if (absVal > 0) {
                        val amps = when {
                            absVal > 15_000L -> absVal / 1_000_000.0
                            absVal in 50L..15_000L -> absVal / 1_000.0
                            else -> null
                        }
                        if (amps != null && amps in 0.1..40.0) {
                            return amps
                        }
                    }
                }
            } catch (_: Exception) {
            }
        }
        return null
    }

    private fun readSysfsVoltageVolts(): Double? {
        for (path in SYSFS_VOLTAGE_PATHS) {
            try {
                val file = File(path)
                if (file.exists() && file.canRead()) {
                    val text = file.readText().trim()
                    val value = text.toIntOrNull() ?: continue
                    val volts = normalizeVoltageVolts(value)
                    if (volts != null && volts in 3.0..25.0) {
                        return volts
                    }
                }
            } catch (_: Exception) {
            }
        }
        return null
    }

    private fun getBatteryIntent(context: Context): Intent? {
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                context.registerReceiver(
                    null,
                    IntentFilter(Intent.ACTION_BATTERY_CHANGED),
                    Context.RECEIVER_NOT_EXPORTED
                )
            } else {
                context.registerReceiver(
                    null,
                    IntentFilter(Intent.ACTION_BATTERY_CHANGED)
                )
            }
        } catch (_: Exception) {
            try {
                context.registerReceiver(
                    null,
                    IntentFilter(Intent.ACTION_BATTERY_CHANGED)
                )
            } catch (_: Exception) {
                null
            }
        }
    }

    private fun normalizeCurrentAmps(rawCurrent: Int): Double? {
        if (rawCurrent <= 0 || rawCurrent == Int.MIN_VALUE) return null
        return when {
            rawCurrent > 15_000 -> rawCurrent / 1_000_000.0 // Microamperes (uA) -> Amps
            rawCurrent in 50..15_000 -> rawCurrent / 1_000.0 // Milliamperes (mA) -> Amps
            else -> null
        }
    }

    private fun normalizeVoltageVolts(rawVoltage: Int): Double? {
        if (rawVoltage <= 0 || rawVoltage == Int.MIN_VALUE) return null
        return when {
            rawVoltage > 100_000 -> rawVoltage / 1_000_000.0 // Microvolts (uV) -> Volts
            rawVoltage in 1_000..100_000 -> rawVoltage / 1_000.0 // Millivolts (mV) -> Volts
            rawVoltage in 3..25 -> rawVoltage.toDouble() // Direct Volts
            else -> null
        }
    }
}
