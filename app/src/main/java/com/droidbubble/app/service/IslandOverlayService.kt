package com.droidbubble.app.service

import android.app.KeyguardManager
import android.media.AudioManager
import com.droidbubble.app.island.IslandStateManager.RingerMode
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.util.Log
import android.app.PendingIntent
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.res.Configuration
import android.content.pm.ServiceInfo
import android.graphics.PixelFormat
import android.os.BatteryManager
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import kotlin.math.roundToInt
import android.provider.Settings
import com.droidbubble.app.model.ActiveCallState
import com.droidbubble.app.model.MediaInfo
import com.droidbubble.app.model.NotificationInfo
import com.droidbubble.app.model.ScreenRecordingState
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialExpressiveTheme
import androidx.compose.material3.MotionScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.ComposeView
import androidx.core.app.NotificationCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.lifecycle.setViewTreeViewModelStoreOwner
import androidx.savedstate.SavedStateRegistry
import androidx.savedstate.SavedStateRegistryController
import androidx.savedstate.SavedStateRegistryOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import com.droidbubble.app.MainActivity
import com.droidbubble.app.R
import com.droidbubble.app.data.SettingsRepository
import com.droidbubble.app.island.IslandOverlayRoot
import com.droidbubble.app.island.IslandStateManager
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.os.Handler
import android.os.Looper
import com.droidbubble.app.model.IslandSettings
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.isActive
import kotlinx.coroutines.runBlocking

class IslandOverlayService : Service() {

    companion object {
        const val CHANNEL_ID = "droidbubble_silent_service_v2"
        const val NOTIFICATION_ID = 1001
        const val ACTION_START = "com.droidbubble.app.START_OVERLAY"
        const val ACTION_STOP = "com.droidbubble.app.STOP_OVERLAY"
        const val CHANNEL_UPDATES_ID = "droidbubble_app_updates"
        const val UPDATE_NOTIFICATION_ID = 2002

        private var serviceInstance: IslandOverlayService? = null

        fun onAccessibilityServiceChanged(accessibilityService: IslandAccessibilityService?) {
            serviceInstance?.handleAccessibilityServiceChanged(accessibilityService)
        }

        fun start(context: Context) {
            val intent = Intent(context, IslandOverlayService::class.java).apply {
                action = ACTION_START
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun stop(context: Context) {
            val intent = Intent(context, IslandOverlayService::class.java).apply {
                action = ACTION_STOP
            }
            context.startService(intent)
        }
    }

    private val serviceScope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private lateinit var settingsRepository: SettingsRepository
    private var windowManager: WindowManager? = null
    private var composeView: ComposeView? = null
    private var layoutParams: WindowManager.LayoutParams? = null

    private val lifecycleOwner = CustomLifecycleOwner()
    private var currentSettings = IslandSettings()

    private var cameraManager: CameraManager? = null
    private var torchCameraId: String? = null
    private var maxTorchStrengthLevel: Int = 1
    private var torchCallback: CameraManager.TorchCallback? = null
    private var persistentTelemetryManager: PersistentWidgetTelemetryManager? = null

    override fun onBind(intent: Intent?): IBinder? = null

    private val debugReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            when (intent?.action) {
                "com.droidbubble.app.ACTION_SIMULATE_MEDIA" -> {
                    val title = intent.getStringExtra("title") ?: "Starboy"
                    val artist = intent.getStringExtra("artist") ?: "The Weeknd"
                    IslandStateManager.updateMedia(
                        MediaInfo(
                            packageName = "com.spotify.music",
                            title = title,
                            artist = artist,
                            isPlaying = true,
                            durationMs = 230000L,
                            positionMs = 65000L
                        )
                    )
                }
                "com.droidbubble.app.ACTION_SIMULATE_NOTIFICATION" -> {
                    val title = intent.getStringExtra("title") ?: "Sarah Jenkins"
                    val appName = intent.getStringExtra("appName") ?: "Messages"
                    val text = intent.getStringExtra("text") ?: "Dinner is ready! Can you pick up some dessert on your way home?"
                    val action1 = intent.getStringExtra("action1") ?: "Reply"
                    val action2 = intent.getStringExtra("action2")
                    val actionsList = mutableListOf<com.droidbubble.app.model.NotificationAction>()
                    if (action1.isNotBlank() && action1 != "none") {
                        actionsList.add(com.droidbubble.app.model.NotificationAction(action1, null))
                    }
                    if (!action2.isNullOrBlank()) {
                        actionsList.add(com.droidbubble.app.model.NotificationAction(action2, null))
                    }
                    IslandStateManager.updateNotification(
                        NotificationInfo(
                            key = "debug_notif_1",
                            packageName = "com.google.android.apps.messaging",
                            appName = appName,
                            title = title,
                            text = text,
                            actions = actionsList
                        )
                    )
                }
                "com.droidbubble.app.ACTION_SIMULATE_PROGRESS" -> {
                    val progress = intent.getIntExtra("progress", 68)
                    val maxProgress = intent.getIntExtra("max", 100)
                    val appName = intent.getStringExtra("appName") ?: "Google Play Store"
                    val title = intent.getStringExtra("title") ?: "Updating Spotify"
                    val text = intent.getStringExtra("text") ?: "45 MB / 66 MB • 3.2 MB/s"
                    IslandStateManager.updateNotification(
                        NotificationInfo(
                            key = "debug_progress_1",
                            packageName = "com.android.vending",
                            appName = appName,
                            title = title,
                            text = text,
                            progress = progress,
                            maxProgress = maxProgress,
                            isOngoing = true
                        )
                    )
                }
                "com.droidbubble.app.ACTION_SIMULATE_RECORDING" -> {
                    val isRec = intent.getBooleanExtra("isRecording", true)
                    IslandStateManager.updateRecordingState(
                        ScreenRecordingState(isRecording = isRec, durationSeconds = 42)
                    )
                }
                "com.droidbubble.app.ACTION_SIMULATE_CALL" -> {
                    val isAct = intent.getBooleanExtra("isActive", true)
                    val caller = intent.getStringExtra("callerName") ?: "Alex Rivera"
                    IslandStateManager.updateCallState(
                        ActiveCallState(isActive = isAct, callerName = caller, durationSeconds = 84)
                    )
                }
                "com.droidbubble.app.ACTION_EXPAND" -> {
                    IslandStateManager.expand()
                }
                "com.droidbubble.app.ACTION_COLLAPSE" -> {
                    IslandStateManager.collapse()
                }
                "com.droidbubble.app.ACTION_RESET" -> {
                    IslandStateManager.updateMedia(null)
                    IslandStateManager.updateNotification(null)
                    IslandStateManager.updateRecordingState(ScreenRecordingState(isRecording = false))
                    IslandStateManager.updateCallState(ActiveCallState(isActive = false))
                    IslandStateManager.updateFlashlightState(false)
                    IslandStateManager.collapse()
                }
                "com.droidbubble.app.ACTION_SIMULATE_UNLOCK" -> {
                    IslandStateManager.triggerUnlockEvent()
                }
                "com.droidbubble.app.ACTION_SIMULATE_BLUETOOTH" -> {
                    IslandStateManager.triggerBluetoothEvent()
                }
                "com.droidbubble.app.ACTION_SIMULATE_FLASHLIGHT" -> {
                    val enabled = intent.getBooleanExtra("enabled", true)
                    val level = intent.getIntExtra("level", 3)
                    IslandStateManager.updateFlashlightState(enabled, level.coerceIn(1, 4), 4)
                }
                "com.droidbubble.app.ACTION_SIMULATE_CHARGING" -> {
                    val pct = intent.getIntExtra("percentage", 85)
                    val wattage = if (intent.hasExtra("wattage")) {
                        intent.getDoubleExtra("wattage", 0.0).takeIf { it > 0 }
                    } else {
                        BatteryUtils.getChargingWattage(this@IslandOverlayService)
                    }
                    IslandStateManager.triggerChargingEvent(pct, wattage)
                }
                "com.droidbubble.app.ACTION_SIMULATE_RINGER" -> {
                    val modeStr = intent.getStringExtra("mode")?.lowercase()
                    val mode = when (modeStr) {
                        "silent" -> RingerMode.SILENT
                        "vibrate" -> RingerMode.VIBRATE
                        else -> RingerMode.RING
                    }
                    IslandStateManager.triggerRingerModeEvent(mode)
                }
            }
        }
    }

    private var lastRingerMode: Int? = null

    private val powerReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val action = intent?.action ?: return
            Log.d("IslandOverlayService", "System broadcast received: $action")
            if (action == AudioManager.RINGER_MODE_CHANGED_ACTION || action == "android.media.INTERNAL_RINGER_MODE_CHANGED_ACTION") {
                if (!currentSettings.showRingerMode) return
                val mode = intent.getIntExtra(AudioManager.EXTRA_RINGER_MODE, -1)
                val am = context?.getSystemService(Context.AUDIO_SERVICE) as? AudioManager
                val currentMode = if (mode != -1) mode else (am?.ringerMode ?: return)
                if (lastRingerMode == currentMode) {
                    return
                }
                lastRingerMode = currentMode
                when (currentMode) {
                    AudioManager.RINGER_MODE_SILENT -> IslandStateManager.triggerRingerModeEvent(RingerMode.SILENT)
                    AudioManager.RINGER_MODE_VIBRATE -> IslandStateManager.triggerRingerModeEvent(RingerMode.VIBRATE)
                    AudioManager.RINGER_MODE_NORMAL -> IslandStateManager.triggerRingerModeEvent(RingerMode.RING)
                }
                return
            }
            if (action == Intent.ACTION_USER_PRESENT) {
                IslandStateManager.setDeviceLocked(false)
                IslandStateManager.triggerUnlockEvent()
                return
            }
            if (action == Intent.ACTION_SCREEN_OFF) {
                val km = context?.getSystemService(Context.KEYGUARD_SERVICE) as? KeyguardManager
                IslandStateManager.setDeviceLocked(km?.isKeyguardLocked ?: true)
                return
            }
            if (action == Intent.ACTION_SCREEN_ON) {
                val km = context?.getSystemService(Context.KEYGUARD_SERVICE) as? KeyguardManager
                val locked = km?.isKeyguardLocked == true
                IslandStateManager.setDeviceLocked(locked)
                if (km != null && !locked) {
                    IslandStateManager.triggerUnlockEvent()
                }
                return
            }
            if (action == android.bluetooth.BluetoothDevice.ACTION_ACL_CONNECTED) {
                if (!currentSettings.showBluetooth) return
                IslandStateManager.triggerBluetoothEvent()
                return
            }
            if (action == "android.bluetooth.a2dp.profile.action.CONNECTION_STATE_CHANGED" ||
                action == "android.bluetooth.headset.profile.action.CONNECTION_STATE_CHANGED" ||
                action == "android.bluetooth.adapter.action.CONNECTION_STATE_CHANGED"
            ) {
                if (!currentSettings.showBluetooth) return
                val state = intent.getIntExtra(android.bluetooth.BluetoothProfile.EXTRA_STATE, -1)
                if (state == android.bluetooth.BluetoothProfile.STATE_CONNECTED) {
                    IslandStateManager.triggerBluetoothEvent()
                    return
                }
            }
            if (action == Intent.ACTION_POWER_CONNECTED || action == "android.os.action.CHARGING") {
                if (!currentSettings.showCharging) return
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

                // Poll charging wattage at 400ms, 1000ms, 1700ms, and 2400ms so fast-charging negotiation updates live
                serviceScope.launch {
                    val sampleDelays = longArrayOf(400L, 600L, 700L, 700L)
                    for (waitMs in sampleDelays) {
                        delay(waitMs)
                        if (!IslandStateManager.isChargingActive) break
                        val updated = BatteryUtils.getChargingWattage(this@IslandOverlayService)
                        if (updated != null && updated > 0.0) {
                            IslandStateManager.updateChargingWattage(updated)
                        }
                    }
                }
            }
        }
    }

    private fun initCameraTorch() {
        try {
            val cm = getSystemService(Context.CAMERA_SERVICE) as? CameraManager ?: return
            cameraManager = cm
            for (id in cm.cameraIdList) {
                val chars = cm.getCameraCharacteristics(id)
                val hasFlash = chars.get(CameraCharacteristics.FLASH_INFO_AVAILABLE) == true
                val facing = chars.get(CameraCharacteristics.LENS_FACING)
                if (hasFlash && facing == CameraCharacteristics.LENS_FACING_BACK) {
                    torchCameraId = id
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        maxTorchStrengthLevel = chars.get(CameraCharacteristics.FLASH_INFO_STRENGTH_MAXIMUM_LEVEL) ?: 1
                    }
                    break
                }
            }
            if (torchCameraId == null) {
                for (id in cm.cameraIdList) {
                    val chars = cm.getCameraCharacteristics(id)
                    if (chars.get(CameraCharacteristics.FLASH_INFO_AVAILABLE) == true) {
                        torchCameraId = id
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            maxTorchStrengthLevel = chars.get(CameraCharacteristics.FLASH_INFO_STRENGTH_MAXIMUM_LEVEL) ?: 1
                        }
                        break
                    }
                }
            }

            IslandStateManager.onSetFlashlightLevel = { level1to4 ->
                torchCameraId?.let { id ->
                    runCatching {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && maxTorchStrengthLevel > 1) {
                            val scaled = if (maxTorchStrengthLevel == 4) {
                                level1to4
                            } else {
                                ((level1to4 - 1f) / 3f * (maxTorchStrengthLevel - 1) + 1).roundToInt().coerceIn(1, maxTorchStrengthLevel)
                            }
                            cm.turnOnTorchWithStrengthLevel(id, scaled)
                        } else {
                            cm.setTorchMode(id, true)
                        }
                    }
                }
            }

            IslandStateManager.onTurnOffFlashlight = {
                torchCameraId?.let { id ->
                    runCatching {
                        cm.setTorchMode(id, false)
                    }
                }
            }

            val cb = object : CameraManager.TorchCallback() {
                override fun onTorchModeChanged(cameraId: String, enabled: Boolean) {
                    super.onTorchModeChanged(cameraId, enabled)
                    if (torchCameraId == null && enabled) {
                        torchCameraId = cameraId
                    }
                    if (cameraId == torchCameraId || torchCameraId == null) {
                        IslandStateManager.updateFlashlightState(
                            enabled = enabled,
                            maxLevel = 4
                        )
                    }
                }

                override fun onTorchStrengthLevelChanged(cameraId: String, newStrengthLevel: Int) {
                    super.onTorchStrengthLevelChanged(cameraId, newStrengthLevel)
                    if (cameraId == torchCameraId) {
                        val level1to4 = if (maxTorchStrengthLevel <= 1) {
                            1
                        } else if (maxTorchStrengthLevel == 4) {
                            newStrengthLevel.coerceIn(1, 4)
                        } else {
                            ((newStrengthLevel - 1f) / (maxTorchStrengthLevel - 1).toFloat() * 3f + 1).roundToInt().coerceIn(1, 4)
                        }
                        IslandStateManager.updateFlashlightLevel(level1to4)
                    }
                }
            }
            torchCallback = cb
            cm.registerTorchCallback(cb, Handler(Looper.getMainLooper()))
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onCreate() {
        super.onCreate()
        serviceInstance = this
        settingsRepository = SettingsRepository(this)
        currentSettings = runBlocking(Dispatchers.IO) {
            try {
                settingsRepository.settingsFlow.first()
            } catch (_: Exception) {
                IslandSettings()
            }
        }
        IslandStateManager.dismissMediaOnPause = currentSettings.dismissMediaOnPause
        IslandStateManager.autoDismissNotifications = currentSettings.autoDismissNotifications
        val km = getSystemService(Context.KEYGUARD_SERVICE) as? KeyguardManager
        if (km != null) {
            IslandStateManager.setDeviceLocked(km.isKeyguardLocked)
        }
        lifecycleOwner.performRestore(null)
        lifecycleOwner.handleLifecycleEvent(Lifecycle.Event.ON_CREATE)
        lifecycleOwner.handleLifecycleEvent(Lifecycle.Event.ON_START)
        lifecycleOwner.handleLifecycleEvent(Lifecycle.Event.ON_RESUME)

        createNotificationChannel()
        initCameraTorch()
        startAutoUpdateChecker()

        persistentTelemetryManager = PersistentWidgetTelemetryManager(this).apply { start(currentSettings) }

        val filter = IntentFilter().apply {
            addAction("com.droidbubble.app.ACTION_SIMULATE_UNLOCK")
            addAction("com.droidbubble.app.ACTION_SIMULATE_BLUETOOTH")
            addAction("com.droidbubble.app.ACTION_SIMULATE_FLASHLIGHT")
            addAction("com.droidbubble.app.ACTION_SIMULATE_CHARGING")
            addAction("com.droidbubble.app.ACTION_SIMULATE_MEDIA")
            addAction("com.droidbubble.app.ACTION_SIMULATE_NOTIFICATION")
            addAction("com.droidbubble.app.ACTION_SIMULATE_PROGRESS")
            addAction("com.droidbubble.app.ACTION_SIMULATE_RECORDING")
            addAction("com.droidbubble.app.ACTION_SIMULATE_CALL")
            addAction("com.droidbubble.app.ACTION_SIMULATE_RINGER")
            addAction("com.droidbubble.app.ACTION_EXPAND")
            addAction("com.droidbubble.app.ACTION_COLLAPSE")
            addAction("com.droidbubble.app.ACTION_RESET")
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(debugReceiver, filter, Context.RECEIVER_EXPORTED)
        } else {
            registerReceiver(debugReceiver, filter)
        }

        val powerFilter = IntentFilter().apply {
            addAction(Intent.ACTION_POWER_CONNECTED)
            addAction("android.os.action.CHARGING")
            addAction(Intent.ACTION_USER_PRESENT)
            addAction(Intent.ACTION_SCREEN_ON)
            addAction(Intent.ACTION_SCREEN_OFF)
            addAction(android.bluetooth.BluetoothDevice.ACTION_ACL_CONNECTED)
            addAction("android.bluetooth.a2dp.profile.action.CONNECTION_STATE_CHANGED")
            addAction("android.bluetooth.headset.profile.action.CONNECTION_STATE_CHANGED")
            addAction("android.bluetooth.adapter.action.CONNECTION_STATE_CHANGED")
            addAction(AudioManager.RINGER_MODE_CHANGED_ACTION)
            addAction("android.media.INTERNAL_RINGER_MODE_CHANGED_ACTION")
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(powerReceiver, powerFilter, Context.RECEIVER_EXPORTED)
        } else {
            registerReceiver(powerReceiver, powerFilter)
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_STOP) {
            removeOverlay()
            stopForeground(STOP_FOREGROUND_REMOVE)
            stopSelf()
            return START_NOT_STICKY
        }

        startForegroundService()

        if (Settings.canDrawOverlays(this)) {
            initOverlay()
        } else {
            stopSelf()
        }

        return START_STICKY
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        updateWindowState(IslandStateManager.isExpanded.value)
    }

    private fun startForegroundService() {
        val openIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, openIntent, PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification_island)
            .setContentTitle("Droidbubble is active")
            .setContentText("Dynamic Island is running")
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setSilent(true)
            .setShowWhen(false)
            .setPriority(NotificationCompat.PRIORITY_MIN)
            .setVisibility(NotificationCompat.VISIBILITY_SECRET)
            .build()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            startForeground(
                NOTIFICATION_ID,
                notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE
            )
        } else {
            startForeground(NOTIFICATION_ID, notification)
        }
    }

    private var touchManager: IslandTouchManager? = null

    private fun disableMoveAnimation(params: WindowManager.LayoutParams) {
        try {
            val field = WindowManager.LayoutParams::class.java.getField("privateFlags")
            val current = field.getInt(params)
            field.setInt(params, current or 0x00000040)
        } catch (_: Exception) {}
    }



    fun handleAccessibilityServiceChanged(accessibilityService: IslandAccessibilityService?) {
        if (composeView != null) {
            removeOverlay()
            if (Settings.canDrawOverlays(this) && currentSettings.isEnabled) {
                initOverlay()
            }
        }
    }

    @OptIn(ExperimentalMaterial3ExpressiveApi::class)
    private fun initOverlay() {
        if (composeView != null) return

        val aService = IslandAccessibilityService.instance
        windowManager = if (aService != null) {
            aService.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        } else {
            getSystemService(Context.WINDOW_SERVICE) as WindowManager
        }

        val density = resources.displayMetrics.density
        val flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS

        val isLandscape = resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
        val isDeviceLocked = IslandStateManager.isDeviceLocked.value
        val allowedInOrientation = currentSettings.isEnabled && (currentSettings.showInLandscape || !isLandscape) && (!currentSettings.hideOnLockScreen || !isDeviceLocked)
        val isOccupied = IslandStateManager.isOccupied && allowedInOrientation
        val hasPersistentWidgets = (currentSettings.showFps || currentSettings.showWeather || currentSettings.showNetworkSpeed) && allowedInOrientation
        val shouldShowCompact = (isOccupied || currentSettings.alwaysShowPill || hasPersistentWidgets) && allowedInOrientation

        val mainHalf = (currentSettings.widthDp * 0.90f).coerceAtLeast(124f) / 2f
        val dualRightEdge = mainHalf + 8f + currentSettings.heightDp + 44f
        val chargingEdge = ((currentSettings.widthDp + 20f).coerceAtLeast(140f) + 80f) / 2f
        val totalWidthDp = (maxOf(dualRightEdge, chargingEdge) * 2f).coerceAtLeast(currentSettings.widthDp + 100f)

        val initialWidth = if (shouldShowCompact) {
            if (currentSettings.isLeftCutout) {
                WindowManager.LayoutParams.MATCH_PARENT
            } else {
                ((totalWidthDp + 2f * kotlin.math.abs(currentSettings.horizontalOffsetDp)) * density).toInt()
            }
        } else 1
        val initialHeight = if (shouldShowCompact) {
            ((currentSettings.topOffsetDp + currentSettings.heightDp + 12) * density).toInt()
        } else 1
        val initialFlags = if (isOccupied || hasPersistentWidgets) flags else (flags or WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)

        val windowType = if (aService != null) {
            WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            @Suppress("DEPRECATION")
            WindowManager.LayoutParams.TYPE_PHONE
        }

        layoutParams = WindowManager.LayoutParams(
            initialWidth,
            initialHeight,
            windowType,
            initialFlags,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.CENTER_HORIZONTAL
            x = 0
            y = 0
            windowAnimations = 0
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_ALWAYS
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
            }
        }
        disableMoveAnimation(layoutParams!!)

        val view = ComposeView(aService ?: this).apply {
            setViewTreeLifecycleOwner(lifecycleOwner)
            setViewTreeSavedStateRegistryOwner(lifecycleOwner)
            setViewTreeViewModelStoreOwner(lifecycleOwner)
            fitsSystemWindows = false

            setContent {
                val liveSettings by settingsRepository.settingsFlow.collectAsState(initial = currentSettings)

                MaterialExpressiveTheme(
                    colorScheme = darkColorScheme(),
                    motionScheme = MotionScheme.expressive()
                ) {
                    IslandOverlayRoot(
                        settings = liveSettings,
                        onBoundsChange = { _, _ -> }
                    )
                }
            }
        }

        composeView = view

        try {
            windowManager?.addView(view, layoutParams)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        // Listen for expansion and state changes to resize WindowManager touch bounds
        serviceScope.launch {
            kotlinx.coroutines.flow.combine(
                IslandStateManager.isExpanded,
                kotlinx.coroutines.flow.combine(
                    IslandStateManager.activeMedia,
                    IslandStateManager.activeNotification,
                    IslandStateManager.activeProgress,
                    IslandStateManager.screenRecordingState,
                    IslandStateManager.activeCallState
                ) { _, _, _, _, _ -> Unit },
                kotlinx.coroutines.flow.combine(
                    IslandStateManager.activeCharging,
                    IslandStateManager.isUnlocked,
                    IslandStateManager.isBluetoothConnected,
                    IslandStateManager.activeRingerMode,
                    IslandStateManager.isFlashlightOn
                ) { _, _, _, _, _ -> Unit },
                IslandStateManager.isDeviceLocked
            ) { expanded, _, _, _ -> expanded }.collectLatest { expanded ->
                updateWindowState(expanded)
            }
        }

        // Listen for keyboard focus state to enable soft keyboard for inline replies
        serviceScope.launch {
            IslandStateManager.isKeyboardFocused.collectLatest { focused ->
                val view = composeView ?: return@collectLatest
                val params = layoutParams ?: return@collectLatest
                if (focused) {
                    params.flags = params.flags and WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE.inv()
                } else {
                    params.flags = params.flags or WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                }
                disableMoveAnimation(params)
                runCatching { windowManager?.updateViewLayout(view, params) }
            }
        }

        // Listen for settings changes
        serviceScope.launch {
            settingsRepository.settingsFlow.collectLatest { settings ->
                currentSettings = settings
                persistentTelemetryManager?.updateSettings(settings)
                IslandStateManager.dismissMediaOnPause = settings.dismissMediaOnPause
                IslandStateManager.autoDismissNotifications = settings.autoDismissNotifications
                if (!settings.isEnabled) {
                    removeOverlay()
                } else if (composeView == null) {
                    initOverlay()
                } else {
                    updateWindowState(IslandStateManager.isExpanded.value)
                }
            }
        }
    }

    private var isCollapsing = false
    private var collapseJob: kotlinx.coroutines.Job? = null
    private var wasExpanded = false

    private fun applyLayoutParams(
        view: View,
        params: WindowManager.LayoutParams,
        width: Int,
        height: Int,
        x: Int,
        y: Int,
        gravity: Int,
        flags: Int
    ) {
        if (params.width == width &&
            params.height == height &&
            params.x == x &&
            params.y == y &&
            params.gravity == gravity &&
            params.flags == flags
        ) {
            return
        }
        params.width = width
        params.height = height
        params.x = x
        params.y = y
        params.gravity = gravity
        params.flags = flags
        disableMoveAnimation(params)
        runCatching { windowManager?.updateViewLayout(view, params) }
    }

    private fun updateWindowState(expanded: Boolean) {
        val view = composeView ?: return
        val params = layoutParams ?: return
        val density = resources.displayMetrics.density

        val isLandscape = resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
        val km = getSystemService(Context.KEYGUARD_SERVICE) as? KeyguardManager
        if (km != null) {
            IslandStateManager.setDeviceLocked(km.isKeyguardLocked)
        }
        val isDeviceLocked = IslandStateManager.isDeviceLocked.value
        val allowedInOrientation = currentSettings.isEnabled && (currentSettings.showInLandscape || !isLandscape) && (!currentSettings.hideOnLockScreen || !isDeviceLocked)

        if (!allowedInOrientation) {
            collapseJob?.cancel()
            isCollapsing = false
            applyLayoutParams(
                view = view,
                params = params,
                width = 1,
                height = 1,
                x = 0,
                y = 0,
                gravity = Gravity.TOP or Gravity.CENTER_HORIZONTAL,
                flags = params.flags or WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
            )
            return
        }

        if (expanded) {
            collapseJob?.cancel()
            isCollapsing = false
            wasExpanded = true
            // Full screen touch area to allow tapping outside to dismiss
            applyLayoutParams(
                view = view,
                params = params,
                width = WindowManager.LayoutParams.MATCH_PARENT,
                height = WindowManager.LayoutParams.MATCH_PARENT,
                x = 0,
                y = 0,
                gravity = Gravity.TOP or Gravity.CENTER_HORIZONTAL,
                flags = params.flags and WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE.inv()
            )
        } else {
            val isCharging = currentSettings.showCharging && IslandStateManager.isChargingActive
            val isUnlock = IslandStateManager.isUnlockActive
            val isBluetooth = currentSettings.showBluetooth && IslandStateManager.isBluetoothActive
            val isRingerMode = currentSettings.showRingerMode && IslandStateManager.isRingerModeActive
            val isFlashlight = currentSettings.showFlashlight && IslandStateManager.isFlashlightActive
            val isOccupied = (currentSettings.showCalls && IslandStateManager.isCallActive) ||
                    (currentSettings.showMedia && IslandStateManager.isMediaActive) ||
                    (currentSettings.showNotifications && IslandStateManager.isNotificationActive) ||
                    (currentSettings.showLiveProgress && IslandStateManager.isProgressActive) ||
                    (currentSettings.showScreenRecording && IslandStateManager.isRecordingActive) ||
                    isFlashlight ||
                    isCharging ||
                    isUnlock ||
                    isBluetooth ||
                    isRingerMode
            val hasPersistentWidgets = currentSettings.showFps || currentSettings.showWeather || currentSettings.showNetworkSpeed
            val shouldShowCompact = isOccupied || currentSettings.alwaysShowPill || hasPersistentWidgets

            // Use a unified compact window width across all compact states (single, dual, charging, unlock, ringer)
            // so WindowManager never resizes params.width mid-animation, completely eliminating horizontal teleporting!
            val mainHalf = (currentSettings.widthDp * 0.90f).coerceAtLeast(124f) / 2f
            val dualRightEdge = mainHalf + 8f + currentSettings.heightDp + 44f
            val chargingEdge = ((currentSettings.widthDp + 20f).coerceAtLeast(140f) + 80f) / 2f
            val totalWidthDp = (maxOf(dualRightEdge, chargingEdge) * 2f).coerceAtLeast(currentSettings.widthDp + 100f)

            fun applyCompactBounds() {
                collapseJob?.cancel()
                if (shouldShowCompact) {
                    val targetFlags = if (isOccupied || hasPersistentWidgets) {
                        params.flags and WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE.inv()
                    } else {
                        params.flags or WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
                    }
                    val compactWindowWidth = if (currentSettings.isLeftCutout) {
                        WindowManager.LayoutParams.MATCH_PARENT
                    } else {
                        ((totalWidthDp + 2f * kotlin.math.abs(currentSettings.horizontalOffsetDp)) * density).toInt()
                    }
                    applyLayoutParams(
                        view = view,
                        params = params,
                        width = compactWindowWidth,
                        height = ((currentSettings.topOffsetDp + currentSettings.heightDp + 12) * density).toInt(),
                        x = 0,
                        y = 0,
                        gravity = Gravity.TOP or Gravity.CENTER_HORIZONTAL,
                        flags = targetFlags
                    )
                } else {
                    applyLayoutParams(
                        view = view,
                        params = params,
                        width = 1,
                        height = 1,
                        x = 0,
                        y = 0,
                        gravity = Gravity.TOP or Gravity.CENTER_HORIZONTAL,
                        flags = params.flags or WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
                    )
                }
            }

            if (isCharging || isUnlock || isBluetooth || isRingerMode) {
                isCollapsing = false
                collapseJob?.cancel()
            } else if (isCollapsing) {
                return
            }

            val speedFactor = currentSettings.animationSpeed.coerceIn(0.4f, 2.5f)
            val collapseDelayMs = (560f / speedFactor).toLong().coerceIn(380L, 950L)

            if (wasExpanded) {
                wasExpanded = false
                isCollapsing = true
                // Keep MATCH_PARENT until the spring collapse animation has completely settled!
                collapseJob = serviceScope.launch {
                    kotlinx.coroutines.delay(collapseDelayMs)
                    isCollapsing = false
                    if (!IslandStateManager.isExpanded.value) {
                        applyCompactBounds()
                    }
                }
            } else if (!isOccupied && !currentSettings.alwaysShowPill && !hasPersistentWidgets) {
                // When unoccupied and not always showing pill and no persistent widgets, keep bounds briefly for the smooth shrink-to-middle animation, then collapse
                collapseJob?.cancel()
                collapseJob = serviceScope.launch {
                    kotlinx.coroutines.delay((420f / speedFactor).toLong().coerceIn(320L, 750L))
                    if (!isOccupied && !IslandStateManager.isExpanded.value && !currentSettings.alwaysShowPill && !hasPersistentWidgets) {
                        applyLayoutParams(
                            view = view,
                            params = params,
                            width = 1,
                            height = 1,
                            x = 0,
                            y = 0,
                            gravity = Gravity.TOP or Gravity.CENTER_HORIZONTAL,
                            flags = params.flags or WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
                        )
                    }
                }
            } else {
                applyCompactBounds()
            }
        }
    }

    private fun removeOverlay() {
        composeView?.let { view ->
            try {
                windowManager?.removeView(view)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        composeView = null
    }

    override fun onDestroy() {
        super.onDestroy()
        if (serviceInstance === this) {
            serviceInstance = null
        }
        runCatching { unregisterReceiver(debugReceiver) }
        persistentTelemetryManager?.stop()
        persistentTelemetryManager = null
        runCatching { unregisterReceiver(powerReceiver) }
        torchCallback?.let { cb ->
            runCatching { cameraManager?.unregisterTorchCallback(cb) }
        }
        torchCallback = null
        lifecycleOwner.handleLifecycleEvent(Lifecycle.Event.ON_PAUSE)
        lifecycleOwner.handleLifecycleEvent(Lifecycle.Event.ON_STOP)
        lifecycleOwner.handleLifecycleEvent(Lifecycle.Event.ON_DESTROY)
        removeOverlay()
        serviceScope.cancel()
    }


    private fun startAutoUpdateChecker() {
        serviceScope.launch(Dispatchers.IO) {
            delay(15_000L)
            while (isActive) {
                try {
                    val result = com.droidbubble.app.util.UpdateManager.checkGithubUpdates(applicationContext)
                    if (result is com.droidbubble.app.util.UpdateCheckResult.UpdateAvailable) {
                        showUpdateNotification(result.release.version)
                    } else if (result is com.droidbubble.app.util.UpdateCheckResult.UpToDate) {
                        cancelUpdateNotification()
                    }
                } catch (e: Exception) {
                    Log.w("IslandOverlayService", "Auto-update check encountered error", e)
                }
                delay(2 * 60 * 60 * 1000L)
            }
        }
    }

    private fun showUpdateNotification(version: String) {
        val notificationManager = getSystemService(NotificationManager::class.java) ?: return
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_UPDATES_ID,
                "App Updates",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifies when a new Droidbubble update is available"
                setShowBadge(true)
            }
            notificationManager.createNotificationChannel(channel)
        }

        val launchIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("EXTRA_SHOW_UPDATE", true)
        }
        val pendingIntent = PendingIntent.getActivity(
            this,
            UPDATE_NOTIFICATION_ID,
            launchIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else 0)
        )

        val notification = NotificationCompat.Builder(this, CHANNEL_UPDATES_ID)
            .setSmallIcon(R.drawable.ic_notification_island)
            .setContentTitle("Update to $version")
            .setContentText("Update to $version")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setOngoing(true)
            .setAutoCancel(false)
            .setContentIntent(pendingIntent)
            .build()

        notificationManager.notify(UPDATE_NOTIFICATION_ID, notification)
    }

    private fun cancelUpdateNotification() {
        val notificationManager = getSystemService(NotificationManager::class.java) ?: return
        notificationManager.cancel(UPDATE_NOTIFICATION_ID)
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = getSystemService(NotificationManager::class.java)
            runCatching {
                manager?.deleteNotificationChannel("droidbubble_overlay_channel")
                manager?.deleteNotificationChannel("droidbubble_overlay_service")
            }
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Droidbubble Service",
                NotificationManager.IMPORTANCE_MIN
            ).apply {
                description = "Keeps Dynamic Island running smoothly"
                setShowBadge(false)
                enableLights(false)
                enableVibration(false)
                lockscreenVisibility = android.app.Notification.VISIBILITY_SECRET
            }
            manager?.createNotificationChannel(channel)
        }
    }
}

// Custom LifecycleOwner for Compose in WindowManager Service
class CustomLifecycleOwner : LifecycleOwner, ViewModelStoreOwner, SavedStateRegistryOwner {
    private val lifecycleRegistry = LifecycleRegistry(this)
    private val savedStateRegistryController = SavedStateRegistryController.create(this)
    private val store = ViewModelStore()

    override val lifecycle: Lifecycle get() = lifecycleRegistry
    override val viewModelStore: ViewModelStore get() = store
    override val savedStateRegistry: SavedStateRegistry get() = savedStateRegistryController.savedStateRegistry

    fun handleLifecycleEvent(event: Lifecycle.Event) {
        lifecycleRegistry.handleLifecycleEvent(event)
    }

    fun performRestore(savedState: Bundle?) {
        savedStateRegistryController.performRestore(savedState)
    }
}
