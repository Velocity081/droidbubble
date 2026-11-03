package com.droidbubble.app.droidbar

import android.accessibilityservice.AccessibilityService
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.media.AudioManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.wifi.WifiManager
import android.os.Build
import android.provider.Settings
import android.view.HapticFeedbackConstants
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.FlashlightOff
import androidx.compose.material.icons.filled.FlashlightOn
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Screenshot
import androidx.compose.material.icons.filled.SignalCellularAlt
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.coerceAtLeast
import androidx.compose.ui.unit.dp
import com.droidbubble.app.island.IslandStateManager
import com.droidbubble.app.island.M3CookieShape
import com.droidbubble.app.model.IslandSettings
import com.droidbubble.app.service.IslandAccessibilityService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun DroidbarOverlay(
    context: Context,
    settings: IslandSettings,
    onExpandChanged: (Boolean) -> Unit
) {
    val view = LocalView.current
    val configuration = LocalConfiguration.current
    val coroutineScope = rememberCoroutineScope()
    val animProgress = remember { Animatable(0f) }
    var isExpanded by remember { mutableStateOf(false) }
    var isWindowExpanded by remember { mutableStateOf(false) }
    var apps by remember { mutableStateOf<List<BarAppInfo>>(emptyList()) }

    // Flashlight state from IslandStateManager
    val isFlashlightOn by IslandStateManager.isFlashlightOn.collectAsState()

    // Ringer mode state
    var currentRingerMode by remember {
        val am = context.getSystemService(Context.AUDIO_SERVICE) as? AudioManager
        mutableIntStateOf(am?.ringerMode ?: AudioManager.RINGER_MODE_NORMAL)
    }

    // Wi-Fi state
    var isWifiEnabled by remember {
        val wm = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as? WifiManager
        mutableStateOf(wm?.isWifiEnabled == true)
    }

    // Mobile data connectivity state
    var isMobileDataActive by remember {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
        val caps = cm?.getNetworkCapabilities(cm.activeNetwork)
        mutableStateOf(caps?.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) == true)
    }

    DisposableEffect(context) {
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(c: Context?, intent: Intent?) {
                when (intent?.action) {
                    AudioManager.RINGER_MODE_CHANGED_ACTION,
                    "android.media.INTERNAL_RINGER_MODE_CHANGED_ACTION" -> {
                        val am = context.getSystemService(Context.AUDIO_SERVICE) as? AudioManager
                        currentRingerMode = am?.ringerMode ?: AudioManager.RINGER_MODE_NORMAL
                    }
                    WifiManager.WIFI_STATE_CHANGED_ACTION -> {
                        val state = intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE, WifiManager.WIFI_STATE_UNKNOWN)
                        isWifiEnabled = (state == WifiManager.WIFI_STATE_ENABLED)
                    }
                    ConnectivityManager.CONNECTIVITY_ACTION -> {
                        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
                        val caps = cm?.getNetworkCapabilities(cm.activeNetwork)
                        isMobileDataActive = (caps?.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) == true)
                    }
                }
            }
        }
        val filter = IntentFilter().apply {
            addAction(AudioManager.RINGER_MODE_CHANGED_ACTION)
            addAction("android.media.INTERNAL_RINGER_MODE_CHANGED_ACTION")
            addAction(WifiManager.WIFI_STATE_CHANGED_ACTION)
            @Suppress("DEPRECATION")
            addAction(ConnectivityManager.CONNECTIVITY_ACTION)
        }
        context.registerReceiver(receiver, filter)
        onDispose {
            runCatching { context.unregisterReceiver(receiver) }
        }
    }

    val loadApps: suspend () -> List<BarAppInfo> = {
        withContext(Dispatchers.IO) {
            if (settings.droidbarUseMostUsedApps) {
                MostUsedAppsHelper.getMostUsedApps(context, limit = 6)
            } else {
                val custom = MostUsedAppsHelper.getAppsByPackages(context, settings.droidbarCustomApps)
                if (custom.isNotEmpty()) custom else MostUsedAppsHelper.getMostUsedApps(context, limit = 6)
            }
        }
    }

    LaunchedEffect(settings.droidbarUseMostUsedApps, settings.droidbarCustomApps) {
        apps = loadApps()
    }

    // Animate expand and collapse smoothly with single Animatable
    LaunchedEffect(isExpanded) {
        if (isExpanded) {
            isWindowExpanded = true
            onExpandChanged(true)
            apps = loadApps()
            animProgress.animateTo(
                targetValue = 1f,
                animationSpec = spring(
                    dampingRatio = 0.68f,
                    stiffness = 380f
                )
            )
        } else {
            animProgress.animateTo(
                targetValue = 0f,
                animationSpec = tween(
                    durationMillis = 260,
                    easing = FastOutSlowInEasing
                )
            )
            isWindowExpanded = false
            onExpandChanged(false)
        }
    }

    val controlCount = (if (settings.droidbarShowFlashlight) 1 else 0) +
            (if (settings.droidbarShowRingMode) 1 else 0) +
            (if (settings.droidbarShowScreenshot) 1 else 0) +
            (if (settings.droidbarShowWifi) 1 else 0) +
            (if (settings.droidbarShowMobileData) 1 else 0)

    val hasControls = controlCount > 0
    val appCount = apps.size
    val hasApps = appCount > 0
    val totalItems = controlCount + appCount

    val targetExpandedHeight = ((totalItems * 48) + (if (hasControls && hasApps) 14 else 0) + 24).dp.coerceAtLeast(80.dp)
    val maxAvailableHeight = (configuration.screenHeightDp - 64).dp.coerceAtLeast(180.dp)
    val finalExpandedHeight = targetExpandedHeight.coerceAtMost(maxAvailableHeight)

    val progress = animProgress.value.coerceIn(0f, 1.25f)

    val barWidth = (5.dp + (68.dp - 5.dp) * progress).coerceAtLeast(5.dp)
    val barHeight = (44.dp + (finalExpandedHeight - 44.dp) * progress).coerceAtLeast(44.dp)
    val endMargin = (8.dp * progress).coerceAtLeast(0.dp)

    // Smooth corner radius interpolation to eliminate corners sharpening before collapse
    val startRadius = (3.dp + (26.dp - 3.dp) * progress).coerceAtLeast(3.dp)
    val endRadius = (26.dp * progress).coerceAtLeast(0.dp)

    // Quick content fade during collapse to avoid layout hitching
    val contentAlpha by animateFloatAsState(
        targetValue = if (isExpanded) 1f else 0f,
        animationSpec = if (isExpanded) tween(200, delayMillis = 60) else tween(90),
        label = "contentAlpha"
    )

    // Backdrop dim fade
    val backdropAlpha by animateFloatAsState(
        targetValue = if (isExpanded) 0.30f else 0f,
        animationSpec = tween(durationMillis = if (isExpanded) 220 else 180),
        label = "backdropAlpha"
    )

    // Bounce scales for controls
    val (flashlightBounceScale, triggerFlashlightBounce) = rememberBounceScale()
    val (ringerBounceScale, triggerRingerBounce) = rememberBounceScale()
    val (screenshotBounceScale, triggerScreenshotBounce) = rememberBounceScale()
    val (wifiBounceScale, triggerWifiBounce) = rememberBounceScale()
    val (mobileDataBounceScale, triggerMobileDataBounce) = rememberBounceScale()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .then(
                if (isWindowExpanded && backdropAlpha > 0.01f) {
                    Modifier
                        .background(Color.Black.copy(alpha = backdropAlpha))
                        .clickable(
                            indication = null,
                            interactionSource = remember { MutableInteractionSource() }
                        ) {
                            view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                            isExpanded = false
                        }
                } else Modifier
            ),
        contentAlignment = Alignment.CenterEnd
    ) {
        val currentShape = RoundedCornerShape(
            topStart = startRadius,
            bottomStart = startRadius,
            topEnd = endRadius,
            bottomEnd = endRadius
        )

        // The Droidbar surface: fully opaque black, NO outline, smooth collapse and bounce expand
        Box(
            modifier = Modifier
                .padding(end = endMargin)
                .width(barWidth)
                .height(barHeight)
                .shadow(
                    elevation = if (isExpanded) 12.dp else 4.dp,
                    shape = currentShape
                )
                .clip(currentShape)
                .background(Color.Black)
                .clickable(
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() }
                ) {
                    if (!isExpanded) {
                        view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                        isExpanded = true
                    }
                },
            contentAlignment = Alignment.Center
        ) {
            if (!isExpanded && contentAlpha < 0.05f) {
                // Subtle white edge indicator stripe inside the black edge bar for visibility
                Box(
                    modifier = Modifier
                        .width(2.5.dp)
                        .height(26.dp)
                        .clip(RoundedCornerShape(1.5.dp))
                        .background(Color.White.copy(alpha = 0.70f))
                )
            } else if (isExpanded || contentAlpha > 0.02f) {
                // Fixed 68dp container avoids child reflow and constraint hitching during size animation
                Box(
                    modifier = Modifier
                        .width(68.dp)
                        .height(barHeight)
                        .graphicsLayer {
                            alpha = contentAlpha
                            scaleX = 0.88f + (0.12f * contentAlpha)
                            scaleY = 0.88f + (0.12f * contentAlpha)
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(vertical = 12.dp, horizontal = 10.dp)
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(6.dp, Alignment.CenterVertically),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // 1. Flashlight toggle control (12-sided M3 Cookie Shape, subtle bounce)
                        if (settings.droidbarShowFlashlight) {
                            val isFlashlightActive = isFlashlightOn
                            val containerColor = if (isFlashlightActive) {
                                MaterialTheme.colorScheme.primaryContainer
                            } else {
                                Color.White.copy(alpha = 0.10f)
                            }
                            val contentColor = if (isFlashlightActive) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                Color.White.copy(alpha = 0.85f)
                            }

                            Surface(
                                modifier = Modifier
                                    .size(44.dp)
                                    .graphicsLayer {
                                        scaleX = flashlightBounceScale
                                        scaleY = flashlightBounceScale
                                    },
                                shape = M3CookieShape,
                                color = containerColor,
                                contentColor = contentColor,
                                onClick = {
                                    triggerFlashlightBounce()
                                    view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                                    toggleFlashlight(context)
                                }
                            ) {
                                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = if (isFlashlightActive) Icons.Default.FlashlightOn else Icons.Default.FlashlightOff,
                                        contentDescription = "Flashlight",
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                            }
                        }

                        // 2. Ring mode toggle control: ONLY Ring and Vibrate (12-sided M3 Cookie Shape, subtle bounce)
                        if (settings.droidbarShowRingMode) {
                            val isVibrate = currentRingerMode == AudioManager.RINGER_MODE_VIBRATE
                            val containerColor = if (isVibrate) {
                                MaterialTheme.colorScheme.primaryContainer
                            } else {
                                Color.White.copy(alpha = 0.10f)
                            }
                            val contentColor = if (isVibrate) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                Color.White.copy(alpha = 0.85f)
                            }

                            Surface(
                                modifier = Modifier
                                    .size(44.dp)
                                    .graphicsLayer {
                                        scaleX = ringerBounceScale
                                        scaleY = ringerBounceScale
                                    },
                                shape = M3CookieShape,
                                color = containerColor,
                                contentColor = contentColor,
                                onClick = {
                                    triggerRingerBounce()
                                    view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                                    toggleRingVibrateMode(context) { newMode ->
                                        currentRingerMode = newMode
                                    }
                                }
                            ) {
                                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = if (isVibrate) Icons.Default.Vibration else Icons.Default.NotificationsActive,
                                        contentDescription = if (isVibrate) "Vibrate" else "Ring",
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                            }
                        }

                        // 3. Screenshot control (12-sided M3 Cookie Shape, subtle bounce)
                        if (settings.droidbarShowScreenshot) {
                            Surface(
                                modifier = Modifier
                                    .size(44.dp)
                                    .graphicsLayer {
                                        scaleX = screenshotBounceScale
                                        scaleY = screenshotBounceScale
                                    },
                                shape = M3CookieShape,
                                color = Color.White.copy(alpha = 0.10f),
                                contentColor = Color.White.copy(alpha = 0.85f),
                                onClick = {
                                    triggerScreenshotBounce()
                                    view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                                    isExpanded = false
                                    coroutineScope.launch {
                                        delay(180L)
                                        val accessibility = IslandAccessibilityService.instance
                                        if (accessibility != null) {
                                            accessibility.performGlobalAction(AccessibilityService.GLOBAL_ACTION_TAKE_SCREENSHOT)
                                        }
                                    }
                                }
                            ) {
                                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.Screenshot,
                                        contentDescription = "Screenshot",
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                            }
                        }

                        // 4. Wi-Fi toggle control (12-sided M3 Cookie Shape, subtle bounce)
                        if (settings.droidbarShowWifi) {
                            val containerColor = if (isWifiEnabled) {
                                MaterialTheme.colorScheme.primaryContainer
                            } else {
                                Color.White.copy(alpha = 0.10f)
                            }
                            val contentColor = if (isWifiEnabled) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                Color.White.copy(alpha = 0.85f)
                            }

                            Surface(
                                modifier = Modifier
                                    .size(44.dp)
                                    .graphicsLayer {
                                        scaleX = wifiBounceScale
                                        scaleY = wifiBounceScale
                                    },
                                shape = M3CookieShape,
                                color = containerColor,
                                contentColor = contentColor,
                                onClick = {
                                    triggerWifiBounce()
                                    view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                                    isExpanded = false
                                    toggleOrOpenWifi(context)
                                }
                            ) {
                                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.Wifi,
                                        contentDescription = "Wi-Fi",
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                            }
                        }

                        // 5. Mobile Data toggle control (12-sided M3 Cookie Shape, subtle bounce)
                        if (settings.droidbarShowMobileData) {
                            val containerColor = if (isMobileDataActive) {
                                MaterialTheme.colorScheme.primaryContainer
                            } else {
                                Color.White.copy(alpha = 0.10f)
                            }
                            val contentColor = if (isMobileDataActive) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                Color.White.copy(alpha = 0.85f)
                            }

                            Surface(
                                modifier = Modifier
                                    .size(44.dp)
                                    .graphicsLayer {
                                        scaleX = mobileDataBounceScale
                                        scaleY = mobileDataBounceScale
                                    },
                                shape = M3CookieShape,
                                color = containerColor,
                                contentColor = contentColor,
                                onClick = {
                                    triggerMobileDataBounce()
                                    view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                                    isExpanded = false
                                    openMobileDataPanel(context)
                                }
                            ) {
                                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.SignalCellularAlt,
                                        contentDescription = "Mobile Data",
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                            }
                        }

                        // Subtle divider between controls and apps
                        if (hasControls && hasApps) {
                            Box(
                                modifier = Modifier
                                    .width(32.dp)
                                    .height(1.dp)
                                    .background(Color.White.copy(alpha = 0.15f))
                            )
                        }

                        // Apps list: standard responsive tap launch with subtle bounce (No split-screen)
                        apps.forEach { app ->
                            DroidbarAppItem(
                                app = app,
                                onTap = {
                                    val launchIntent = context.packageManager.getLaunchIntentForPackage(app.packageName)?.apply {
                                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                    }
                                    if (launchIntent != null) {
                                        runCatching { context.startActivity(launchIntent) }
                                    }
                                    isExpanded = false
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DroidbarAppItem(
    app: BarAppInfo,
    onTap: () -> Unit
) {
    val (bounceScale, triggerBounce) = rememberBounceScale()
    val view = LocalView.current

    Surface(
        modifier = Modifier
            .size(44.dp)
            .graphicsLayer {
                scaleX = bounceScale
                scaleY = bounceScale
            },
        shape = RoundedCornerShape(14.dp),
        color = Color.Transparent,
        onClick = {
            triggerBounce()
            view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
            onTap()
        }
    ) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            if (app.iconBitmap != null) {
                Image(
                    bitmap = app.iconBitmap.asImageBitmap(),
                    contentDescription = app.label,
                    modifier = Modifier.size(38.dp)
                )
            } else {
                Icon(
                    imageVector = Icons.Default.Apps,
                    contentDescription = app.label,
                    modifier = Modifier.size(24.dp),
                    tint = Color.White.copy(alpha = 0.7f)
                )
            }
        }
    }
}

@Composable
fun rememberBounceScale(): Pair<Float, () -> Unit> {
    val scale = remember { Animatable(1f) }
    val coroutineScope = rememberCoroutineScope()
    val trigger: () -> Unit = {
        coroutineScope.launch {
            scale.animateTo(0.85f, tween(70))
            scale.animateTo(1f, spring(dampingRatio = 0.52f, stiffness = 500f))
        }
    }
    return Pair(scale.value, trigger)
}

private fun toggleFlashlight(context: Context) {
    val cm = context.getSystemService(Context.CAMERA_SERVICE) as? CameraManager ?: return
    try {
        val id = cm.cameraIdList.firstOrNull { id ->
            cm.getCameraCharacteristics(id).get(CameraCharacteristics.FLASH_INFO_AVAILABLE) == true
        } ?: return
        val currentOn = IslandStateManager.isFlashlightOn.value
        cm.setTorchMode(id, !currentOn)
    } catch (_: Exception) {}
}

/**
 * Toggles ONLY between Normal (Ring) and Vibrate modes. Never enters Silent mode.
 */
private fun toggleRingVibrateMode(context: Context, onModeChanged: (Int) -> Unit) {
    val am = context.getSystemService(Context.AUDIO_SERVICE) as? AudioManager ?: return
    val current = am.ringerMode
    val next = if (current == AudioManager.RINGER_MODE_VIBRATE) {
        AudioManager.RINGER_MODE_NORMAL
    } else {
        AudioManager.RINGER_MODE_VIBRATE
    }
    runCatching {
        am.ringerMode = next
    }
    onModeChanged(am.ringerMode)
}

/**
 * Toggles or opens Wi-Fi settings/panel.
 */
private fun toggleOrOpenWifi(context: Context) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        runCatching {
            val panelIntent = Intent(Settings.Panel.ACTION_WIFI).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(panelIntent)
            return
        }
    }
    runCatching {
        val wm = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as? WifiManager
        if (wm != null) {
            @Suppress("DEPRECATION")
            wm.isWifiEnabled = !wm.isWifiEnabled
        }
    }.onFailure {
        runCatching {
            val intent = Intent(Settings.ACTION_WIFI_SETTINGS).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        }
    }
}

/**
 * Opens Mobile data / Internet settings panel.
 */
private fun openMobileDataPanel(context: Context) {
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
