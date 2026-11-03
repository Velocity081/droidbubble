package com.droidbubble.app.tasks

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

sealed class TaskAction {
    data class OpenApp(val packageName: String, val appLabel: String) : TaskAction()
    data class Wifi(val type: ToggleActionType) : TaskAction()
    data class MobileData(val type: ToggleActionType) : TaskAction()
    data class Flashlight(val type: ToggleActionType) : TaskAction()
    data class LowPower(val type: ToggleActionType) : TaskAction()
    data class AirplaneMode(val type: ToggleActionType) : TaskAction()
    data class Bluetooth(val type: ToggleActionType) : TaskAction()
    data class RingerMode(val type: RingerActionType) : TaskAction()

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
                ToggleActionType.TOGGLE -> "Toggle Airplane Mode"
                ToggleActionType.TURN_ON -> "Turn On Airplane Mode"
                ToggleActionType.TURN_OFF -> "Turn Off Airplane Mode"
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

