package com.droidbubble.app.island

import com.droidbubble.app.model.ActiveCallState
import com.droidbubble.app.model.IslandPriority
import com.droidbubble.app.model.MediaInfo
import com.droidbubble.app.model.NotificationInfo
import com.droidbubble.app.model.ScreenRecordingState
import com.droidbubble.app.model.FpsInfo
import com.droidbubble.app.model.WeatherInfo
import com.droidbubble.app.model.NetworkSpeedInfo
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

object IslandStateManager {

    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private var popInJob: Job? = null
    private var chargingJob: Job? = null

    data class ChargingInfo(
        val percentage: Int,
        val wattage: Double? = null,
        val timestamp: Long = System.currentTimeMillis()
    )

    private val _activeCharging = MutableStateFlow<ChargingInfo?>(null)
    val activeCharging: StateFlow<ChargingInfo?> = _activeCharging.asStateFlow()

    private val _persistentCharging = MutableStateFlow<ChargingInfo?>(null)
    val persistentCharging: StateFlow<ChargingInfo?> = _persistentCharging.asStateFlow()

    val isPersistentChargingActive: Boolean
        get() = _persistentCharging.value != null

    private val _isUnlocked = MutableStateFlow(false)
    val isUnlocked: StateFlow<Boolean> = _isUnlocked.asStateFlow()
    private var unlockJob: Job? = null

    private val _isDeviceLocked = MutableStateFlow(false)
    val isDeviceLocked: StateFlow<Boolean> = _isDeviceLocked.asStateFlow()

    fun setDeviceLocked(locked: Boolean) {
        if (_isDeviceLocked.value != locked) {
            _isDeviceLocked.value = locked
            if (locked && _isExpanded.value) {
                collapse()
            }
        }
    }

    private val _isBluetoothConnected = MutableStateFlow(false)
    val isBluetoothConnected: StateFlow<Boolean> = _isBluetoothConnected.asStateFlow()
    private var bluetoothJob: Job? = null

    enum class RingerMode {
        SILENT,
        VIBRATE,
        RING
    }

    private val _activeRingerMode = MutableStateFlow<RingerMode?>(null)
    val activeRingerMode: StateFlow<RingerMode?> = _activeRingerMode.asStateFlow()
    private var ringerJob: Job? = null

    private val _activeAirplaneMode = MutableStateFlow<Boolean?>(null)
    val activeAirplaneMode: StateFlow<Boolean?> = _activeAirplaneMode.asStateFlow()
    private var airplaneJob: Job? = null

    private val _activeMedia = MutableStateFlow<MediaInfo?>(null)
    val activeMedia: StateFlow<MediaInfo?> = _activeMedia.asStateFlow()

    private val _activeNotification = MutableStateFlow<NotificationInfo?>(null)
    val activeNotification: StateFlow<NotificationInfo?> = _activeNotification.asStateFlow()

    private val _activeProgress = MutableStateFlow<NotificationInfo?>(null)
    val activeProgress: StateFlow<NotificationInfo?> = _activeProgress.asStateFlow()

    private val _screenRecordingState = MutableStateFlow(ScreenRecordingState())
    val screenRecordingState: StateFlow<ScreenRecordingState> = _screenRecordingState.asStateFlow()

    private val _activeCallState = MutableStateFlow(ActiveCallState())
    val activeCallState: StateFlow<ActiveCallState> = _activeCallState.asStateFlow()

    private val _isExpanded = MutableStateFlow(false)
    val isExpanded: StateFlow<Boolean> = _isExpanded.asStateFlow()

    private val _fpsInfo = MutableStateFlow(FpsInfo())
    val fpsInfo: StateFlow<FpsInfo> = _fpsInfo.asStateFlow()

    fun updateFps(info: FpsInfo) {
        if (_fpsInfo.value != info) {
            _fpsInfo.value = info
        }
    }

    private val _weatherInfo = MutableStateFlow(WeatherInfo())
    val weatherInfo: StateFlow<WeatherInfo> = _weatherInfo.asStateFlow()

    fun updateWeather(info: WeatherInfo) {
        if (_weatherInfo.value != info) {
            _weatherInfo.value = info
        }
    }

    private val _networkSpeedInfo = MutableStateFlow(NetworkSpeedInfo())
    val networkSpeedInfo: StateFlow<NetworkSpeedInfo> = _networkSpeedInfo.asStateFlow()

    fun updateNetworkSpeed(info: NetworkSpeedInfo) {
        if (_networkSpeedInfo.value != info) {
            _networkSpeedInfo.value = info
        }
    }

    private val _expandedPriority = MutableStateFlow<IslandPriority?>(null)
    val expandedPriority: StateFlow<IslandPriority?> = _expandedPriority.asStateFlow()

    private val _isPopInActive = MutableStateFlow(false)
    val isPopInActive: StateFlow<Boolean> = _isPopInActive.asStateFlow()

    private val _isKeyboardFocused = MutableStateFlow(false)
    val isKeyboardFocused: StateFlow<Boolean> = _isKeyboardFocused.asStateFlow()

    fun setKeyboardFocused(focused: Boolean) {
        _isKeyboardFocused.value = focused
    }

    private var lastMediaPlayingTime = 0L

    // Callbacks for media transport controls
    var onPlayPause: (() -> Unit)? = null
    var onPauseMedia: (() -> Unit)? = null
    var onSkipNext: (() -> Unit)? = null
    var onSkipPrevious: (() -> Unit)? = null
    var onSeekTo: ((Long) -> Unit)? = null
    var onToggleLike: (() -> Unit)? = null

    var dismissedMediaPackage: String? = null
        private set

    // Callbacks for screen recording controls
    var onStopRecording: (() -> Unit)? = null
    var onTogglePauseRecording: (() -> Unit)? = null

    // Callbacks for active phone call controls
    var onEndCall: (() -> Unit)? = null
    var onToggleCallMuteOrSpeaker: (() -> Unit)? = null

    // Callbacks for persistent widget actions
    var onRefreshWeather: (() -> Unit)? = null
    var onResetNetworkSession: (() -> Unit)? = null
    var onRecalibrateFps: (() -> Unit)? = null

    // Callback to dismiss notification from listener
    var onDismissNotification: ((String) -> Unit)? = null

    // Callback to send direct reply to notification
    var onSendReply: ((key: String, replyText: String) -> Unit)? = null

    private val _isFlashlightOn = MutableStateFlow(false)
    val isFlashlightOn: StateFlow<Boolean> = _isFlashlightOn.asStateFlow()

    private val _flashlightLevel = MutableStateFlow(1)
    val flashlightLevel: StateFlow<Int> = _flashlightLevel.asStateFlow()

    private val _maxFlashlightLevel = MutableStateFlow(1)
    val maxFlashlightLevel: StateFlow<Int> = _maxFlashlightLevel.asStateFlow()

    // Callbacks for flashlight controls
    var onSetFlashlightLevel: ((Int) -> Unit)? = null
    var onTurnOffFlashlight: (() -> Unit)? = null

    val isRecordingActive: Boolean
        get() = _screenRecordingState.value.isRecording

    val isCallActive: Boolean
        get() = _activeCallState.value.isActive

    val isNotificationActive: Boolean
        get() = _activeNotification.value != null

    var dismissMediaOnPause: Boolean = false
    var autoDismissNotifications: Boolean = false
    var autoDismissNotificationSeconds: Int = 3
    private var autoDismissNotificationJob: Job? = null

    val isMediaActive: Boolean
        get() {
            val media = _activeMedia.value ?: return false
            if (dismissMediaOnPause && !media.isPlaying) return false
            // Keep active whenever a media session exists with a title so the island remains visible & expandable when paused!
            return media.title.isNotBlank()
        }

    val isProgressActive: Boolean
        get() = _activeProgress.value != null

    val isChargingActive: Boolean
        get() = _activeCharging.value != null

    val isUnlockActive: Boolean
        get() = _isUnlocked.value

    val isBluetoothActive: Boolean
        get() = _isBluetoothConnected.value

    val isFlashlightActive: Boolean
        get() = _isFlashlightOn.value

    val isAirplaneModeActive: Boolean
        get() = _activeAirplaneMode.value != null

    val isRingerModeActive: Boolean
        get() = _activeRingerMode.value != null

    val isOccupied: Boolean
        get() = isCallActive || isRecordingActive || isNotificationActive || isMediaActive || isProgressActive || isChargingActive || isUnlockActive || isBluetoothActive || isFlashlightActive || isRingerModeActive || isAirplaneModeActive

    // Live priority determination: Call is top priority, then Media, Flashlight, Progress, Screen Recording, and Notification alerts last
    val currentPriority: IslandPriority
        get() {
            return when {
                isCallActive -> IslandPriority.CALL
                isMediaActive -> IslandPriority.MEDIA
                isFlashlightActive -> IslandPriority.FLASHLIGHT
                isProgressActive -> IslandPriority.PROGRESS
                _screenRecordingState.value.isRecording -> IslandPriority.SCREEN_RECORDING
                _activeNotification.value != null -> IslandPriority.POP_NOTIFICATION
                else -> IslandPriority.IDLE
            }
        }

    val activeExpandedPriority: IslandPriority
        get() = _expandedPriority.value ?: currentPriority

    fun updateFlashlightState(enabled: Boolean, level: Int = _flashlightLevel.value, maxLevel: Int = 4) {
        _isFlashlightOn.value = enabled
        _flashlightLevel.value = level.coerceIn(1, 4)
        _maxFlashlightLevel.value = 4
        if (!enabled && _isExpanded.value && (_expandedPriority.value == IslandPriority.FLASHLIGHT || !isOccupied)) {
            collapse()
        }
    }

    fun updateFlashlightLevel(level: Int) {
        _flashlightLevel.value = level.coerceIn(1, 4)
    }

    fun setFlashlightBrightness(level: Int) {
        val clamped = level.coerceIn(1, 4)
        _flashlightLevel.value = clamped
        onSetFlashlightLevel?.invoke(clamped)
    }

    fun turnOffFlashlight() {
        _isFlashlightOn.value = false
        onTurnOffFlashlight?.invoke()
        if (_isExpanded.value && (_expandedPriority.value == IslandPriority.FLASHLIGHT || !isOccupied)) {
            collapse()
        }
    }

    fun triggerUnlockEvent() {
        android.util.Log.d("IslandStateManager", "triggerUnlockEvent() invoked: setting isUnlocked = true for 1500ms")
        unlockJob?.cancel()
        _isUnlocked.value = true
        unlockJob = scope.launch {
            delay(1500) // 1.5 seconds as requested
            _isUnlocked.value = false
            android.util.Log.d("IslandStateManager", "triggerUnlockEvent() finished: isUnlocked reset to false")
        }
    }

    fun triggerBluetoothEvent() {
        android.util.Log.d("IslandStateManager", "triggerBluetoothEvent() invoked: setting isBluetoothConnected = true for 1500ms")
        bluetoothJob?.cancel()
        _isBluetoothConnected.value = true
        bluetoothJob = scope.launch {
            delay(1500) // 1.5 seconds exact same as unlock
            _isBluetoothConnected.value = false
            android.util.Log.d("IslandStateManager", "triggerBluetoothEvent() finished: isBluetoothConnected reset to false")
        }
    }

    fun triggerRingerModeEvent(mode: RingerMode) {
        android.util.Log.d("IslandStateManager", "triggerRingerModeEvent($mode) invoked for 1500ms")
        ringerJob?.cancel()
        _activeRingerMode.value = mode
        ringerJob = scope.launch {
            delay(1500) // Should last only 1.5 seconds on screen as requested!
            _activeRingerMode.value = null
            android.util.Log.d("IslandStateManager", "triggerRingerModeEvent finished: reset to null")
        }
    }

    fun triggerAirplaneModeEvent(isOn: Boolean) {
        android.util.Log.d("IslandStateManager", "triggerAirplaneModeEvent($isOn) invoked for 1500ms")
        airplaneJob?.cancel()
        _activeAirplaneMode.value = isOn
        airplaneJob = scope.launch {
            delay(1500) // Stay visible for 1.5 seconds exact matching Ring mode duration
            _activeAirplaneMode.value = null
            android.util.Log.d("IslandStateManager", "triggerAirplaneModeEvent finished: reset to null")
        }
    }

    fun triggerChargingEvent(percentage: Int, wattage: Double? = null) {
        chargingJob?.cancel()
        _activeCharging.value = ChargingInfo(percentage.coerceIn(0, 100), wattage)
        chargingJob = scope.launch {
            delay(3000)
            _activeCharging.value = null
        }
    }

    fun updateChargingWattage(wattage: Double?) {
        val current = _activeCharging.value
        if (current != null) {
            _activeCharging.value = current.copy(wattage = wattage)
        }
        val currentPersistent = _persistentCharging.value
        if (currentPersistent != null) {
            _persistentCharging.value = currentPersistent.copy(wattage = wattage)
        }
    }

    fun updatePersistentCharging(isCharging: Boolean, percentage: Int, wattage: Double? = null) {
        if (isCharging) {
            val currentWattage = wattage ?: _persistentCharging.value?.wattage
            _persistentCharging.value = ChargingInfo(percentage.coerceIn(0, 100), currentWattage)
        } else {
            _persistentCharging.value = null
        }
    }

    fun updateMedia(media: MediaInfo?) {
        if (dismissMediaOnPause && media != null && !media.isPlaying) {
            _activeMedia.value = null
            if (_isExpanded.value && (_expandedPriority.value == IslandPriority.MEDIA || !isOccupied)) {
                collapse()
            }
            return
        }
        if (media != null && media.packageName == dismissedMediaPackage) {
            if (media.isPlaying) {
                // Playback resumed, un-dismiss!
                dismissedMediaPackage = null
            } else {
                // Still dismissed by user swipe
                _activeMedia.value = null
                if (_isExpanded.value && (_expandedPriority.value == IslandPriority.MEDIA || !isOccupied)) {
                    collapse()
                }
                return
            }
        }
        _activeMedia.value = media
        if (media == null && _isExpanded.value && (_expandedPriority.value == IslandPriority.MEDIA || !isOccupied)) {
            collapse()
        }
        if (media?.isPlaying == true) {
            lastMediaPlayingTime = System.currentTimeMillis()
        }
    }

    fun dismissMedia() {
        val current = _activeMedia.value
        dismissedMediaPackage = current?.packageName
        onPauseMedia?.invoke()
        _activeMedia.value = null
        if (_isExpanded.value) {
            collapse()
        }
    }

    fun unDismissMedia(packageName: String? = null) {
        if (packageName == null || dismissedMediaPackage == packageName) {
            dismissedMediaPackage = null
        }
    }

    fun updateNotification(notification: NotificationInfo?) {
        if (notification != null && notification.hasProgress) {
            updateProgress(notification)
            return
        }
        autoDismissNotificationJob?.cancel()
        _activeNotification.value = notification
        if (notification == null) {
            _isPopInActive.value = false
            if (_isExpanded.value && (_expandedPriority.value == IslandPriority.POP_NOTIFICATION || !isOccupied)) {
                if (!isOccupied) {
                    collapse()
                } else {
                    _expandedPriority.value = null
                }
            }
        } else {
            triggerPopIn()
            if (autoDismissNotifications) {
                val notifKey = notification.key
                autoDismissNotificationJob = scope.launch {
                    delay(autoDismissNotificationSeconds * 1000L)
                    if (_activeNotification.value?.key == notifKey && (!_isExpanded.value || _expandedPriority.value != IslandPriority.POP_NOTIFICATION)) {
                        dismissCurrentNotification()
                    }
                }
            }
        }
    }

    fun updateProgress(progress: NotificationInfo?) {
        _activeProgress.value = progress
        if (progress == null) {
            if (_isExpanded.value && (_expandedPriority.value == IslandPriority.PROGRESS || !isOccupied)) {
                if (!isOccupied) {
                    collapse()
                } else {
                    _expandedPriority.value = null
                }
            }
        }
    }

    fun removeNotification(key: String) {
        var removed = false
        if (_activeNotification.value?.key == key) {
            autoDismissNotificationJob?.cancel()
            _activeNotification.value = null
            _isPopInActive.value = false
            removed = true
        }
        if (_activeProgress.value?.key == key) {
            _activeProgress.value = null
            removed = true
        }
        if (removed && _isExpanded.value && !isOccupied) {
            collapse()
        }
    }

    fun triggerPopIn() {
        _isPopInActive.value = true
        popInJob?.cancel()
        popInJob = scope.launch {
            delay(4500) // Bubble pop-in for 4.5 seconds then auto-shrink to compact
            _isPopInActive.value = false
        }
    }

    fun dismissCurrentNotification(): Boolean {
        autoDismissNotificationJob?.cancel()
        val notif = _activeNotification.value ?: return false
        // Dismiss card only, without cancelling actual system notification
        _activeNotification.value = null
        _isPopInActive.value = false
        if (_isExpanded.value && (_expandedPriority.value == IslandPriority.POP_NOTIFICATION || !isOccupied)) {
            if (!isOccupied) {
                collapse()
            } else {
                _expandedPriority.value = null
            }
        }
        return true
    }

    fun dismissProgress(): Boolean {
        val prog = _activeProgress.value ?: return false
        // Dismiss card only, without cancelling actual system notification
        _activeProgress.value = null
        if (_isExpanded.value && (_expandedPriority.value == IslandPriority.PROGRESS || !isOccupied)) {
            if (!isOccupied) {
                collapse()
            } else {
                _expandedPriority.value = null
            }
        }
        return true
    }

    fun updateRecordingState(state: ScreenRecordingState) {
        val wasRecording = _screenRecordingState.value.isRecording
        _screenRecordingState.value = state
        if (wasRecording && !state.isRecording && _isExpanded.value && (_expandedPriority.value == IslandPriority.SCREEN_RECORDING || !isOccupied)) {
            collapse()
        }
    }

    fun updateCallState(state: ActiveCallState) {
        val wasActive = _activeCallState.value.isActive
        _activeCallState.value = state
        if (wasActive && !state.isActive && _isExpanded.value && (_expandedPriority.value == IslandPriority.CALL || !isOccupied)) {
            collapse()
        }
    }

    fun expand(priority: IslandPriority? = null) {
        val resolved = priority ?: currentPriority
        if (resolved == IslandPriority.POP_NOTIFICATION) {
            autoDismissNotificationJob?.cancel()
        }
        _expandedPriority.value = priority
        _isExpanded.value = true
    }

    fun collapse() {
        _isExpanded.value = false
        _expandedPriority.value = null
        _isKeyboardFocused.value = false
    }

    fun toggleExpand(priority: IslandPriority? = null) {
        if (_isExpanded.value) {
            collapse()
        } else {
            expand(priority)
        }
    }
}
