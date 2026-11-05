package com.droidbubble.app.tasks

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector

enum class ToggleActionType {
    TOGGLE,
    TURN_ON,
    TURN_OFF
}

enum class RingerActionType {
    SET_RING,
    SET_VIBRATE,
    TOGGLE
}

enum class MediaActionType {
    PLAY,
    PAUSE,
    TOGGLE_PLAYBACK,
    SKIP
}

sealed class TaskAction {
    data class OpenApp(val packageName: String, val appLabel: String) : TaskAction()
    data class Wifi(val type: ToggleActionType) : TaskAction()
    data class MobileData(val type: ToggleActionType) : TaskAction()
    data class Flashlight(val type: ToggleActionType) : TaskAction()
    data class LowPower(val type: ToggleActionType) : TaskAction()
    data class AirplaneMode(val type: ToggleActionType) : TaskAction()
    data class Bluetooth(val type: ToggleActionType) : TaskAction()
    data class RingerMode(val type: RingerActionType) : TaskAction()
    data class SetBrightness(val level: Int) : TaskAction()
    data class SetVolume(val level: Int) : TaskAction()
    object TakeScreenshot : TaskAction()
    object GoHome : TaskAction()
    data class MediaControls(val type: MediaActionType) : TaskAction()
    data class SendNotification(val title: String, val text: String) : TaskAction()

    fun getDisplayName(): String {
        return when (this) {
            is OpenApp -> "Open $appLabel"
            is Wifi -> when (type) {
                ToggleActionType.TOGGLE -> "Toggle Wi-Fi"
                ToggleActionType.TURN_ON -> "Turn On Wi-Fi"
                ToggleActionType.TURN_OFF -> "Turn Off Wi-Fi"
            }
            is MobileData -> when (type) {
                ToggleActionType.TOGGLE -> "Toggle Mobile Data"
                ToggleActionType.TURN_ON -> "Turn On Mobile Data"
                ToggleActionType.TURN_OFF -> "Turn Off Mobile Data"
            }
            is Flashlight -> when (type) {
                ToggleActionType.TOGGLE -> "Toggle Flashlight"
                ToggleActionType.TURN_ON -> "Turn On Flashlight"
                ToggleActionType.TURN_OFF -> "Turn Off Flashlight"
            }
            is LowPower -> when (type) {
                ToggleActionType.TOGGLE -> "Toggle Low Power Mode"
                ToggleActionType.TURN_ON -> "Turn On Low Power Mode"
                ToggleActionType.TURN_OFF -> "Turn Off Low Power Mode"
            }
            is AirplaneMode -> when (type) {
                ToggleActionType.TOGGLE -> "Toggle Aeroplane Mode"
                ToggleActionType.TURN_ON -> "Turn On Aeroplane Mode"
                ToggleActionType.TURN_OFF -> "Turn Off Aeroplane Mode"
            }
            is Bluetooth -> when (type) {
                ToggleActionType.TOGGLE -> "Toggle Bluetooth"
                ToggleActionType.TURN_ON -> "Turn On Bluetooth"
                ToggleActionType.TURN_OFF -> "Turn Off Bluetooth"
            }
            is RingerMode -> when (type) {
                RingerActionType.SET_RING -> "Set Ring Mode"
                RingerActionType.SET_VIBRATE -> "Set Vibrate Mode"
                RingerActionType.TOGGLE -> "Toggle Ring / Vibrate"
            }
            is SetBrightness -> "Set brightness to $level%"
            is SetVolume -> "Set volume to $level%"
            is TakeScreenshot -> "Take screenshot"
            is GoHome -> "Go to home screen"
            is MediaControls -> when (type) {
                MediaActionType.PLAY -> "Play media"
                MediaActionType.PAUSE -> "Pause media"
                MediaActionType.TOGGLE_PLAYBACK -> "Toggle media playback"
                MediaActionType.SKIP -> "Skip to next track"
            }
            is SendNotification -> "Send notification"
        }
    }

    fun getIcon(): ImageVector {
        return when (this) {
            is OpenApp -> Icons.Default.Apps
            is Wifi -> Icons.Default.Wifi
            is MobileData -> Icons.Default.SignalCellularAlt
            is Flashlight -> Icons.Default.FlashlightOn
            is LowPower -> Icons.Default.BatteryChargingFull
            is AirplaneMode -> Icons.Default.AirplanemodeActive
            is Bluetooth -> Icons.Default.Bluetooth
            is RingerMode -> when (type) {
                RingerActionType.SET_VIBRATE -> Icons.Default.Vibration
                else -> Icons.Default.NotificationsActive
            }
            is SetBrightness -> Icons.Default.BrightnessMedium
            is SetVolume -> Icons.Default.VolumeUp
            is TakeScreenshot -> Icons.Default.Screenshot
            is GoHome -> Icons.Default.Home
            is MediaControls -> when (type) {
                MediaActionType.PLAY -> Icons.Default.PlayArrow
                MediaActionType.PAUSE -> Icons.Default.Pause
                MediaActionType.TOGGLE_PLAYBACK -> Icons.Default.PlayCircleOutline
                MediaActionType.SKIP -> Icons.Default.SkipNext
            }
            is SendNotification -> Icons.Default.NotificationAdd
        }
    }
}

data class TaskShortcut(
    val id: String = java.util.UUID.randomUUID().toString(),
    val name: String,
    val iconName: String = "Bolt",
    val showInBubble: Boolean = true,
    val actions: List<TaskAction> = emptyList()
)
