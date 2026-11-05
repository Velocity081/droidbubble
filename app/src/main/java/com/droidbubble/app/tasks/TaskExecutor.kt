package com.droidbubble.app.tasks

import android.accessibilityservice.AccessibilityService
import android.app.NotificationChannel
import android.app.NotificationManager
import android.bluetooth.BluetoothAdapter
import android.content.Context
import android.content.Intent
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.media.AudioManager
import android.net.wifi.WifiManager
import android.os.Build
import android.provider.Settings
import android.view.KeyEvent
import androidx.core.app.NotificationCompat
import com.droidbubble.app.R
import com.droidbubble.app.island.IslandStateManager
import com.droidbubble.app.service.IslandAccessibilityService

object TaskExecutor {

    fun execute(context: Context, shortcut: TaskShortcut) {
        val appContext = context.applicationContext

        // Run chained actions sequentially
        for (action in shortcut.actions) {
            executeAction(appContext, action)
        }

        // If option enabled, briefly expand Droidbubble with task icon and checkmark
        if (shortcut.showInBubble) {
            IslandStateManager.triggerTaskConfirmation(shortcut.iconName, shortcut.name)
        }
    }

    private fun executeAction(context: Context, action: TaskAction) {
        when (action) {
            is TaskAction.OpenApp -> {
                runCatching {
                    val intent = context.packageManager.getLaunchIntentForPackage(action.packageName)?.apply {
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    if (intent != null) {
                        context.startActivity(intent)
                    }
                }
            }
            is TaskAction.Wifi -> {
                val wm = context.getSystemService(Context.WIFI_SERVICE) as? WifiManager
                val currentOn = wm?.isWifiEnabled == true
                val targetOn = when (action.type) {
                    ToggleActionType.TOGGLE -> !currentOn
                    ToggleActionType.TURN_ON -> true
                    ToggleActionType.TURN_OFF -> false
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    runCatching {
                        val panelIntent = Intent(Settings.Panel.ACTION_WIFI).apply {
                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        }
                        context.startActivity(panelIntent)
                    }.onFailure {
                        runCatching {
                            val intent = Intent(Settings.ACTION_WIFI_SETTINGS).apply {
                                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            }
                            context.startActivity(intent)
                        }
                    }
                } else {
                    @Suppress("DEPRECATION")
                    runCatching { wm?.isWifiEnabled = targetOn }
                }
            }
            is TaskAction.MobileData -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    runCatching {
                        val panelIntent = Intent(Settings.Panel.ACTION_INTERNET_CONNECTIVITY).apply {
                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        }
                        context.startActivity(panelIntent)
                        return
                    }
                }
                runCatching {
                    val intent = Intent(Settings.ACTION_DATA_ROAMING_SETTINGS).apply {
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    context.startActivity(intent)
                }.onFailure {
                    runCatching {
                        val intent = Intent(Settings.ACTION_NETWORK_OPERATOR_SETTINGS).apply {
                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        }
                        context.startActivity(intent)
                    }
                }
            }
            is TaskAction.Flashlight -> {
                val cm = context.getSystemService(Context.CAMERA_SERVICE) as? CameraManager ?: return
                runCatching {
                    val id = cm.cameraIdList.firstOrNull { camId ->
                        cm.getCameraCharacteristics(camId).get(CameraCharacteristics.FLASH_INFO_AVAILABLE) == true
                    } ?: return
                    val currentOn = IslandStateManager.isFlashlightOn.value
                    val targetOn = when (action.type) {
                        ToggleActionType.TOGGLE -> !currentOn
                        ToggleActionType.TURN_ON -> true
                        ToggleActionType.TURN_OFF -> false
                    }
                    cm.setTorchMode(id, targetOn)
                }
            }
            is TaskAction.LowPower -> {
                runCatching {
                    val intent = Intent(Settings.ACTION_BATTERY_SAVER_SETTINGS).apply {
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    context.startActivity(intent)
                }
            }
            is TaskAction.AirplaneMode -> {
                runCatching {
                    val intent = Intent(Settings.ACTION_AIRPLANE_MODE_SETTINGS).apply {
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    context.startActivity(intent)
                }
            }
            is TaskAction.Bluetooth -> {
                runCatching {
                    @Suppress("DEPRECATION")
                    val btAdapter = BluetoothAdapter.getDefaultAdapter()
                    if (btAdapter != null) {
                        val currentOn = btAdapter.isEnabled
                        val targetOn = when (action.type) {
                            ToggleActionType.TOGGLE -> !currentOn
                            ToggleActionType.TURN_ON -> true
                            ToggleActionType.TURN_OFF -> false
                        }
                        @Suppress("DEPRECATION")
                        val success = if (targetOn) btAdapter.enable() else btAdapter.disable()
                        if (!success) {
                            val intent = Intent(Settings.ACTION_BLUETOOTH_SETTINGS).apply {
                                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            }
                            context.startActivity(intent)
                        }
                    } else {
                        val intent = Intent(Settings.ACTION_BLUETOOTH_SETTINGS).apply {
                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        }
                        context.startActivity(intent)
                    }
                }
            }
            is TaskAction.RingerMode -> {
                val am = context.getSystemService(Context.AUDIO_SERVICE) as? AudioManager ?: return
                runCatching {
                    val current = am.ringerMode
                    val target = when (action.type) {
                        RingerActionType.SET_RING -> AudioManager.RINGER_MODE_NORMAL
                        RingerActionType.SET_VIBRATE -> AudioManager.RINGER_MODE_VIBRATE
                        RingerActionType.TOGGLE -> if (current == AudioManager.RINGER_MODE_VIBRATE) AudioManager.RINGER_MODE_NORMAL else AudioManager.RINGER_MODE_VIBRATE
                    }
                    am.ringerMode = target
                }.onFailure {
                    runCatching {
                        val intent = Intent(Settings.ACTION_SOUND_SETTINGS).apply {
                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        }
                        context.startActivity(intent)
                    }
                }
            }
            is TaskAction.SetBrightness -> {
                runCatching {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && Settings.System.canWrite(context)) {
                        val brightness = (action.level * 255 / 100).coerceIn(0, 255)
                        Settings.System.putInt(context.contentResolver, Settings.System.SCREEN_BRIGHTNESS_MODE, Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL)
                        Settings.System.putInt(context.contentResolver, Settings.System.SCREEN_BRIGHTNESS, brightness)
                    } else {
                        val intent = Intent(Settings.ACTION_DISPLAY_SETTINGS).apply {
                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        }
                        context.startActivity(intent)
                    }
                }
            }
            is TaskAction.SetVolume -> {
                val am = context.getSystemService(Context.AUDIO_SERVICE) as? AudioManager ?: return
                runCatching {
                    val maxVol = am.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
                    val target = (action.level * maxVol / 100).coerceIn(0, maxVol)
                    am.setStreamVolume(AudioManager.STREAM_MUSIC, target, AudioManager.FLAG_SHOW_UI)
                }
            }
            is TaskAction.TakeScreenshot -> {
                runCatching {
                    val accessibility = IslandAccessibilityService.instance
                    if (accessibility != null) {
                        accessibility.performGlobalAction(AccessibilityService.GLOBAL_ACTION_TAKE_SCREENSHOT)
                    }
                }
            }
            is TaskAction.GoHome -> {
                runCatching {
                    val accessibility = IslandAccessibilityService.instance
                    val handled = accessibility?.performGlobalAction(AccessibilityService.GLOBAL_ACTION_HOME) ?: false
                    if (!handled) {
                        val homeIntent = Intent(Intent.ACTION_MAIN).apply {
                            addCategory(Intent.CATEGORY_HOME)
                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        }
                        context.startActivity(homeIntent)
                    }
                }
            }
            is TaskAction.MediaControls -> {
                runCatching {
                    val am = context.getSystemService(Context.AUDIO_SERVICE) as? AudioManager
                    when (action.type) {
                        MediaActionType.PLAY -> {
                            if (IslandStateManager.onPlayPause != null) {
                                IslandStateManager.onPlayPause?.invoke()
                            } else {
                                am?.dispatchMediaKeyEvent(KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_MEDIA_PLAY))
                                am?.dispatchMediaKeyEvent(KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_MEDIA_PLAY))
                            }
                        }
                        MediaActionType.PAUSE -> {
                            if (IslandStateManager.onPauseMedia != null) {
                                IslandStateManager.onPauseMedia?.invoke()
                            } else {
                                am?.dispatchMediaKeyEvent(KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_MEDIA_PAUSE))
                                am?.dispatchMediaKeyEvent(KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_MEDIA_PAUSE))
                            }
                        }
                        MediaActionType.TOGGLE_PLAYBACK -> {
                            if (IslandStateManager.onPlayPause != null) {
                                IslandStateManager.onPlayPause?.invoke()
                            } else {
                                am?.dispatchMediaKeyEvent(KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE))
                                am?.dispatchMediaKeyEvent(KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE))
                            }
                        }
                        MediaActionType.SKIP -> {
                            if (IslandStateManager.onSkipNext != null) {
                                IslandStateManager.onSkipNext?.invoke()
                            } else {
                                am?.dispatchMediaKeyEvent(KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_MEDIA_NEXT))
                                am?.dispatchMediaKeyEvent(KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_MEDIA_NEXT))
                            }
                        }
                    }
                }
            }
            is TaskAction.SendNotification -> {
                runCatching {
                    val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager ?: return
                    val channelId = "droidtools_tasks_notifications"
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        val channel = NotificationChannel(
                            channelId,
                            "Tasks Notifications",
                            NotificationManager.IMPORTANCE_HIGH
                        ).apply {
                            description = "Notifications triggered by Tasks"
                        }
                        nm.createNotificationChannel(channel)
                    }
                    val notif = NotificationCompat.Builder(context, channelId)
                        .setSmallIcon(R.drawable.ic_notification_island)
                        .setContentTitle(action.title.ifBlank { "Task Notification" })
                        .setContentText(action.text.ifBlank { "Task executed" })
                        .setPriority(NotificationCompat.PRIORITY_HIGH)
                        .setAutoCancel(true)
                        .build()
                    nm.notify((System.currentTimeMillis() % 100000).toInt(), notif)
                }
            }
        }
    }
}
