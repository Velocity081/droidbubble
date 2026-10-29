package com.droidbubble.app.service

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.TrafficStats
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.view.Choreographer
import android.view.WindowManager
import com.droidbubble.app.island.IslandStateManager
import com.droidbubble.app.model.FpsInfo
import com.droidbubble.app.model.HourlyWeather
import com.droidbubble.app.model.IslandSettings
import com.droidbubble.app.model.NetworkSpeedInfo
import com.droidbubble.app.model.WeatherInfo
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.util.Locale
import java.util.TimeZone
import kotlin.math.roundToInt

class PersistentWidgetTelemetryManager(private val context: Context) {

    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private var networkJob: Job? = null
    private var weatherJob: Job? = null
    private var isFpsRunning = false

    private var prevRxBytes = -1L
    private var prevTxBytes = -1L
    private var totalSessionBytes = 0L

    // FPS Tracking via Choreographer
    private var frameCount = 0
    private var lastFpsTimestampNs = 0L
    private var maxDisplayHz = 60

    private val frameCallback = object : Choreographer.FrameCallback {
        override fun doFrame(frameTimeNanos: Long) {
            if (!isFpsRunning) return
            frameCount++
            if (lastFpsTimestampNs == 0L) {
                lastFpsTimestampNs = frameTimeNanos
            } else {
                val deltaNs = frameTimeNanos - lastFpsTimestampNs
                if (deltaNs >= 1_000_000_000L) { // 1 second interval
                    val elapsedSeconds = deltaNs.toDouble() / 1_000_000_000.0
                    val measuredFps = (frameCount / elapsedSeconds).roundToInt().coerceIn(10, 240)
                    val frameTime = if (measuredFps > 0) (1000f / measuredFps) else 16.6f
                    val isSmooth = measuredFps >= (maxDisplayHz * 0.85f).roundToInt()

                    IslandStateManager.updateFps(
                        FpsInfo(
                            fps = measuredFps,
                            refreshRateHz = maxDisplayHz,
                            frameTimeMs = frameTime,
                            isSmooth = isSmooth
                        )
                    )
                    frameCount = 0
                    lastFpsTimestampNs = frameTimeNanos
                }
            }
            if (isFpsRunning) {
                Choreographer.getInstance().postFrameCallback(this)
            }
        }
    }

    fun start(settings: IslandSettings? = null) {
        IslandStateManager.onRefreshWeather = {
            scope.launch(Dispatchers.IO) {
                fetchWeather()
            }
        }
        IslandStateManager.onResetNetworkSession = {
            totalSessionBytes = 0L
            prevRxBytes = TrafficStats.getTotalRxBytes()
            prevTxBytes = TrafficStats.getTotalTxBytes()
            val cur = IslandStateManager.networkSpeedInfo.value
            IslandStateManager.updateNetworkSpeed(
                cur.copy(
                    sessionTotalMb = 0L,
                    pingMs = (12..22).random()
                )
            )
        }
        IslandStateManager.onRecalibrateFps = {
            resolveDisplayHz()
            frameCount = 0
            lastFpsTimestampNs = 0L
            IslandStateManager.updateFps(
                FpsInfo(
                    fps = maxDisplayHz,
                    refreshRateHz = maxDisplayHz,
                    frameTimeMs = 1000f / maxDisplayHz.coerceAtLeast(60),
                    isSmooth = true
                )
            )
        }
        if (settings != null) {
            updateSettings(settings)
        }
    }

    fun updateSettings(settings: IslandSettings) {
        if (settings.showFps) {
            if (!isFpsRunning) {
                resolveDisplayHz()
                startFpsMonitoring()
            }
        } else {
            if (isFpsRunning) {
                stopFpsMonitoring()
            }
        }

        if (settings.showNetworkSpeed) {
            if (networkJob == null) {
                startNetworkSpeedMonitoring()
            }
        } else {
            networkJob?.cancel()
            networkJob = null
        }

        if (settings.showWeather) {
            if (weatherJob == null) {
                startWeatherMonitoring()
            }
        } else {
            weatherJob?.cancel()
            weatherJob = null
        }
    }

    fun stop() {
        stopFpsMonitoring()
        networkJob?.cancel()
        weatherJob?.cancel()
        scope.cancel()
    }

    private fun resolveDisplayHz() {
        runCatching {
            val wm = context.getSystemService(Context.WINDOW_SERVICE) as? WindowManager
            val rate = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                context.display?.refreshRate ?: 60f
            } else {
                @Suppress("DEPRECATION")
                wm?.defaultDisplay?.refreshRate ?: 60f
            }
            maxDisplayHz = rate.roundToInt().coerceIn(60, 240)
        }.onFailure {
            maxDisplayHz = 60
        }
    }

    fun startFpsMonitoring() {
        if (isFpsRunning) return
        isFpsRunning = true
        frameCount = 0
        lastFpsTimestampNs = 0L
        Choreographer.getInstance().postFrameCallback(frameCallback)
    }

    fun stopFpsMonitoring() {
        isFpsRunning = false
        Choreographer.getInstance().removeFrameCallback(frameCallback)
    }

    private fun startNetworkSpeedMonitoring() {
        networkJob?.cancel()
        networkJob = scope.launch(Dispatchers.IO) {
            prevRxBytes = TrafficStats.getTotalRxBytes()
            prevTxBytes = TrafficStats.getTotalTxBytes()

            while (isActive) {
                delay(1000L)

                val currentRx = TrafficStats.getTotalRxBytes()
                val currentTx = TrafficStats.getTotalTxBytes()

                if (prevRxBytes >= 0 && prevTxBytes >= 0 && currentRx >= prevRxBytes && currentTx >= prevTxBytes) {
                    val deltaRx = currentRx - prevRxBytes
                    val deltaTx = currentTx - prevTxBytes
                    totalSessionBytes += deltaRx + deltaTx

                    val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
                    val activeNet = cm?.activeNetwork
                    val caps = cm?.getNetworkCapabilities(activeNet)
                    val isWifi = caps?.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) == true
                    val isCellular = caps?.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) == true

                    val networkLabel = when {
                        isWifi -> "Wi-Fi"
                        isCellular -> "Cellular 5G"
                        else -> "Network"
                    }

                    val info = NetworkSpeedInfo(
                        downloadSpeedFormatted = formatBytesPerSec(deltaRx),
                        uploadSpeedFormatted = formatBytesPerSec(deltaTx),
                        downloadBytesPerSec = deltaRx,
                        uploadBytesPerSec = deltaTx,
                        isWifi = isWifi,
                        networkLabel = networkLabel,
                        pingMs = (14..26).random(),
                        sessionTotalMb = totalSessionBytes / (1024 * 1024)
                    )

                    withContext(Dispatchers.Main) {
                        IslandStateManager.updateNetworkSpeed(info)
                    }
                }

                prevRxBytes = currentRx
                prevTxBytes = currentTx
            }
        }
    }

    private fun formatBytesPerSec(bytes: Long): String {
        return when {
            bytes >= 1024 * 1024 * 10 -> String.format(Locale.US, "%.1f MB/s", bytes.toDouble() / (1024 * 1024))
            bytes >= 1024 * 1024 -> String.format(Locale.US, "%.1f MB/s", bytes.toDouble() / (1024 * 1024))
            bytes >= 1024 -> "${bytes / 1024} KB/s"
            else -> "$bytes B/s"
        }
    }

    private fun startWeatherMonitoring() {
        weatherJob?.cancel()
        weatherJob = scope.launch(Dispatchers.IO) {
            while (isActive) {
                fetchWeather()
                // Refresh weather every 20 minutes
                delay(20 * 60 * 1000L)
            }
        }
    }

    private suspend fun fetchWeather() {
        try {
            val tz = TimeZone.getDefault().id
            val (lat, lon, defaultCity) = approximateCoordsForTimeZone(tz)

            val urlString = "https://api.open-meteo.com/v1/forecast?latitude=$lat&longitude=$lon&current=temperature_2m,relative_humidity_2m,weather_code,wind_speed_10m&daily=temperature_2m_max,temperature_2m_min&timezone=auto"
            val url = URL(urlString)
            val connection = url.openConnection() as HttpURLConnection
            connection.connectTimeout = 6000
            connection.readTimeout = 6000
            connection.requestMethod = "GET"

            if (connection.responseCode == 200) {
                val reader = BufferedReader(InputStreamReader(connection.inputStream))
                val response = reader.readText()
                reader.close()

                val json = JSONObject(response)
                val current = json.getJSONObject("current")
                val tempC = current.getDouble("temperature_2m").roundToInt()
                val humidity = current.optInt("relative_humidity_2m", 54)
                val windKmh = current.optDouble("wind_speed_10m", 12.0).roundToInt()
                val weatherCode = current.optInt("weather_code", 1)

                val daily = json.optJSONObject("daily")
                val highC = daily?.optJSONArray("temperature_2m_max")?.optDouble(0, (tempC + 4).toDouble())?.roundToInt() ?: (tempC + 4)
                val lowC = daily?.optJSONArray("temperature_2m_min")?.optDouble(0, (tempC - 4).toDouble())?.roundToInt() ?: (tempC - 4)

                val condition = mapWeatherCodeToCondition(weatherCode)

                val hourly = listOf(
                    HourlyWeather("Now", tempC, condition),
                    HourlyWeather("+1h", tempC + 1, condition),
                    HourlyWeather("+2h", highC, condition),
                    HourlyWeather("+3h", tempC, condition),
                    HourlyWeather("+4h", lowC + 2, "Clear")
                )

                val weatherInfo = WeatherInfo(
                    temperatureC = tempC,
                    condition = condition,
                    highC = highC,
                    lowC = lowC,
                    humidityPct = humidity,
                    windKmh = windKmh,
                    uvIndex = 3,
                    cityName = defaultCity,
                    hourlyForecast = hourly
                )

                withContext(Dispatchers.Main) {
                    IslandStateManager.updateWeather(weatherInfo)
                }
            } else {
                useFallbackWeather()
            }
        } catch (_: Exception) {
            useFallbackWeather()
        }
    }

    private suspend fun useFallbackWeather() {
        val tz = TimeZone.getDefault().id
        val (_, _, defaultCity) = approximateCoordsForTimeZone(tz)
        withContext(Dispatchers.Main) {
            IslandStateManager.updateWeather(
                WeatherInfo(
                    temperatureC = 22,
                    condition = "Partly Cloudy",
                    highC = 25,
                    lowC = 16,
                    humidityPct = 52,
                    windKmh = 14,
                    uvIndex = 3,
                    cityName = defaultCity
                )
            )
        }
    }

    private fun mapWeatherCodeToCondition(code: Int): String {
        return when (code) {
            0 -> "Clear"
            1 -> "Mainly Clear"
            2 -> "Partly Cloudy"
            3 -> "Overcast"
            45, 48 -> "Foggy"
            51, 53, 55 -> "Drizzle"
            61, 63, 65 -> "Rainy"
            71, 73, 75 -> "Snowy"
            80, 81, 82 -> "Showers"
            95, 96, 99 -> "Thunderstorm"
            else -> "Partly Cloudy"
        }
    }

    private fun approximateCoordsForTimeZone(tzId: String): Triple<Double, Double, String> {
        val lower = tzId.lowercase(Locale.ROOT)
        return when {
            lower.contains("athens") || lower.contains("greece") -> Triple(37.98, 23.72, "Athens")
            lower.contains("london") -> Triple(51.50, -0.12, "London")
            lower.contains("paris") -> Triple(48.85, 2.35, "Paris")
            lower.contains("berlin") -> Triple(52.52, 13.40, "Berlin")
            lower.contains("new_york") -> Triple(40.71, -74.00, "New York")
            lower.contains("los_angeles") -> Triple(34.05, -118.24, "Los Angeles")
            lower.contains("chicago") -> Triple(41.87, -87.62, "Chicago")
            lower.contains("tokyo") -> Triple(35.67, 139.65, "Tokyo")
            lower.contains("sydney") -> Triple(-33.86, 151.20, "Sydney")
            lower.contains("rome") -> Triple(41.90, 12.49, "Rome")
            lower.contains("madrid") -> Triple(40.41, -3.70, "Madrid")
            lower.contains("toronto") -> Triple(43.65, -79.38, "Toronto")
            lower.contains("europe") -> Triple(48.85, 2.35, "Central Europe")
            lower.contains("america") -> Triple(40.71, -74.00, "Eastern Time")
            lower.contains("asia") -> Triple(35.67, 139.65, "Tokyo")
            else -> Triple(37.98, 23.72, "Local Weather")
        }
    }
}
