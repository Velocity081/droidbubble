package com.droidbubble.app.droidbar

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.media.AudioManager
import android.view.HapticFeedbackConstants
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.FlashlightOff
import androidx.compose.material.icons.filled.FlashlightOn
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.VerticalSplit
import androidx.compose.material.icons.filled.Vibration
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
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.coerceAtLeast
import androidx.compose.ui.unit.dp
import com.droidbubble.app.island.IslandStateManager
import com.droidbubble.app.island.M3CookieShape
import com.droidbubble.app.model.IslandSettings
import com.droidbubble.app.service.IslandAccessibilityService
import kotlinx.coroutines.CoroutineScope
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
    val coroutineScope = rememberCoroutineScope()
    val animProgress = remember { Animatable(0f) }
    var isExpanded by remember { mutableStateOf(false) }
    var isWindowExpanded by remember { mutableStateOf(false) }
    var apps by remember { mutableStateOf<List<BarAppInfo>>(emptyList()) }

    // Split-screen selection state
    var selectedSplitFirstApp by remember { mutableStateOf<BarAppInfo?>(null) }

    val isFlashlightOn by IslandStateManager.isFlashlightOn.collectAsState()

    var currentRingerMode by remember {
        val am = context.getSystemService(Context.AUDIO_SERVICE) as? AudioManager
        mutableIntStateOf(am?.ringerMode ?: AudioManager.RINGER_MODE_NORMAL)
    }

    DisposableEffect(context) {
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(c: Context?, intent: Intent?) {
                val am = context.getSystemService(Context.AUDIO_SERVICE) as? AudioManager
                currentRingerMode = am?.ringerMode ?: AudioManager.RINGER_MODE_NORMAL
            }
        }
        val filter = IntentFilter(AudioManager.RINGER_MODE_CHANGED_ACTION).apply {
            addAction("android.media.INTERNAL_RINGER_MODE_CHANGED_ACTION")
        }
        context.registerReceiver(receiver, filter)
        onDispose {
            runCatching { context.unregisterReceiver(receiver) }
        }
    }

    // Initial load of apps
    LaunchedEffect(Unit) {
        val loadedApps = withContext(Dispatchers.IO) {
            MostUsedAppsHelper.getMostUsedApps(context, limit = 6)
        }
        apps = loadedApps
    }

    // Animate expand and collapse smoothly with single Animatable
    LaunchedEffect(isExpanded) {
        if (isExpanded) {
            selectedSplitFirstApp = null
            isWindowExpanded = true
            onExpandChanged(true)
            val loadedApps = withContext(Dispatchers.IO) {
                MostUsedAppsHelper.getMostUsedApps(context, limit = 6)
            }
            apps = loadedApps
            animProgress.animateTo(
                targetValue = 1f,
                animationSpec = spring(
                    dampingRatio = 0.68f,
                    stiffness = 380f
                )
            )
        } else {
            selectedSplitFirstApp = null
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

    val controlCount = (if (settings.droidbarShowFlashlight) 1 else 0) + (if (settings.droidbarShowRingMode) 1 else 0)
    val hasControls = controlCount > 0
    val appCount = apps.size
    val hasApps = appCount > 0

    val targetExpandedHeight = when {
        hasControls && hasApps -> (controlCount * 50 + 12 + appCount * 50 + 24).dp
        hasControls -> (controlCount * 52 + 24).dp
        hasApps -> (appCount * 52 + 24).dp
        else -> 120.dp
    }.coerceAtLeast(80.dp)

    val progress = animProgress.value.coerceIn(0f, 1.25f)

    val barWidth = (5.dp + (68.dp - 5.dp) * progress).coerceAtLeast(5.dp)
    val barHeight = (44.dp + (targetExpandedHeight - 44.dp) * progress).coerceAtLeast(44.dp)
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
                            selectedSplitFirstApp = null
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
                            .padding(vertical = 12.dp, horizontal = 10.dp),
                        verticalArrangement = Arrangement.SpaceEvenly,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Flashlight toggle control (12-sided M3 Cookie Shape, subtle tonal palette, subtle bounce)
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
                                shape = M3CookieShape, // 12-sided cookie shape
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

                        // Ring mode toggle control: ONLY Ring and Vibrate (12-sided M3 Cookie Shape, subtle bounce)
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
                                shape = M3CookieShape, // 12-sided cookie shape
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

                        // Subtle divider between controls and apps
                        if (hasControls && hasApps) {
                            Box(
                                modifier = Modifier
                                    .width(32.dp)
                                    .height(1.dp)
                                    .background(Color.White.copy(alpha = 0.15f))
                            )
                        }

                        // Apps list with 1-second long-press split screen selection and subtle bounce
                        apps.forEach { app ->
                            val isFirstSelected = selectedSplitFirstApp?.packageName == app.packageName
                            val isSplitActive = selectedSplitFirstApp != null

                            DroidbarAppItem(
                                app = app,
                                isFirstSplitSelected = isFirstSelected,
                                isSplitModeActive = isSplitActive,
                                onTap = {
                                    if (selectedSplitFirstApp == null) {
                                        // Regular app launch
                                        val launchIntent = context.packageManager.getLaunchIntentForPackage(app.packageName)?.apply {
                                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                        }
                                        if (launchIntent != null) {
                                            runCatching { context.startActivity(launchIntent) }
                                        }
                                        isExpanded = false
                                    } else if (selectedSplitFirstApp?.packageName != app.packageName) {
                                        // Second app selected for split screen!
                                        val firstApp = selectedSplitFirstApp!!
                                        selectedSplitFirstApp = null
                                        launchSplitScreen(context, firstApp, app)
                                        isExpanded = false
                                    } else {
                                        // Tapped same app again -> cancel selection
                                        selectedSplitFirstApp = null
                                    }
                                },
                                onLongPress1s = {
                                    selectedSplitFirstApp = app
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
    isFirstSplitSelected: Boolean,
    isSplitModeActive: Boolean,
    onTap: () -> Unit,
    onLongPress1s: () -> Unit
) {
    val scale = remember { Animatable(1f) }
    val coroutineScope = rememberCoroutineScope()
    val view = LocalView.current

    val triggerBounce: () -> Unit = {
        coroutineScope.launch {
            scale.animateTo(0.85f, tween(70))
            scale.animateTo(1f, spring(dampingRatio = 0.52f, stiffness = 500f))
        }
    }

    Box(
        modifier = Modifier
            .size(44.dp)
            .graphicsLayer {
                scaleX = scale.value
                scaleY = scale.value
            }
            .then(
                if (isFirstSplitSelected) {
                    Modifier
                        .border(
                            width = 2.5.dp,
                            color = MaterialTheme.colorScheme.primary,
                            shape = RoundedCornerShape(14.dp)
                        )
                        .background(
                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f),
                            shape = RoundedCornerShape(14.dp)
                        )
                } else if (isSplitModeActive) {
                    Modifier
                        .border(
                            width = 1.dp,
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.35f),
                            shape = RoundedCornerShape(14.dp)
                        )
                } else Modifier
            )
            .clip(RoundedCornerShape(14.dp))
            .pointerInput(app.packageName, isSplitModeActive, isFirstSplitSelected) {
                awaitEachGesture {
                    val down = awaitFirstDown(requireUnconsumed = false)
                    var isLongPress = false
                    val timerJob = coroutineScope.launch {
                        delay(1000L) // 1 second hold for split-screen selection
                        isLongPress = true
                        view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
                        triggerBounce()
                        onLongPress1s()
                    }
                    val upOrCancel = waitForUpOrCancellation()
                    timerJob.cancel()
                    if (upOrCancel != null && !isLongPress) {
                        triggerBounce()
                        view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                        onTap()
                    }
                }
            },
        contentAlignment = Alignment.Center
    ) {
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

        if (isFirstSplitSelected) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .size(16.dp)
                    .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.VerticalSplit,
                    contentDescription = "Split 1",
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(11.dp)
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
 * Launches App A and App B together in split-screen mode.
 */
private fun launchSplitScreen(context: Context, appA: BarAppInfo, appB: BarAppInfo) {
    val pm = context.packageManager
    val intentA = pm.getLaunchIntentForPackage(appA.packageName)?.apply {
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    } ?: return

    val intentB = pm.getLaunchIntentForPackage(appB.packageName)?.apply {
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_LAUNCH_ADJACENT)
    } ?: return

    val accessibilityService = IslandAccessibilityService.instance
    if (accessibilityService != null) {
        runCatching { context.startActivity(intentA) }
        CoroutineScope(Dispatchers.Main).launch {
            delay(350L)
            IslandAccessibilityService.toggleSplitScreen()
            delay(400L)
            runCatching { context.startActivity(intentB) }
        }
    } else {
        runCatching { context.startActivity(intentA) }
        CoroutineScope(Dispatchers.Main).launch {
            delay(350L)
            runCatching { context.startActivity(intentB) }
        }
    }
}
