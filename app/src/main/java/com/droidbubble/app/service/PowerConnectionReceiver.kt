package com.droidbubble.app.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import com.droidbubble.app.island.IslandStateManager
import kotlin.math.roundToInt

/**
 * Manifest-registered broadcast receiver for device power connection events.
 * Triggered whenever the user plugs the phone into a charger or USB power source,
 * allowing the Dynamic Island charging indicator to pop up even when idle.
 */
class PowerConnectionReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        val action = intent?.action ?: return
        if (action == Intent.ACTION_POWER_CONNECTED || action == "android.os.action.CHARGING") {
            val ctx = context ?: return
            val batteryIntent = ctx.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
            val level = batteryIntent?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
            val scale = batteryIntent?.getIntExtra(BatteryManager.EXTRA_SCALE, -1) ?: -1
            val pct = if (level >= 0 && scale > 0) {
                ((level.toFloat() / scale.toFloat()) * 100).roundToInt().coerceIn(0, 100)
            } else {
                100
            }
            val wattage = BatteryUtils.getChargingWattage(ctx, batteryIntent)
            IslandStateManager.triggerChargingEvent(pct, wattage)
            IslandStateManager.updatePersistentCharging(true, pct, wattage)
        } else if (action == Intent.ACTION_POWER_DISCONNECTED) {
            IslandStateManager.updatePersistentCharging(false, 0, null)
        }
    }
}
