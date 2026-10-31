package com.droidbubble.app.island

import android.app.ActivityOptions
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.media.AudioManager
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.util.Log
import android.view.HapticFeedbackConstants
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CallEnd
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.FastRewind
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.FlashlightOff
import androidx.compose.material.icons.filled.FlashlightOn
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsOff
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Radio
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.filled.Air
import androidx.compose.material.icons.filled.AcUnit
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SignalCellularAlt
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.KeyboardArrowUp
import com.droidbubble.app.model.FpsInfo
import com.droidbubble.app.model.HourlyWeather
import com.droidbubble.app.model.WeatherInfo
import com.droidbubble.app.model.NetworkSpeedInfo
import com.droidbubble.app.island.IslandStateManager.RingerMode
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.BlurredEdgeTreatment
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import com.droidbubble.app.service.IslandAccessibilityService
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.droidbubble.app.model.ActiveCallState
import com.droidbubble.app.model.IslandPriority
import com.droidbubble.app.model.IslandSettings
import com.droidbubble.app.model.MediaInfo
import com.droidbubble.app.model.NotificationInfo
import com.droidbubble.app.model.ScreenRecordingState
import java.util.Locale
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.roundToInt
import kotlin.math.sin


private val blurredBitmapCache = java.util.WeakHashMap<Bitmap, Bitmap>()

fun createBlurredBitmap(src: Bitmap, blurRadius: Int = 14, scaleFraction: Float = 0.20f): Bitmap? {
    if (src.isRecycled) return null
    synchronized(blurredBitmapCache) {
        blurredBitmapCache[src]?.let { if (!it.isRecycled) return it }
    }
    return try {
        val w = (src.width * scaleFraction).toInt().coerceAtLeast(16)
        val h = (src.height * scaleFraction).toInt().coerceAtLeast(16)
        val scaled = Bitmap.createScaledBitmap(src, w, h, true)
        val mutable = scaled.copy(Bitmap.Config.ARGB_8888, true)
        if (scaled != src && scaled != mutable && !scaled.isRecycled) {
            scaled.recycle()
        }
        fastBoxBlur(mutable, blurRadius.coerceIn(3, 25))
        synchronized(blurredBitmapCache) {
            blurredBitmapCache[src] = mutable
        }
        mutable
    } catch (_: Throwable) {
        null
    }
}

private fun fastBoxBlur(bitmap: Bitmap, radius: Int) {
    val width = bitmap.width
    val height = bitmap.height
    val pixels = IntArray(width * height)
    bitmap.getPixels(pixels, 0, width, 0, 0, width, height)

    val wm = width - 1
    val hm = height - 1
    val wh = width * height
    val div = radius + radius + 1

    val r = IntArray(wh)
    val g = IntArray(wh)
    val b = IntArray(wh)
    val vmin = IntArray(maxOf(width, height))

    var yw = 0
    var yi = 0

    for (y in 0 until height) {
        var rsum = 0; var gsum = 0; var bsum = 0
        for (i in -radius..radius) {
            val p = pixels[yi + minOf(wm, maxOf(i, 0))]
            rsum += (p shr 16) and 0xff
            gsum += (p shr 8) and 0xff
            bsum += p and 0xff
        }
        for (x in 0 until width) {
            r[yi] = rsum / div
            g[yi] = gsum / div
            b[yi] = bsum / div

            if (y == 0) {
                vmin[x] = minOf(x + radius + 1, wm)
            }
            val p1 = pixels[yw + vmin[x]]
            val p2 = pixels[yw + maxOf(x - radius, 0)]

            rsum += ((p1 shr 16) and 0xff) - ((p2 shr 16) and 0xff)
            gsum += ((p1 shr 8) and 0xff) - ((p2 shr 8) and 0xff)
            bsum += (p1 and 0xff) - (p2 and 0xff)
            yi++
        }
        yw += width
    }

    for (x in 0 until width) {
        var rsum = 0; var gsum = 0; var bsum = 0
        for (i in -radius..radius) {
            yi = maxOf(0, minOf(hm, i)) * width + x
            rsum += r[yi]
            gsum += g[yi]
            bsum += b[yi]
        }
        yi = x
        for (y in 0 until height) {
            pixels[yi] = (0xff shl 24) or ((rsum / div) shl 16) or ((gsum / div) shl 8) or (bsum / div)
            if (x == 0) {
                vmin[y] = minOf(y + radius + 1, hm) * width
            }
            val p1 = x + vmin[y]
            val p2 = x + maxOf(y - radius, 0) * width

            rsum += r[p1] - r[p2]
            gsum += g[p1] - g[p2]
            bsum += b[p1] - b[p2]

            yi += width
        }
    }

    bitmap.setPixels(pixels, 0, width, 0, 0, width, height)
}

private val accentColorCache = java.util.WeakHashMap<Bitmap, Color>()

fun extractMediaAccentColor(bitmap: Bitmap?): Color {
    if (bitmap == null) return Color.Transparent
    synchronized(accentColorCache) {
        accentColorCache[bitmap]?.let { return it }
    }
    return try {
        // Sample with a small 16x16 scaled bitmap for ultra-fast, smooth calculation without JNI pixel bottlenecks
        val scaled = if (bitmap.width > 16 || bitmap.height > 16) {
            Bitmap.createScaledBitmap(bitmap, 16, 16, false)
        } else {
            bitmap
        }
        val pixels = IntArray(scaled.width * scaled.height)
        scaled.getPixels(pixels, 0, scaled.width, 0, 0, scaled.width, scaled.height)
        if (scaled != bitmap && !scaled.isRecycled) {
            scaled.recycle()
        }

        var redSum = 0L
        var greenSum = 0L
        var blueSum = 0L
        var count = 0L

        for (pixel in pixels) {
            val a = (pixel shr 24) and 0xff
            if (a > 60) {
                redSum += (pixel shr 16) and 0xff
                greenSum += (pixel shr 8) and 0xff
                blueSum += pixel and 0xff
                count++
            }
        }

        val color = if (count > 0L) {
            val avgR = (redSum / count).toInt().coerceIn(0, 255)
            val avgG = (greenSum / count).toInt().coerceIn(0, 255)
            val avgB = (blueSum / count).toInt().coerceIn(0, 255)
            Color(avgR, avgG, avgB)
        } else {
            Color.Transparent
        }

        synchronized(accentColorCache) {
            accentColorCache[bitmap] = color
        }
        color
    } catch (_: Exception) {
        Color.Transparent
    }
}

private val LocalIslandHapticsEnabled = androidx.compose.runtime.compositionLocalOf { true }

@Composable
private fun rememberBounceButtonState(
    pressedScale: Float = 0.92f
): Pair<MutableInteractionSource, Modifier> {
    val interactionSource = remember { MutableInteractionSource() }
    val view = LocalView.current
    val hapticsEnabled = LocalIslandHapticsEnabled.current
    val scale = remember { Animatable(1f) }

    LaunchedEffect(interactionSource, hapticsEnabled) {
        interactionSource.interactions.collect { interaction ->
            when (interaction) {
                is androidx.compose.foundation.interaction.PressInteraction.Press -> {
                    if (hapticsEnabled) {
                        view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
                    }
                    launch {
                        scale.animateTo(
                            targetValue = pressedScale,
                            animationSpec = tween(durationMillis = 65, easing = FastOutSlowInEasing)
                        )
                    }
                }
                is androidx.compose.foundation.interaction.PressInteraction.Release -> {
                    launch {
                        scale.animateTo(
                            targetValue = 1f,
                            animationSpec = spring(dampingRatio = 0.48f, stiffness = 520f)
                        )
                    }
                }
                is androidx.compose.foundation.interaction.PressInteraction.Cancel -> {
                    launch {
                        scale.animateTo(
                            targetValue = 1f,
                            animationSpec = spring(dampingRatio = 0.65f, stiffness = 480f)
                        )
                    }
                }
            }
        }
    }

    return interactionSource to Modifier.graphicsLayer {
        scaleX = scale.value
        scaleY = scale.value
    }
}

@Composable
private fun BouncyFilledTonalButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    shape: Shape = ButtonDefaults.filledTonalShape,
    colors: androidx.compose.material3.ButtonColors = ButtonDefaults.filledTonalButtonColors(),
    contentPadding: PaddingValues = ButtonDefaults.ContentPadding,
    content: @Composable androidx.compose.foundation.layout.RowScope.() -> Unit
) {
    val (interactionSource, bounceModifier) = rememberBounceButtonState()
    FilledTonalButton(
        onClick = onClick,
        modifier = modifier.then(bounceModifier),
        enabled = enabled,
        shape = shape,
        colors = colors,
        contentPadding = contentPadding,
        interactionSource = interactionSource,
        content = content
    )
}

@Composable
private fun BouncyIconButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    content: @Composable () -> Unit
) {
    val (interactionSource, bounceModifier) = rememberBounceButtonState()
    IconButton(
        onClick = onClick,
        modifier = modifier.then(bounceModifier),
        enabled = enabled,
        interactionSource = interactionSource,
        content = content
    )
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun IslandOverlayRoot(
    settings: IslandSettings,
    onBoundsChange: (widthPx: Int, heightPx: Int) -> Unit = { _, _ -> },
    onCompactRectsChange: (rects: List<android.graphics.Rect>) -> Unit = {}
) {
    androidx.compose.runtime.CompositionLocalProvider(LocalIslandHapticsEnabled provides settings.hapticsEnabled) {
    val isExpanded by IslandStateManager.isExpanded.collectAsState()
    val isPopIn by IslandStateManager.isPopInActive.collectAsState()
    val media by IslandStateManager.activeMedia.collectAsState()
    val notification by IslandStateManager.activeNotification.collectAsState()
    val progressNotification by IslandStateManager.activeProgress.collectAsState()
    val recording by IslandStateManager.screenRecordingState.collectAsState()
    val activeCall by IslandStateManager.activeCallState.collectAsState()
    val charging by IslandStateManager.activeCharging.collectAsState()
    val isUnlocked by IslandStateManager.isUnlocked.collectAsState()
    val isDeviceLocked by IslandStateManager.isDeviceLocked.collectAsState()
    val isBluetoothConnected by IslandStateManager.isBluetoothConnected.collectAsState()
    val activeRingerMode by IslandStateManager.activeRingerMode.collectAsState()
    val isFlashlightOn by IslandStateManager.isFlashlightOn.collectAsState()
    val flashlightLevel by IslandStateManager.flashlightLevel.collectAsState()
    val maxFlashlightLevel by IslandStateManager.maxFlashlightLevel.collectAsState()
    val isKeyboardFocused by IslandStateManager.isKeyboardFocused.collectAsState()
    val fpsInfo by IslandStateManager.fpsInfo.collectAsState()
    val weatherInfo by IslandStateManager.weatherInfo.collectAsState()
    val networkSpeedInfo by IslandStateManager.networkSpeedInfo.collectAsState()

    val view = LocalView.current
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var lastRingerMode by remember { mutableStateOf(RingerMode.RING) }
    LaunchedEffect(activeRingerMode) {
        if (activeRingerMode != null) {
            lastRingerMode = activeRingerMode!!
        }
    }
    val displayRingerMode = activeRingerMode ?: lastRingerMode

    val idleBounceY = remember { Animatable(0f) }
    val idleBounceScale = remember { Animatable(1f) }

    val configuration = androidx.compose.ui.platform.LocalConfiguration.current
    val isLandscape = configuration.orientation == android.content.res.Configuration.ORIENTATION_LANDSCAPE
    val allowInOrientation = settings.isEnabled && (settings.showInLandscape || !isLandscape) && (!settings.hideOnLockScreen || !isDeviceLocked)

    if (!allowInOrientation || !settings.isEnabled) {
        return@CompositionLocalProvider
    }

    val currentMedia = media
    val currentNotif = notification
    val currentProgress = progressNotification
    val mediaArt = currentMedia?.albumArt ?: currentMedia?.appIcon
    val mediaAccentColor = remember(mediaArt) { extractMediaAccentColor(mediaArt) }
    val blurredMediaArt = remember(mediaArt, settings.blurredAlbumArtBackground) {
        if (settings.blurredAlbumArtBackground && mediaArt != null) {
            createBlurredBitmap(mediaArt)
        } else {
            null
        }
    }
    val isCall = allowInOrientation && activeCall.isActive && settings.showCalls
    val isRecording = allowInOrientation && recording.isRecording && settings.showScreenRecording
    val isProgress = allowInOrientation && currentProgress != null && settings.showLiveProgress
    val isNotification = allowInOrientation && currentNotif != null && settings.showNotifications
    val isMedia = allowInOrientation && currentMedia != null && (!settings.dismissMediaOnPause || currentMedia.isPlaying) && (currentMedia.isPlaying || currentMedia.title.isNotBlank()) && settings.showMedia
    val isFlashlight = allowInOrientation && settings.showFlashlight && isFlashlightOn
    val isCharging = allowInOrientation && settings.showCharging && charging != null
    val isUnlock = allowInOrientation && isUnlocked && !isCharging
    val isBluetooth = allowInOrientation && settings.showBluetooth && isBluetoothConnected && !isCharging && !isUnlock
    val isRingerMode = allowInOrientation && settings.showRingerMode && activeRingerMode != null && !isCharging && !isUnlock && !isBluetooth

    val isFps = allowInOrientation && settings.showFps
    val isWeather = allowInOrientation && settings.showWeather
    val isNetworkSpeed = allowInOrientation && settings.showNetworkSpeed
    val hasPersistent = isFps || isWeather || isNetworkSpeed

    val isOccupied = allowInOrientation && (isCall || isRecording || isNotification || isMedia || isProgress || isCharging || isUnlock || isBluetooth || isFlashlight || isRingerMode || hasPersistent)

    // Unified priority list ordered dynamically by settings.featurePriorities (with persistent views at the bottom by default):
    val orderedActivePriorities = buildList {
        for (featureKey in settings.featurePriorities) {
            when (featureKey) {
                "CALL" -> if (isCall) add(IslandPriority.CALL)
                "MEDIA" -> if (isMedia) add(IslandPriority.MEDIA)
                "PROGRESS" -> if (isProgress) add(IslandPriority.PROGRESS)
                "RECORDING" -> if (isRecording) add(IslandPriority.SCREEN_RECORDING)
                "NOTIFICATION" -> if (isNotification) add(IslandPriority.POP_NOTIFICATION)
                "FLASHLIGHT" -> if (isFlashlight) add(IslandPriority.FLASHLIGHT)
                "FPS" -> if (isFps) add(IslandPriority.FPS)
                "WEATHER" -> if (isWeather) add(IslandPriority.WEATHER)
                "NETWORK_SPEED" -> if (isNetworkSpeed) add(IslandPriority.NETWORK_SPEED)
            }
        }
        if (isCall && IslandPriority.CALL !in this) add(0, IslandPriority.CALL)
        if (isMedia && IslandPriority.MEDIA !in this) add(IslandPriority.MEDIA)
        if (isProgress && IslandPriority.PROGRESS !in this) add(IslandPriority.PROGRESS)
        if (isRecording && IslandPriority.SCREEN_RECORDING !in this) add(IslandPriority.SCREEN_RECORDING)
        if (isNotification && IslandPriority.POP_NOTIFICATION !in this) add(IslandPriority.POP_NOTIFICATION)
        if (isFlashlight && IslandPriority.FLASHLIGHT !in this) add(IslandPriority.FLASHLIGHT)
        if (isFps && IslandPriority.FPS !in this) add(IslandPriority.FPS)
        if (isWeather && IslandPriority.WEATHER !in this) add(IslandPriority.WEATHER)
        if (isNetworkSpeed && IslandPriority.NETWORK_SPEED !in this) add(IslandPriority.NETWORK_SPEED)
    }

    val activeCount = orderedActivePriorities.size
    val isDual = !isCharging && !isUnlock && !isBluetooth && !isRingerMode && activeCount >= 2
    val isTriple = !isCharging && !isUnlock && !isBluetooth && !isRingerMode && activeCount >= 3

    val primaryPriority: IslandPriority = orderedActivePriorities.firstOrNull() ?: IslandPriority.IDLE

    val secondaryPriority: IslandPriority? = when {
        !isDual -> null
        isPopIn && isNotification && primaryPriority != IslandPriority.POP_NOTIFICATION -> IslandPriority.POP_NOTIFICATION
        orderedActivePriorities.size >= 2 -> orderedActivePriorities[1]
        else -> null
    }

    val tertiaryPriority: IslandPriority? = when {
        !isTriple -> null
        orderedActivePriorities.size >= 3 -> orderedActivePriorities.firstOrNull { it != primaryPriority && it != secondaryPriority }
        else -> null
    }

    // Dynamic non-customizable outline colors (around 15% opacity):
    val greenCallOutline = Color(0xFF30D158).copy(alpha = 0.15f)
    val redRecordingOutline = Color(0xFFFF3B30).copy(alpha = 0.15f)
    val defaultTaskOutline = Color.White.copy(alpha = 0.15f)
    val mediaOutline = if (mediaAccentColor != Color.Transparent) {
        mediaAccentColor.copy(alpha = 0.15f)
    } else {
        defaultTaskOutline
    }
    val flashlightOutline = Color.White.copy(alpha = 0.25f)

    // Card 1 (Main Island):
    val primaryOutlineColor = when {
        isCharging -> defaultTaskOutline
        primaryPriority == IslandPriority.CALL -> greenCallOutline
        primaryPriority == IslandPriority.SCREEN_RECORDING -> redRecordingOutline
        primaryPriority == IslandPriority.MEDIA -> mediaOutline
        primaryPriority == IslandPriority.FLASHLIGHT -> flashlightOutline
        else -> defaultTaskOutline
    }

    // Card 2 / Auxiliary Bubble (Secondary Island):
    val secondaryOutlineColor = when {
        secondaryPriority == IslandPriority.CALL -> greenCallOutline
        secondaryPriority == IslandPriority.SCREEN_RECORDING -> redRecordingOutline
        secondaryPriority == IslandPriority.MEDIA -> mediaOutline
        secondaryPriority == IslandPriority.FLASHLIGHT -> flashlightOutline
        else -> defaultTaskOutline
    }

    // Card 3 (Tertiary Expanded Island):
    val tertiaryOutlineColor = when {
        tertiaryPriority == IslandPriority.CALL -> greenCallOutline
        tertiaryPriority == IslandPriority.SCREEN_RECORDING -> redRecordingOutline
        tertiaryPriority == IslandPriority.MEDIA -> mediaOutline
        tertiaryPriority == IslandPriority.FLASHLIGHT -> flashlightOutline
        else -> defaultTaskOutline
    }

    val speed = settings.animationSpeed.coerceIn(0.5f, 2.0f)
    val speedSq = speed * speed

    // Animation Specs (dynamically scaled by settings.animationSpeed):
    val sizeAnimSpecDp = when {
        isCharging || isUnlock || isBluetooth || isRingerMode -> spring<androidx.compose.ui.unit.Dp>(
            dampingRatio = settings.appearDamping,
            stiffness = settings.appearStiffness * speedSq
        )
        isExpanded -> spring<androidx.compose.ui.unit.Dp>(
            dampingRatio = settings.expandDamping,
            stiffness = settings.expandStiffness * speedSq
        )
        else -> tween<androidx.compose.ui.unit.Dp>(
            durationMillis = (190 / speed).roundToInt(),
            easing = CubicBezierEasing(0.28f, 0.0f, 0.20f, 1.0f)
        )
    }

    val sizeAnimSpecFloat = when {
        isCharging || isUnlock || isBluetooth || isRingerMode -> spring<Float>(
            dampingRatio = settings.appearDamping,
            stiffness = settings.appearStiffness * speedSq
        )
        isExpanded -> spring<Float>(
            dampingRatio = settings.expandDamping,
            stiffness = settings.expandStiffness * speedSq
        )
        else -> tween<Float>(
            durationMillis = (190 / speed).roundToInt(),
            easing = CubicBezierEasing(0.28f, 0.0f, 0.20f, 1.0f)
        )
    }

    val positionAnimSpecDp = if (isExpanded) {
        spring<androidx.compose.ui.unit.Dp>(
            dampingRatio = settings.expandDamping,
            stiffness = settings.expandStiffness * speedSq
        )
    } else {
        tween<androidx.compose.ui.unit.Dp>(
            durationMillis = (200 / speed).roundToInt(),
            easing = CubicBezierEasing(0.28f, 0.0f, 0.20f, 1.0f)
        )
    }

    // Dedicated spec for compact pill width:
    // When expanding: crisp spring shrinking into 0 width without overshoot
    // When emerging back: subtle horizontal bounce of a few pixels (dampingRatio = 0.63f)!
    val compactPillWidthAnimSpec = if (isExpanded) {
        spring<androidx.compose.ui.unit.Dp>(
            dampingRatio = 0.82f,
            stiffness = 380f * speedSq
        )
    } else {
        spring<androidx.compose.ui.unit.Dp>(
            dampingRatio = settings.splitDamping,
            stiffness = settings.splitStiffness * speedSq
        )
    }

    val expandProgress by animateFloatAsState(
        targetValue = if (isExpanded && !isCharging && !isUnlock && !isBluetooth && !isRingerMode) 1f else 0f,
        animationSpec = sizeAnimSpecFloat,
        label = "expandProgress"
    )

    val chargingContentAlpha by animateFloatAsState(
        targetValue = if (isCharging) 1f else 0f,
        animationSpec = tween(durationMillis = (130 / speed).roundToInt(), easing = FastOutSlowInEasing),
        label = "chargingContentAlpha"
    )

    val unlockContentAlpha by animateFloatAsState(
        targetValue = if (isUnlock) 1f else 0f,
        animationSpec = tween(durationMillis = (130 / speed).roundToInt(), easing = FastOutSlowInEasing),
        label = "unlockContentAlpha"
    )

    var unlockCheckmarkVisible by remember { mutableStateOf(false) }
    LaunchedEffect(isUnlock) {
        if (isUnlock) {
            unlockCheckmarkVisible = false
            kotlinx.coroutines.delay((110L / speed).toLong())
            unlockCheckmarkVisible = true
        } else {
            unlockCheckmarkVisible = false
        }
    }

    val unlockCheckmarkAlpha by animateFloatAsState(
        targetValue = if (unlockCheckmarkVisible && isUnlock) 1f else 0f,
        animationSpec = tween(durationMillis = (150 / speed).roundToInt(), easing = FastOutSlowInEasing),
        label = "unlockCheckmarkAlpha"
    )

    val unlockCheckmarkScale by animateFloatAsState(
        targetValue = if (unlockCheckmarkVisible && isUnlock) 1f else 0.4f,
        animationSpec = spring(
            dampingRatio = 0.52f, // Gentle bounce: overshoots slightly by ~3-4px, then settles
            stiffness = 320f * speedSq
        ),
        label = "unlockCheckmarkScale"
    )

    val bluetoothContentAlpha by animateFloatAsState(
        targetValue = if (isBluetooth) 1f else 0f,
        animationSpec = tween(durationMillis = (130 / speed).roundToInt(), easing = FastOutSlowInEasing),
        label = "bluetoothContentAlpha"
    )

    var bluetoothCheckmarkVisible by remember { mutableStateOf(false) }
    LaunchedEffect(isBluetooth) {
        if (isBluetooth) {
            bluetoothCheckmarkVisible = false
            kotlinx.coroutines.delay((110L / speed).toLong())
            bluetoothCheckmarkVisible = true
        } else {
            bluetoothCheckmarkVisible = false
        }
    }

    val bluetoothCheckmarkAlpha by animateFloatAsState(
        targetValue = if (bluetoothCheckmarkVisible && isBluetooth) 1f else 0f,
        animationSpec = tween(durationMillis = (150 / speed).roundToInt(), easing = FastOutSlowInEasing),
        label = "bluetoothCheckmarkAlpha"
    )

    val bluetoothCheckmarkScale by animateFloatAsState(
        targetValue = if (bluetoothCheckmarkVisible && isBluetooth) 1f else 0.4f,
        animationSpec = spring(
            dampingRatio = 0.52f, // Gentle bounce: overshoots slightly by ~3-4px, then settles
            stiffness = 320f * speedSq
        ),
        label = "bluetoothCheckmarkScale"
    )

    // Ringer Mode (Silent, Vibrate, Ring) animations:
    val ringerContentAlpha by animateFloatAsState(
        targetValue = if (isRingerMode) 1f else 0f,
        animationSpec = tween(durationMillis = (130 / speed).roundToInt(), easing = FastOutSlowInEasing),
        label = "ringerContentAlpha"
    )

    var ringerSecondaryVisible by remember { mutableStateOf(false) }
    LaunchedEffect(isRingerMode) {
        if (isRingerMode) {
            ringerSecondaryVisible = false
            kotlinx.coroutines.delay((110L / speed).toLong())
            ringerSecondaryVisible = true
        } else {
            ringerSecondaryVisible = false
        }
    }

    val ringerSecondaryAlpha by animateFloatAsState(
        targetValue = if (ringerSecondaryVisible && isRingerMode) 1f else 0f,
        animationSpec = tween(durationMillis = (150 / speed).roundToInt(), easing = FastOutSlowInEasing),
        label = "ringerSecondaryAlpha"
    )

    val ringerSecondaryScale by animateFloatAsState(
        targetValue = if (ringerSecondaryVisible && isRingerMode) 1f else 0.4f,
        animationSpec = spring(
            dampingRatio = 0.52f,
            stiffness = 320f * speedSq
        ),
        label = "ringerSecondaryScale"
    )

    val bellShakeRotation = remember { Animatable(0f) }

    LaunchedEffect(isRingerMode, activeRingerMode) {
        if (isRingerMode && activeRingerMode == RingerMode.RING) {
            launch {
                if (settings.hapticsEnabled) {
                    try {
                        val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                            val vm = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                            vm?.defaultVibrator
                        } else {
                            @Suppress("DEPRECATION")
                            context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
                        }
                        if (vibrator != null && vibrator.hasVibrator()) {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                val timings = longArrayOf(0, 25, 100, 25, 100, 25)
                                val amplitudes = intArrayOf(0, 180, 0, 200, 0, 220)
                                vibrator.vibrate(VibrationEffect.createWaveform(timings, amplitudes, -1))
                            } else {
                                @Suppress("DEPRECATION")
                                vibrator.vibrate(longArrayOf(0, 25, 100, 25, 100, 25), -1)
                            }
                        } else {
                            for (i in 0 until 3) {
                                if (i > 0) delay(100)
                                view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
                            }
                        }
                    } catch (_: Throwable) {
                        view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                    }
                }
            }
            bellShakeRotation.snapTo(0f)
            val animDuration = (1000 / speed).roundToInt()
            val rotSpec = keyframes<Float> {
                durationMillis = animDuration
                0f at 0 using FastOutSlowInEasing
                -24f at (animDuration * 0.06f).roundToInt() using FastOutSlowInEasing
                24f at (animDuration * 0.13f).roundToInt() using FastOutSlowInEasing
                -18f at (animDuration * 0.22f).roundToInt() using FastOutSlowInEasing
                14f at (animDuration * 0.33f).roundToInt() using FastOutSlowInEasing
                -10f at (animDuration * 0.46f).roundToInt() using FastOutSlowInEasing
                6f at (animDuration * 0.60f).roundToInt() using FastOutSlowInEasing
                -3f at (animDuration * 0.75f).roundToInt() using FastOutSlowInEasing
                1f at (animDuration * 0.88f).roundToInt() using FastOutSlowInEasing
                0f at animDuration using FastOutSlowInEasing
            }
            bellShakeRotation.animateTo(0f, animationSpec = rotSpec)
        } else {
            bellShakeRotation.snapTo(0f)
        }
    }

    val vibrateShakeOffset = remember { Animatable(0f) }
    LaunchedEffect(isRingerMode, activeRingerMode) {
        if (isRingerMode && activeRingerMode == RingerMode.VIBRATE) {
            vibrateShakeOffset.snapTo(0f)
            val vibDuration = (600 / speed).roundToInt()
            val vibSpec = keyframes<Float> {
                durationMillis = vibDuration
                0f at 0 using FastOutSlowInEasing
                -3f at (vibDuration * 0.08f).roundToInt() using FastOutSlowInEasing
                3f at (vibDuration * 0.16f).roundToInt() using FastOutSlowInEasing
                -3f at (vibDuration * 0.25f).roundToInt() using FastOutSlowInEasing
                3f at (vibDuration * 0.35f).roundToInt() using FastOutSlowInEasing
                -2f at (vibDuration * 0.48f).roundToInt() using FastOutSlowInEasing
                2f at (vibDuration * 0.62f).roundToInt() using FastOutSlowInEasing
                -1f at (vibDuration * 0.78f).roundToInt() using FastOutSlowInEasing
                0f at vibDuration using FastOutSlowInEasing
            }
            vibrateShakeOffset.animateTo(0f, animationSpec = vibSpec)
        } else {
            vibrateShakeOffset.snapTo(0f)
        }
    }

    val chargingPct = charging?.percentage ?: 0
    val targetChargingFraction = if (isCharging) (chargingPct.toFloat() / 100f).coerceIn(0f, 1f) else 0f
    val animatedChargingProgress by animateFloatAsState(
        targetValue = targetChargingFraction,
        animationSpec = if (isCharging) {
            tween(
                durationMillis = (750 / speed).roundToInt(),
                delayMillis = (60 / speed).roundToInt(),
                easing = FastOutSlowInEasing
            )
        } else {
            tween(
                durationMillis = (150 / speed).roundToInt(),
                easing = FastOutLinearInEasing
            )
        },
        label = "chargingProgress"
    )

    val teardropEarRadius = if (settings.isTeardropCutout) 11.dp else 0.dp
    var mainPillRect by remember { mutableStateOf<android.graphics.Rect?>(null) }
    var auxBubbleRect by remember { mutableStateOf<android.graphics.Rect?>(null) }
    LaunchedEffect(isDual) {
        if (!isDual && auxBubbleRect != null) {
            auxBubbleRect = null
            onCompactRectsChange(listOfNotNull(mainPillRect))
        }
    }
    val chargingIslandWidth = (settings.widthDp + 20).dp.coerceAtLeast(140.dp) + (teardropEarRadius * 2f)
    val unlockIslandWidth = (settings.widthDp * 0.82f).dp.coerceAtLeast(108.dp) + (teardropEarRadius * 2f)
    val ringerIslandWidth = (settings.widthDp * 0.95f).dp.coerceAtLeast(130.dp) + (teardropEarRadius * 2f)

    val bubbleSize = settings.heightDp.dp
    val dualMainShiftLeft = if (!settings.isLeftCutout) (-1).dp else 0.dp
    val compactMainFull = if (settings.isLeftCutout) {
        (settings.widthDp * 0.76f).dp.coerceIn(132.dp, 156.dp)
    } else {
        96.dp
    }
    val compactMainHalf = compactMainFull / 2f

    // Minimised Island Pill: sits docked at settings.topOffsetDp.dp (or flush at 0.dp in Tear-drop mode).
    // When expanding, it animates from its compact width down to 0.dp.
    // When minimising, it animates from 0.dp back to its compact width.
    val targetCompactWidth = when {
        !isOccupied && !settings.alwaysShowPill && !hasPersistent -> 0.dp
        isExpanded -> 0.dp // Shrinks horizontally into 0 width while card expands!
        isCharging -> chargingIslandWidth
        isRingerMode -> ringerIslandWidth
        isUnlock || isBluetooth -> unlockIslandWidth
        isDual -> compactMainFull + (teardropEarRadius * 2f)
        isOccupied || hasPersistent -> settings.widthDp.dp.coerceAtLeast(124.dp) + (teardropEarRadius * 2f)
        else -> settings.widthDp.dp + (teardropEarRadius * 2f)
    }

    // Expanded Cards: Card 1 (and Card 2 if dual mode).
    // When expanded, Card 1 slides down to (settings.topOffsetDp + 52).dp, widening to 340.dp * scale.
    // When minimising, Card 1 shrinks back to 0.dp width (transitioning into the small circle at the punch hole) as it slides back up.
    val screenWidth = configuration.screenWidthDp.dp
    val targetCard1Width = if (isExpanded && !isCharging && !isUnlock && !isBluetooth && !isRingerMode) {
        (340.dp * settings.expandedScale).coerceAtMost(screenWidth - 16.dp)
    } else {
        0.dp
    }

    val notifText = currentNotif?.text ?: ""
    val isLongNotif = notifText.length > 55 || notifText.contains('\n')
    val notifBaseHeight = when {
        isKeyboardFocused -> 188.dp
        isLongNotif -> 168.dp
        else -> 144.dp
    }

    val targetCard1Height = when {
        isExpanded && (primaryPriority == IslandPriority.SCREEN_RECORDING || primaryPriority == IslandPriority.CALL || primaryPriority == IslandPriority.FPS || primaryPriority == IslandPriority.WEATHER || primaryPriority == IslandPriority.NETWORK_SPEED) -> (78.dp * settings.expandedScale).coerceAtLeast(68.dp)
        isExpanded && primaryPriority == IslandPriority.PROGRESS -> (84.dp * settings.expandedScale).coerceAtLeast(74.dp)
        isExpanded && primaryPriority == IslandPriority.POP_NOTIFICATION -> (notifBaseHeight * settings.expandedScale).coerceAtLeast(136.dp)
        isExpanded && primaryPriority == IslandPriority.FLASHLIGHT -> (205.dp * settings.expandedScale).coerceAtLeast(165.dp)
        isExpanded -> (195.dp * settings.expandedScale).coerceAtLeast(155.dp)
        else -> settings.heightDp.dp
    }

    val targetCornerRadius = when {
        isExpanded && !isCharging && !isUnlock && !isBluetooth && !isRingerMode -> 28.dp
        else -> settings.cornerRadiusDp.dp
    }

    // Card 2 & Card 3 staggered delayed appearance: starts a split second after Card 1 when expanding so multiple cards look organic!
    var card2Expanded by remember { mutableStateOf(false) }
    var card3Expanded by remember { mutableStateOf(false) }
    LaunchedEffect(isExpanded) {
        if (isExpanded) {
            kotlinx.coroutines.delay((85L / speed).toLong().coerceAtLeast(35L))
            card2Expanded = true
            kotlinx.coroutines.delay((85L / speed).toLong().coerceAtLeast(35L))
            card3Expanded = true
        } else {
            card3Expanded = false
            card2Expanded = false
        }
    }

    fun getExpandedHeightForPriority(priority: IslandPriority?): Dp {
        val baseH = when (priority) {
            IslandPriority.CALL,
            IslandPriority.SCREEN_RECORDING,
            IslandPriority.FPS,
            IslandPriority.WEATHER,
            IslandPriority.NETWORK_SPEED -> 78.dp
            IslandPriority.PROGRESS -> 84.dp
            IslandPriority.POP_NOTIFICATION -> notifBaseHeight
            IslandPriority.FLASHLIGHT -> 205.dp
            else -> 180.dp
        }
        val minH = when (priority) {
            IslandPriority.CALL,
            IslandPriority.SCREEN_RECORDING,
            IslandPriority.FPS,
            IslandPriority.WEATHER,
            IslandPriority.NETWORK_SPEED -> 68.dp
            IslandPriority.PROGRESS -> 74.dp
            IslandPriority.POP_NOTIFICATION -> 136.dp
            IslandPriority.FLASHLIGHT -> 165.dp
            else -> 150.dp
        }
        return (baseH * settings.expandedScale).coerceAtLeast(minH)
    }

    val showSecondaryCard = !isCharging && !isUnlock && !isBluetooth && !isRingerMode && isDual && secondaryPriority != null
    val targetCard2Height = if (card2Expanded && showSecondaryCard) {
        getExpandedHeightForPriority(secondaryPriority)
    } else 0.dp
    val targetCard2Alpha = if (card2Expanded && showSecondaryCard) 1f else 0f
    val targetCard2OffsetY = if (card2Expanded && showSecondaryCard) 0.dp else (-14).dp

    val showTertiaryCard = !isCharging && !isUnlock && !isBluetooth && !isRingerMode && isTriple && tertiaryPriority != null
    val targetCard3Height = if (card3Expanded && showTertiaryCard) {
        getExpandedHeightForPriority(tertiaryPriority)
    } else 0.dp
    val targetCard3Alpha = if (card3Expanded && showTertiaryCard) 1f else 0f
    val targetCard3OffsetY = if (card3Expanded && showTertiaryCard) 0.dp else (-14).dp

    val targetDualMainShiftX = if (!isCharging && !isUnlock && !isBluetooth && !isRingerMode && isDual && !isExpanded && !settings.isLeftCutout) {
        -dualMainShiftLeft
    } else {
        0.dp
    }
    val animatedDualMainShiftX by animateDpAsState(
        targetValue = targetDualMainShiftX,
        animationSpec = compactPillWidthAnimSpec,
        label = "dualMainShiftX"
    )
    val bubbleGap = 5.dp
    val restingBubbleCenterX = if (settings.isLeftCutout) {
        compactMainFull + bubbleGap + (bubbleSize / 2f)
    } else {
        (compactMainHalf - dualMainShiftLeft) + bubbleGap + (bubbleSize / 2f)
    }

    val shouldEmergeSecondary = !isCharging && !isUnlock && !isBluetooth && !isRingerMode && isDual && !isExpanded && (isOccupied || hasPersistent)
    var secondaryBubbleEmerged by remember { mutableStateOf(false) }
    LaunchedEffect(shouldEmergeSecondary) {
        if (shouldEmergeSecondary) {
            // Subtle delay for a split fraction when the main island appears, so the main island establishes first
            kotlinx.coroutines.delay((60L / speed).toLong().coerceAtLeast(10L))
            secondaryBubbleEmerged = true
        } else {
            secondaryBubbleEmerged = false
        }
    }

    val targetBubbleOffsetX = when {
        secondaryBubbleEmerged && shouldEmergeSecondary -> restingBubbleCenterX
        settings.isLeftCutout -> bubbleSize / 2f
        else -> 0.dp
    }

    val bubbleOffsetAnimSpec = if (secondaryBubbleEmerged && shouldEmergeSecondary) {
        spring<Dp>(
            dampingRatio = settings.splitDamping, // Slight bounce! Gently overshoots by ~3-4dp and bounces back
            stiffness = settings.splitStiffness * speedSq
        )
    } else {
        spring<Dp>(
            dampingRatio = 0.85f, // Smooth retraction back inside the punch hole
            stiffness = 380f * speedSq
        )
    }

    val leftCutoutAnchorCenterX = (-screenWidth / 2f) + 28.dp + settings.horizontalOffsetDp.dp
    val targetHorizontalOffset = if (isExpanded && !isCharging && !isUnlock && !isBluetooth && !isRingerMode) {
        0.dp
    } else if (settings.isLeftCutout) {
        leftCutoutAnchorCenterX
    } else {
        settings.horizontalOffsetDp.dp
    }

    val targetTopOffset = if (isExpanded && !isCharging && !isUnlock && !isBluetooth && !isRingerMode) {
        if (settings.isTeardropCutout) {
            (settings.topOffsetDp + 44).dp
        } else {
            (settings.topOffsetDp + 52).dp // Sits way below initial position (comfortably below status bar) when expanded!
        }
    } else if (settings.isTeardropCutout) {
        0.dp // Docks flush at the top bezel in Tear-drop mode
    } else {
        settings.topOffsetDp.dp // Docks right over the punch hole when minimised or charging/unlock/bluetooth/ringer
    }

    val targetCardSpacing = if (isExpanded && showSecondaryCard) 10.dp else 0.dp

    val animatedCompactWidth by animateDpAsState(targetValue = targetCompactWidth, animationSpec = compactPillWidthAnimSpec, label = "compactWidth")
    val animatedCard1Width by animateDpAsState(targetValue = targetCard1Width, animationSpec = sizeAnimSpecDp, label = "card1Width")
    val animatedCard1Height by animateDpAsState(targetValue = targetCard1Height, animationSpec = sizeAnimSpecDp, label = "card1Height")
    val animatedCornerRadius by animateDpAsState(targetValue = targetCornerRadius, animationSpec = sizeAnimSpecDp, label = "cornerRadius")
    val animatedCard2Height by animateDpAsState(targetValue = targetCard2Height, animationSpec = sizeAnimSpecDp, label = "card2Height")
    val animatedCard2Alpha by animateFloatAsState(targetValue = targetCard2Alpha, animationSpec = sizeAnimSpecFloat, label = "card2Alpha")
    val animatedCard2OffsetY by animateDpAsState(targetValue = targetCard2OffsetY, animationSpec = sizeAnimSpecDp, label = "card2OffsetY")
    val animatedCard3Height by animateDpAsState(targetValue = targetCard3Height, animationSpec = sizeAnimSpecDp, label = "card3Height")
    val animatedCard3Alpha by animateFloatAsState(targetValue = targetCard3Alpha, animationSpec = sizeAnimSpecFloat, label = "card3Alpha")
    val animatedCard3OffsetY by animateDpAsState(targetValue = targetCard3OffsetY, animationSpec = sizeAnimSpecDp, label = "card3OffsetY")
    val animatedBubbleOffsetX by animateDpAsState(targetValue = targetBubbleOffsetX, animationSpec = bubbleOffsetAnimSpec, label = "bubbleOffsetX")
    val animatedHorizontalOffset by animateDpAsState(targetValue = targetHorizontalOffset, animationSpec = positionAnimSpecDp, label = "horizontalOffset")
    val animatedTopOffset by animateDpAsState(targetValue = targetTopOffset, animationSpec = positionAnimSpecDp, label = "topOffset")
    val animatedCardSpacing by animateDpAsState(targetValue = targetCardSpacing, animationSpec = sizeAnimSpecDp, label = "cardSpacing")

    val cardContainerAlpha = if (isExpanded) 1f else ((expandProgress - 0.05f) / 0.95f).coerceIn(0f, 1f)

    var card1OffsetX by remember { mutableFloatStateOf(0f) }
    var card2OffsetX by remember { mutableFloatStateOf(0f) }
    var card3OffsetX by remember { mutableFloatStateOf(0f) }

    val animatedCard1OffsetX by animateFloatAsState(
        targetValue = card1OffsetX,
        animationSpec = spring(dampingRatio = 0.76f, stiffness = 420f * speedSq),
        label = "card1OffsetX"
    )
    val animatedCard2OffsetX by animateFloatAsState(
        targetValue = card2OffsetX,
        animationSpec = spring(dampingRatio = 0.76f, stiffness = 420f * speedSq),
        label = "card2OffsetX"
    )
    val animatedCard3OffsetX by animateFloatAsState(
        targetValue = card3OffsetX,
        animationSpec = spring(dampingRatio = 0.76f, stiffness = 420f * speedSq),
        label = "card3OffsetX"
    )

    LaunchedEffect(isExpanded) {
        if (!isExpanded) {
            card1OffsetX = 0f
            card2OffsetX = 0f
            card3OffsetX = 0f
        }
    }

    // Parentheses overshoot animation: curve shoots out with dynamic spring physics and settles
    val parenthesisExtraShoot = remember { Animatable(0f) }
    var wasExpanded by remember { mutableStateOf(isExpanded) }
    var wasOccupied by remember { mutableStateOf(isOccupied) }

    LaunchedEffect(isExpanded, isOccupied, settings.bubbleEffectEnabled) {
        val justMinimised = wasExpanded && !isExpanded
        val justAppeared = !wasOccupied && isOccupied && !isExpanded
        wasExpanded = isExpanded
        wasOccupied = isOccupied

        if (settings.bubbleEffectEnabled && (justMinimised || justAppeared)) {
            parenthesisExtraShoot.snapTo(0f)
            // Wait for minimised island pill to expand close to punch hole exit (~70ms at 1x speed)
            kotlinx.coroutines.delay((70L / speed).toLong().coerceAtLeast(10L))
            // Shoot outward from the main island with spring bounce and settle back to 0
            parenthesisExtraShoot.animateTo(
                targetValue = 0f,
                initialVelocity = 240f * speed,
                animationSpec = spring(
                    dampingRatio = 0.65f,
                    stiffness = 260f * speedSq
                )
            )
        } else {
            parenthesisExtraShoot.snapTo(0f)
        }
    }

    LaunchedEffect(charging?.timestamp) {
        if (charging != null && settings.hapticsEnabled) {
            view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
        }
    }

    LaunchedEffect(isExpanded, isOccupied, primaryPriority) {
        if (isExpanded) {
            if (!isOccupied || primaryPriority == IslandPriority.IDLE) {
                IslandStateManager.collapse()
            }
        }
    }

    fun triggerHaptic() {
        if (settings.hapticsEnabled) {
            view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
        }
    }

    // Completely idle and collapsed: nothing is visible on screen. Skip rendering the view hierarchy to preserve CPU & battery.
    if (!isOccupied && !settings.alwaysShowPill && animatedCompactWidth <= 0.1.dp && !isExpanded && expandProgress <= 0.001f) {
        return@CompositionLocalProvider
    }

    val screenDensity = LocalDensity.current.density
    // Outer container: full touch bounds when expanded to capture outside tap, or exact bounds when minimised
    Box(
        modifier = Modifier
            .fillMaxSize()
            .then(
                if (!isExpanded && expandProgress <= 0.05f) {
                    Modifier.pointerInput(Unit) {
                        detectVerticalDragGestures { change, dragAmount ->
                            if (dragAmount > 15f) {
                                change.consume()
                                val isRightSide = change.position.x > (screenWidth.value * screenDensity * 0.72f)
                                if (isRightSide) {
                                    if (!IslandAccessibilityService.openQuickSettings()) {
                                        IslandStateManager.expand(primaryPriority)
                                    }
                                } else {
                                    if (!IslandAccessibilityService.openNotificationShade()) {
                                        IslandStateManager.expand(primaryPriority)
                                    }
                                }
                            }
                        }
                    }
                } else Modifier
            ),
        contentAlignment = Alignment.TopCenter
    ) {
        // Tap outside backdrop when expanded (active during whole expand and collapse animation)
        if (expandProgress > 0.05f) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) {
                        triggerHaptic()
                        IslandStateManager.collapse()
                    }
            )
        }



        // Expanded Cards Container: Positioned at animated top and horizontal offset
        if (expandProgress > 0.001f || isExpanded) {
            Box(
                modifier = Modifier
                    .offset {
                        IntOffset(
                            x = animatedHorizontalOffset.roundToPx(),
                            y = animatedTopOffset.roundToPx()
                        )
                    },
                contentAlignment = Alignment.TopCenter
            ) {
                // Cards Stack (Card 1 + Card 2)
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(animatedCardSpacing),
                    modifier = if (isExpanded && primaryPriority != IslandPriority.FLASHLIGHT) {
                        Modifier.pointerInput(Unit) {
                            detectVerticalDragGestures { _, dragAmount ->
                                if (dragAmount < -15f) {
                                    triggerHaptic()
                                    IslandStateManager.collapse()
                                }
                            }
                        }
                    } else Modifier
                ) {
                    // Card 1: Primary Expanded Card
                    if (animatedCard1Width > 1.dp && (expandProgress > 0.001f || isExpanded)) {
                        Box(
                            modifier = Modifier
                                .offset { IntOffset(animatedCard1OffsetX.roundToInt(), 0) }
                                .width(animatedCard1Width)
                                .height(animatedCard1Height)
                                .graphicsLayer {
                                    alpha = cardContainerAlpha
                                    if (settings.isTeardropCutout) {
                                        transformOrigin = TransformOrigin(0.5f, 0f)
                                        val morphScaleY = 0.88f + 0.12f * expandProgress.coerceIn(0f, 1f)
                                        scaleY = morphScaleY
                                    }
                                }
                                .clip(RoundedCornerShape(animatedCornerRadius))
                                .background(if (animatedCard1Width > 2.dp) Color.Black else Color.Transparent)
                                .then(
                                    if (settings.showOutline && animatedCard1Width > 8.dp) {
                                        Modifier.border(
                                            width = settings.outlineThicknessDp.dp,
                                            color = primaryOutlineColor,
                                            shape = RoundedCornerShape(animatedCornerRadius)
                                        )
                                    } else Modifier
                                )
                                .then(
                                    if (isExpanded) {
                                        Modifier.pointerInput(primaryPriority) {
                                            detectHorizontalDragGestures(
                                                onDragEnd = {
                                                    val threshold = 160f
                                                    when (primaryPriority) {
                                                        IslandPriority.MEDIA -> {
                                                            if (abs(card1OffsetX) > threshold) {
                                                                triggerHaptic()
                                                                IslandStateManager.dismissMedia()
                                                                if (activeCount <= 1) {
                                                                    IslandStateManager.collapse()
                                                                }
                                                            }
                                                        }
                                                        IslandPriority.POP_NOTIFICATION -> {
                                                            if (abs(card1OffsetX) > threshold) {
                                                                triggerHaptic()
                                                                val dismissed = IslandStateManager.dismissCurrentNotification()
                                                                if (dismissed && activeCount <= 1) {
                                                                    IslandStateManager.collapse()
                                                                }
                                                            }
                                                        }
                                                        IslandPriority.PROGRESS -> {
                                                            if (abs(card1OffsetX) > threshold) {
                                                                triggerHaptic()
                                                                val dismissed = IslandStateManager.dismissProgress()
                                                                if (dismissed && activeCount <= 1) {
                                                                    IslandStateManager.collapse()
                                                                }
                                                            }
                                                        }
                                                        else -> Unit
                                                    }
                                                    card1OffsetX = 0f
                                                },
                                                onDragCancel = {
                                                    card1OffsetX = 0f
                                                },
                                                onHorizontalDrag = { _, dragAmount ->
                                                    when (primaryPriority) {
                                                        IslandPriority.CALL,
                                                        IslandPriority.SCREEN_RECORDING,
                                                        IslandPriority.FPS,
                                                        IslandPriority.WEATHER,
                                                        IslandPriority.NETWORK_SPEED -> {
                                                            card1OffsetX += dragAmount * 0.18f
                                                        }
                                                        else -> {
                                                            card1OffsetX += dragAmount
                                                        }
                                                    }
                                                }
                                            )
                                        }
                                    } else Modifier
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            // Subtle ambient gradient background or blurred album art for media player or flashlight
                            if (isExpanded) {
                                if (primaryPriority == IslandPriority.MEDIA && settings.blurredAlbumArtBackground && mediaArt != null) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .clip(RoundedCornerShape(animatedCornerRadius))
                                    ) {
                                        Image(
                                            bitmap = (blurredMediaArt ?: mediaArt).asImageBitmap(),
                                            contentDescription = null,
                                            contentScale = ContentScale.Crop,
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .blur(radius = 32.dp, edgeTreatment = BlurredEdgeTreatment.Unbounded)
                                                .graphicsLayer {
                                                    scaleX = 1.35f
                                                    scaleY = 1.35f
                                                    alpha = expandProgress * 0.75f
                                                }
                                        )
                                        Box(
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .background(Color.Black.copy(alpha = 0.38f))
                                        )
                                        // Black gradient coming from the bottom
                                        Box(
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .background(
                                                    Brush.verticalGradient(
                                                        colors = listOf(
                                                            Color.Transparent,
                                                            Color.Transparent,
                                                            Color.Black.copy(alpha = 0.40f),
                                                            Color.Black.copy(alpha = 0.82f),
                                                            Color.Black
                                                        )
                                                    )
                                                )
                                        )
                                    }
                                } else {
                                    val ambientBrush = when {
                                        primaryPriority == IslandPriority.MEDIA && mediaAccentColor != Color.Transparent -> {
                                            Brush.verticalGradient(
                                                colors = listOf(
                                                    mediaAccentColor.copy(alpha = 0.35f * expandProgress),
                                                    mediaAccentColor.copy(alpha = 0.12f * expandProgress),
                                                    Color.Transparent
                                                )
                                            )
                                        }
                                        primaryPriority == IslandPriority.FLASHLIGHT -> {
                                            val blurpleTint = Color(0xFFD2D6FF)
                                            Brush.verticalGradient(
                                                colors = listOf(
                                                    Color.Transparent,
                                                    blurpleTint.copy(alpha = 0.06f * expandProgress),
                                                    blurpleTint.copy(alpha = 0.20f * expandProgress)
                                                )
                                            )
                                        }
                                        primaryPriority == IslandPriority.SCREEN_RECORDING -> {
                                            val redTint = Color(0xFFFF453A)
                                            Brush.verticalGradient(
                                                colors = listOf(
                                                    redTint.copy(alpha = 0.16f * expandProgress),
                                                    redTint.copy(alpha = 0.05f * expandProgress),
                                                    Color.Transparent
                                                )
                                            )
                                        }
                                        primaryPriority == IslandPriority.CALL -> {
                                            val greenTint = Color(0xFF30D158)
                                            Brush.verticalGradient(
                                                colors = listOf(
                                                    greenTint.copy(alpha = 0.16f * expandProgress),
                                                    greenTint.copy(alpha = 0.05f * expandProgress),
                                                    Color.Transparent
                                                )
                                            )
                                        }
                                        primaryPriority == IslandPriority.PROGRESS ||
                                        primaryPriority == IslandPriority.FPS ||
                                        primaryPriority == IslandPriority.WEATHER ||
                                        primaryPriority == IslandPriority.NETWORK_SPEED -> {
                                            Brush.verticalGradient(
                                                colors = listOf(
                                                    Color.White.copy(alpha = 0.12f * expandProgress),
                                                    Color.White.copy(alpha = 0.04f * expandProgress),
                                                    Color.Transparent
                                                )
                                            )
                                        }
                                        else -> null
                                    }
                                    if (ambientBrush != null) {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .background(ambientBrush)
                                        )
                                    }
                                }

                                if (primaryPriority == IslandPriority.MEDIA && settings.mediaWaveAnimation) {
                                    MediaBottomWaveOverlay(
                                        accentColor = if (mediaAccentColor != Color.Transparent) mediaAccentColor else Color(0xFF64D2FF),
                                        isPlaying = currentMedia?.isPlaying == true,
                                        alphaMultiplier = expandProgress,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                }
                            }

                            // Expanded Card 1 Content (e.g. Media Player or Primary Notification)
                            val expandedAlpha = if (isExpanded) {
                                ((expandProgress - 0.30f) / 0.70f).coerceIn(0f, 1f)
                            } else {
                                ((expandProgress - 0.45f) / 0.55f).coerceIn(0f, 1f)
                            }

                            if (!isCharging && expandedAlpha > 0.01f) {
                                val isCompactRowCard1 = primaryPriority == IslandPriority.FLASHLIGHT ||
                                        primaryPriority == IslandPriority.SCREEN_RECORDING ||
                                        primaryPriority == IslandPriority.CALL ||
                                        primaryPriority == IslandPriority.PROGRESS ||
                                        primaryPriority == IslandPriority.FPS ||
                                        primaryPriority == IslandPriority.WEATHER ||
                                        primaryPriority == IslandPriority.NETWORK_SPEED
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(
                                            horizontal = ((if (primaryPriority == IslandPriority.FLASHLIGHT) 16.dp else 20.dp) * settings.expandedScale).coerceIn(14.dp, 24.dp),
                                            vertical = ((if (isCompactRowCard1) 12.dp else if (primaryPriority == IslandPriority.POP_NOTIFICATION) 16.dp else 20.dp) * settings.expandedScale).coerceIn(10.dp, 22.dp)
                                        )
                                        .graphicsLayer {
                                            alpha = expandedAlpha
                                            scaleX = 0.94f + (0.06f * expandedAlpha)
                                            scaleY = 0.94f + (0.06f * expandedAlpha)
                                        }
                                ) {
                                    ExpandedCardContent(
                                        priority = primaryPriority,
                                        media = currentMedia,
                                        notification = if (primaryPriority == IslandPriority.PROGRESS) currentProgress else currentNotif,
                                        recording = recording,
                                        activeCall = activeCall,
                                        flashlightLevel = flashlightLevel,
                                        maxFlashlightLevel = maxFlashlightLevel,
                                        fpsInfo = fpsInfo,
                                        weatherInfo = weatherInfo,
                                        networkSpeedInfo = networkSpeedInfo,
                                        scale = settings.expandedScale,
                                        spinningAlbumArt = settings.spinningAlbumArt,
                                        wavyProgressBar = settings.wavyProgressBar,
                                        onCollapse = {
                                            triggerHaptic()
                                            IslandStateManager.collapse()
                                        }
                                    )
                                }
                            }
                        }
                    }

                    // Card 2: Secondary Action Card (smoothly expands and collapses in dual mode!)
                    if (animatedCard2Height > 1.dp) {
                        Box(
                            modifier = Modifier
                                .offset { IntOffset(animatedCard2OffsetX.roundToInt(), animatedCard2OffsetY.roundToPx()) }
                                .width(animatedCard1Width)
                                .height(animatedCard2Height)
                                .graphicsLayer {
                                    alpha = animatedCard2Alpha
                                    scaleX = 0.92f + (0.08f * animatedCard2Alpha.coerceIn(0f, 1f))
                                    scaleY = 0.92f + (0.08f * animatedCard2Alpha.coerceIn(0f, 1f))
                                }
                                .clip(RoundedCornerShape(animatedCornerRadius))
                                .background(Color.Black)
                                .then(
                                    if (settings.showOutline) {
                                        Modifier.border(
                                            width = settings.outlineThicknessDp.dp,
                                            color = secondaryOutlineColor,
                                            shape = RoundedCornerShape(animatedCornerRadius)
                                        )
                                    } else Modifier
                                )
                                .then(
                                    if (isExpanded && secondaryPriority != null) {
                                        Modifier.pointerInput(secondaryPriority) {
                                            detectHorizontalDragGestures(
                                                onDragEnd = {
                                                    val threshold = 160f
                                                    when (secondaryPriority) {
                                                        IslandPriority.MEDIA -> {
                                                            if (abs(card2OffsetX) > threshold) {
                                                                triggerHaptic()
                                                                IslandStateManager.dismissMedia()
                                                            }
                                                        }
                                                        IslandPriority.POP_NOTIFICATION -> {
                                                            if (abs(card2OffsetX) > threshold) {
                                                                triggerHaptic()
                                                                IslandStateManager.dismissCurrentNotification()
                                                            }
                                                        }
                                                        IslandPriority.PROGRESS -> {
                                                            if (abs(card2OffsetX) > threshold) {
                                                                triggerHaptic()
                                                                IslandStateManager.dismissProgress()
                                                            }
                                                        }
                                                        else -> Unit
                                                    }
                                                    card2OffsetX = 0f
                                                },
                                                onDragCancel = {
                                                    card2OffsetX = 0f
                                                },
                                                onHorizontalDrag = { _, dragAmount ->
                                                    when (secondaryPriority) {
                                                        IslandPriority.CALL,
                                                        IslandPriority.SCREEN_RECORDING,
                                                        IslandPriority.FPS,
                                                        IslandPriority.WEATHER,
                                                        IslandPriority.NETWORK_SPEED -> {
                                                            card2OffsetX += dragAmount * 0.18f
                                                        }
                                                        else -> {
                                                            card2OffsetX += dragAmount
                                                        }
                                                    }
                                                }
                                            )
                                        }
                                    } else Modifier
                                )
                        ) {
                            // Subtle ambient gradient background or blurred album art for secondary card if it's the media player or flashlight
                            if (isExpanded) {
                                if (secondaryPriority == IslandPriority.MEDIA && settings.blurredAlbumArtBackground && mediaArt != null) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .clip(RoundedCornerShape(animatedCornerRadius))
                                    ) {
                                        Image(
                                            bitmap = (blurredMediaArt ?: mediaArt).asImageBitmap(),
                                            contentDescription = null,
                                            contentScale = ContentScale.Crop,
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .blur(radius = 32.dp, edgeTreatment = BlurredEdgeTreatment.Unbounded)
                                                .graphicsLayer {
                                                    scaleX = 1.35f
                                                    scaleY = 1.35f
                                                    alpha = animatedCard2Alpha * 0.75f
                                                }
                                        )
                                        Box(
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .background(Color.Black.copy(alpha = 0.38f))
                                        )
                                        // Black gradient coming from the bottom
                                        Box(
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .background(
                                                    Brush.verticalGradient(
                                                        colors = listOf(
                                                            Color.Transparent,
                                                            Color.Transparent,
                                                            Color.Black.copy(alpha = 0.40f),
                                                            Color.Black.copy(alpha = 0.82f),
                                                            Color.Black
                                                        )
                                                    )
                                                )
                                        )
                                    }
                                } else {
                                    val ambientBrush = when {
                                        secondaryPriority == IslandPriority.MEDIA && mediaAccentColor != Color.Transparent -> {
                                            Brush.verticalGradient(
                                                colors = listOf(
                                                    mediaAccentColor.copy(alpha = 0.35f * animatedCard2Alpha),
                                                    mediaAccentColor.copy(alpha = 0.12f * animatedCard2Alpha),
                                                    Color.Transparent
                                                )
                                            )
                                        }
                                        secondaryPriority == IslandPriority.FLASHLIGHT -> {
                                            val blurpleTint = Color(0xFFD2D6FF)
                                            Brush.verticalGradient(
                                                colors = listOf(
                                                    Color.Transparent,
                                                    blurpleTint.copy(alpha = 0.06f * animatedCard2Alpha),
                                                    blurpleTint.copy(alpha = 0.20f * animatedCard2Alpha)
                                                )
                                            )
                                        }
                                        secondaryPriority == IslandPriority.SCREEN_RECORDING -> {
                                            val redTint = Color(0xFFFF453A)
                                            Brush.verticalGradient(
                                                colors = listOf(
                                                    redTint.copy(alpha = 0.16f * animatedCard2Alpha),
                                                    redTint.copy(alpha = 0.05f * animatedCard2Alpha),
                                                    Color.Transparent
                                                )
                                            )
                                        }
                                        secondaryPriority == IslandPriority.CALL -> {
                                            val greenTint = Color(0xFF30D158)
                                            Brush.verticalGradient(
                                                colors = listOf(
                                                    greenTint.copy(alpha = 0.16f * animatedCard2Alpha),
                                                    greenTint.copy(alpha = 0.05f * animatedCard2Alpha),
                                                    Color.Transparent
                                                )
                                            )
                                        }
                                        secondaryPriority == IslandPriority.PROGRESS ||
                                        secondaryPriority == IslandPriority.FPS ||
                                        secondaryPriority == IslandPriority.WEATHER ||
                                        secondaryPriority == IslandPriority.NETWORK_SPEED -> {
                                            Brush.verticalGradient(
                                                colors = listOf(
                                                    Color.White.copy(alpha = 0.12f * animatedCard2Alpha),
                                                    Color.White.copy(alpha = 0.04f * animatedCard2Alpha),
                                                    Color.Transparent
                                                )
                                            )
                                        }
                                        else -> null
                                    }
                                    if (ambientBrush != null) {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .background(ambientBrush)
                                        )
                                    }
                                }

                                if (secondaryPriority == IslandPriority.MEDIA && settings.mediaWaveAnimation) {
                                    MediaBottomWaveOverlay(
                                        accentColor = if (mediaAccentColor != Color.Transparent) mediaAccentColor else Color(0xFF64D2FF),
                                        isPlaying = currentMedia?.isPlaying == true,
                                        alphaMultiplier = animatedCard2Alpha,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                }
                            }

                            if (animatedCard2Alpha > 0.15f && secondaryPriority != null) {
                                val isCompactRowCard2 = secondaryPriority == IslandPriority.FLASHLIGHT ||
                                        secondaryPriority == IslandPriority.SCREEN_RECORDING ||
                                        secondaryPriority == IslandPriority.CALL ||
                                        secondaryPriority == IslandPriority.PROGRESS ||
                                        secondaryPriority == IslandPriority.FPS ||
                                        secondaryPriority == IslandPriority.WEATHER ||
                                        secondaryPriority == IslandPriority.NETWORK_SPEED
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(
                                            horizontal = ((if (secondaryPriority == IslandPriority.FLASHLIGHT) 16.dp else 20.dp) * settings.expandedScale).coerceIn(14.dp, 24.dp),
                                            vertical = ((if (isCompactRowCard2) 12.dp else if (secondaryPriority == IslandPriority.POP_NOTIFICATION) 16.dp else 20.dp) * settings.expandedScale).coerceIn(10.dp, 22.dp)
                                        )
                                ) {
                                    ExpandedCardContent(
                                        priority = secondaryPriority,
                                        media = currentMedia,
                                        notification = if (secondaryPriority == IslandPriority.PROGRESS) currentProgress else currentNotif,
                                        recording = recording,
                                        activeCall = activeCall,
                                        flashlightLevel = flashlightLevel,
                                        maxFlashlightLevel = maxFlashlightLevel,
                                        fpsInfo = fpsInfo,
                                        weatherInfo = weatherInfo,
                                        networkSpeedInfo = networkSpeedInfo,
                                        scale = settings.expandedScale,
                                        spinningAlbumArt = settings.spinningAlbumArt,
                                        wavyProgressBar = settings.wavyProgressBar,
                                        onCollapse = {
                                            triggerHaptic()
                                            IslandStateManager.collapse()
                                        }
                                    )
                                }
                            }
                        }
                    }

                    // Card 3: Tertiary Action Card (smoothly expands with an extra split-second stagger when 3 activities are active!)
                    if (animatedCard3Height > 1.dp) {
                        Box(
                            modifier = Modifier
                                .offset { IntOffset(animatedCard3OffsetX.roundToInt(), animatedCard3OffsetY.roundToPx()) }
                                .width(animatedCard1Width)
                                .height(animatedCard3Height)
                                .graphicsLayer {
                                    alpha = animatedCard3Alpha
                                    scaleX = 0.92f + (0.08f * animatedCard3Alpha.coerceIn(0f, 1f))
                                    scaleY = 0.92f + (0.08f * animatedCard3Alpha.coerceIn(0f, 1f))
                                }
                                .clip(RoundedCornerShape(animatedCornerRadius))
                                .background(Color.Black)
                                .then(
                                    if (settings.showOutline) {
                                        Modifier.border(
                                            width = settings.outlineThicknessDp.dp,
                                            color = tertiaryOutlineColor,
                                            shape = RoundedCornerShape(animatedCornerRadius)
                                        )
                                    } else Modifier
                                )
                                .then(
                                    if (isExpanded && tertiaryPriority != null) {
                                        Modifier.pointerInput(tertiaryPriority) {
                                            detectHorizontalDragGestures(
                                                onDragEnd = {
                                                    val threshold = 160f
                                                    when (tertiaryPriority) {
                                                        IslandPriority.MEDIA -> {
                                                            if (abs(card3OffsetX) > threshold) {
                                                                triggerHaptic()
                                                                IslandStateManager.dismissMedia()
                                                            }
                                                        }
                                                        IslandPriority.POP_NOTIFICATION -> {
                                                            if (abs(card3OffsetX) > threshold) {
                                                                triggerHaptic()
                                                                IslandStateManager.dismissCurrentNotification()
                                                            }
                                                        }
                                                        IslandPriority.PROGRESS -> {
                                                            if (abs(card3OffsetX) > threshold) {
                                                                triggerHaptic()
                                                                IslandStateManager.dismissProgress()
                                                            }
                                                        }
                                                        else -> Unit
                                                    }
                                                    card3OffsetX = 0f
                                                },
                                                onDragCancel = {
                                                    card3OffsetX = 0f
                                                },
                                                onHorizontalDrag = { _, dragAmount ->
                                                    when (tertiaryPriority) {
                                                        IslandPriority.CALL,
                                                        IslandPriority.SCREEN_RECORDING,
                                                        IslandPriority.FPS,
                                                        IslandPriority.WEATHER,
                                                        IslandPriority.NETWORK_SPEED -> {
                                                            card3OffsetX += dragAmount * 0.18f
                                                        }
                                                        else -> {
                                                            card3OffsetX += dragAmount
                                                        }
                                                    }
                                                }
                                            )
                                        }
                                    } else Modifier
                                )
                        ) {
                            if (isExpanded) {
                                if (tertiaryPriority == IslandPriority.MEDIA && settings.blurredAlbumArtBackground && mediaArt != null) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .clip(RoundedCornerShape(animatedCornerRadius))
                                    ) {
                                        Image(
                                            bitmap = (blurredMediaArt ?: mediaArt).asImageBitmap(),
                                            contentDescription = null,
                                            contentScale = ContentScale.Crop,
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .blur(radius = 32.dp, edgeTreatment = BlurredEdgeTreatment.Unbounded)
                                                .graphicsLayer {
                                                    scaleX = 1.35f
                                                    scaleY = 1.35f
                                                    alpha = animatedCard3Alpha * 0.75f
                                                }
                                        )
                                        Box(
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .background(Color.Black.copy(alpha = 0.38f))
                                        )
                                        Box(
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .background(
                                                    Brush.verticalGradient(
                                                        colors = listOf(
                                                            Color.Transparent,
                                                            Color.Transparent,
                                                            Color.Black.copy(alpha = 0.40f),
                                                            Color.Black.copy(alpha = 0.82f),
                                                            Color.Black
                                                        )
                                                    )
                                                )
                                        )
                                    }
                                } else {
                                    val ambientBrush = when {
                                        tertiaryPriority == IslandPriority.MEDIA && mediaAccentColor != Color.Transparent -> {
                                            Brush.verticalGradient(
                                                colors = listOf(
                                                    mediaAccentColor.copy(alpha = 0.35f * animatedCard3Alpha),
                                                    mediaAccentColor.copy(alpha = 0.12f * animatedCard3Alpha),
                                                    Color.Transparent
                                                )
                                            )
                                        }
                                        tertiaryPriority == IslandPriority.FLASHLIGHT -> {
                                            val blurpleTint = Color(0xFFD2D6FF)
                                            Brush.verticalGradient(
                                                colors = listOf(
                                                    Color.Transparent,
                                                    blurpleTint.copy(alpha = 0.06f * animatedCard3Alpha),
                                                    blurpleTint.copy(alpha = 0.20f * animatedCard3Alpha)
                                                )
                                            )
                                        }
                                        tertiaryPriority == IslandPriority.SCREEN_RECORDING -> {
                                            val redTint = Color(0xFFFF453A)
                                            Brush.verticalGradient(
                                                colors = listOf(
                                                    redTint.copy(alpha = 0.16f * animatedCard3Alpha),
                                                    redTint.copy(alpha = 0.05f * animatedCard3Alpha),
                                                    Color.Transparent
                                                )
                                            )
                                        }
                                        tertiaryPriority == IslandPriority.CALL -> {
                                            val greenTint = Color(0xFF30D158)
                                            Brush.verticalGradient(
                                                colors = listOf(
                                                    greenTint.copy(alpha = 0.16f * animatedCard3Alpha),
                                                    greenTint.copy(alpha = 0.05f * animatedCard3Alpha),
                                                    Color.Transparent
                                                )
                                            )
                                        }
                                        tertiaryPriority == IslandPriority.PROGRESS ||
                                        tertiaryPriority == IslandPriority.FPS ||
                                        tertiaryPriority == IslandPriority.WEATHER ||
                                        tertiaryPriority == IslandPriority.NETWORK_SPEED -> {
                                            Brush.verticalGradient(
                                                colors = listOf(
                                                    Color.White.copy(alpha = 0.12f * animatedCard3Alpha),
                                                    Color.White.copy(alpha = 0.04f * animatedCard3Alpha),
                                                    Color.Transparent
                                                )
                                            )
                                        }
                                        else -> null
                                    }
                                    if (ambientBrush != null) {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .background(ambientBrush)
                                        )
                                    }
                                }

                                if (tertiaryPriority == IslandPriority.MEDIA && settings.mediaWaveAnimation) {
                                    MediaBottomWaveOverlay(
                                        accentColor = if (mediaAccentColor != Color.Transparent) mediaAccentColor else Color(0xFF64D2FF),
                                        isPlaying = currentMedia?.isPlaying == true,
                                        alphaMultiplier = animatedCard3Alpha,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                }
                            }

                            if (animatedCard3Alpha > 0.15f && tertiaryPriority != null) {
                                val isCompactRowCard3 = tertiaryPriority == IslandPriority.FLASHLIGHT ||
                                        tertiaryPriority == IslandPriority.SCREEN_RECORDING ||
                                        tertiaryPriority == IslandPriority.CALL ||
                                        tertiaryPriority == IslandPriority.PROGRESS ||
                                        tertiaryPriority == IslandPriority.FPS ||
                                        tertiaryPriority == IslandPriority.WEATHER ||
                                        tertiaryPriority == IslandPriority.NETWORK_SPEED
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(
                                            horizontal = ((if (tertiaryPriority == IslandPriority.FLASHLIGHT) 16.dp else 20.dp) * settings.expandedScale).coerceIn(14.dp, 24.dp),
                                            vertical = ((if (isCompactRowCard3) 12.dp else if (tertiaryPriority == IslandPriority.POP_NOTIFICATION) 16.dp else 20.dp) * settings.expandedScale).coerceIn(10.dp, 22.dp)
                                        )
                                ) {
                                    ExpandedCardContent(
                                        priority = tertiaryPriority,
                                        media = currentMedia,
                                        notification = if (tertiaryPriority == IslandPriority.PROGRESS) currentProgress else currentNotif,
                                        recording = recording,
                                        activeCall = activeCall,
                                        flashlightLevel = flashlightLevel,
                                        maxFlashlightLevel = maxFlashlightLevel,
                                        fpsInfo = fpsInfo,
                                        weatherInfo = weatherInfo,
                                        networkSpeedInfo = networkSpeedInfo,
                                        scale = settings.expandedScale,
                                        spinningAlbumArt = settings.spinningAlbumArt,
                                        wavyProgressBar = settings.wavyProgressBar,
                                        onCollapse = {
                                            triggerHaptic()
                                            IslandStateManager.collapse()
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Minimised Island Container: Positioned strictly at camera cutout level (settings.topOffsetDp.dp, or 0.dp in Tear-drop mode)
        // Shrinks horizontally to 0 width while the card expands downwards, and blossoms from 0 width when minimising!
        val compactContainerOffsetX = if (settings.isLeftCutout) {
            leftCutoutAnchorCenterX
        } else {
            animatedHorizontalOffset
        }
        val compactContainerOffsetY = if (settings.isTeardropCutout) {
            0.dp
        } else {
            settings.topOffsetDp.dp
        }
        val compactPillHeight = if (settings.isTeardropCutout) {
            settings.heightDp.dp + (settings.topOffsetDp.dp * 0.25f)
        } else {
            settings.heightDp.dp
        }
        Box(
            modifier = Modifier
                .offset {
                    IntOffset(
                        x = compactContainerOffsetX.roundToPx(),
                        y = compactContainerOffsetY.roundToPx()
                    )
                },
            contentAlignment = Alignment.TopCenter
        ) {
            // Smoothly fade in over the first frames where it is barely a circle emerging from the punch hole,
            // and smoothly fade out when shrinking back into the punch hole.
            // Applies to both single and dual activities!
            val circleThreshold = (settings.heightDp.dp * 0.95f).coerceAtLeast(10.dp)
            val emergenceProgress = (animatedCompactWidth / circleThreshold).coerceIn(0f, 1f)
            val compactPillAlpha = emergenceProgress * emergenceProgress * (3f - 2f * emergenceProgress)

            // 1. Auxiliary Circular Island Bubble: Placed on the LAYER BELOW (rendered first so main pill covers it!)
            // Fixed circular shape & size; emerges and retracts purely along the X axis from the punch hole
            if (isDual || animatedBubbleOffsetX > 1.dp) {
                val bubbleCenterRelativeToAnchor = if (settings.isLeftCutout) {
                    animatedBubbleOffsetX - (bubbleSize / 2f)
                } else {
                    animatedBubbleOffsetX
                }
                val bubbleOffsetXPx = with(LocalDensity.current) { bubbleCenterRelativeToAnchor.roundToPx() }
                val bubbleOffsetYPx = with(LocalDensity.current) {
                    if (settings.isTeardropCutout) {
                        ((compactPillHeight - bubbleSize) / 2f).coerceAtLeast(2.dp).roundToPx()
                    } else 0
                }
                val baseBubbleAlpha = if (isExpanded) {
                    ((1f - expandProgress * 4f).coerceIn(0f, 1f))
                } else if (!isOccupied && !hasPersistent) {
                    0f
                } else {
                    1f
                }
                // When emerging from the punch hole, the secondary bubble also fades in with the main island
                // so no opaque circle sits exposed at the punch hole during the initial frames!
                val bubbleAlpha = baseBubbleAlpha * compactPillAlpha

                if (bubbleAlpha > 0.01f) {
                    val mainRightEdgeFromOrigin = if (settings.isLeftCutout) compactMainFull else (compactMainHalf + animatedDualMainShiftX)
                    val emergedDistance = animatedBubbleOffsetX - (mainRightEdgeFromOrigin - bubbleSize / 2f)
                    val contentAlpha = ((emergedDistance - bubbleSize * 0.35f) / (bubbleSize * 0.55f)).coerceIn(0f, 1f) * bubbleAlpha

                    Box(
                        modifier = Modifier
                            .offset { IntOffset(x = bubbleOffsetXPx, y = bubbleOffsetYPx) }
                            .size(bubbleSize)
                            .onGloballyPositioned { coordinates ->
                                if (coordinates.isAttached) {
                                    val b = coordinates.boundsInWindow()
                                    val r = android.graphics.Rect(
                                        b.left.toInt(),
                                        b.top.toInt(),
                                        b.right.toInt(),
                                        b.bottom.toInt()
                                    )
                                    if (auxBubbleRect != r) {
                                        auxBubbleRect = r
                                        onCompactRectsChange(listOfNotNull(mainPillRect, r))
                                    }
                                }
                            }
                            .graphicsLayer { alpha = bubbleAlpha }
                            .clip(CircleShape)
                            .background(Color.Black)
                            .then(
                                if (settings.showOutline && animatedBubbleOffsetX > mainRightEdgeFromOrigin * 0.7f) {
                                    Modifier.border(
                                        width = settings.outlineThicknessDp.dp,
                                        color = secondaryOutlineColor,
                                        shape = CircleShape
                                    )
                                } else Modifier
                            )
                            .combinedClickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null,
                                onClick = {
                                    triggerHaptic()
                                    IslandStateManager.expand(secondaryPriority)
                                },
                                onLongClick = {
                                    triggerHaptic()
                                    openAppForPriority(
                                        context = context,
                                        priority = secondaryPriority,
                                        media = currentMedia,
                                        notification = currentNotif,
                                        progress = currentProgress,
                                        activeCall = activeCall,
                                        onDismissNotification = {
                                            IslandStateManager.dismissCurrentNotification()
                                        }
                                    )
                                }
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        if (contentAlpha > 0.05f) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .graphicsLayer { alpha = contentAlpha },
                                contentAlignment = Alignment.Center
                            ) {
                                AuxiliaryBubbleContent(
                                    priority = secondaryPriority,
                                    media = currentMedia,
                                    notification = if (secondaryPriority == IslandPriority.PROGRESS) currentProgress else currentNotif,
                                    recording = recording,
                                    fpsInfo = fpsInfo,
                                    weatherInfo = weatherInfo,
                                    networkSpeedInfo = networkSpeedInfo,
                                    spinningAlbumArt = settings.spinningAlbumArt
                                )
                            }
                        }
                    }
                }
            }

            // 2. Main Compact Pill: Rendered on top of the bubble (covers it when sliding near x = 0)
            if (animatedCompactWidth > 0.5.dp) {
                val mainPillOffsetXPx = with(LocalDensity.current) {
                    if (settings.isLeftCutout) {
                        ((animatedCompactWidth - settings.heightDp.dp) / 2f).roundToPx()
                    } else {
                        animatedDualMainShiftX.roundToPx()
                    }
                }
                val compactPillShape: Shape = if (settings.isTeardropCutout) {
                    TeardropIslandShape(
                        topEarRadius = teardropEarRadius,
                        bottomCornerRadius = settings.cornerRadiusDp.dp
                    )
                } else {
                    RoundedCornerShape(settings.cornerRadiusDp.dp)
                }
                val teardropScaleY = if (settings.isTeardropCutout) {
                    (0.32f + 0.68f * emergenceProgress) * idleBounceScale.value
                } else {
                    idleBounceScale.value
                }
                Box(
                    modifier = Modifier
                        .offset { IntOffset(x = mainPillOffsetXPx, y = 0) }
                        .width(animatedCompactWidth)
                        .height(compactPillHeight)
                        .onGloballyPositioned { coordinates ->
                            if (coordinates.isAttached) {
                                val b = coordinates.boundsInWindow()
                                val r = android.graphics.Rect(
                                    b.left.toInt(),
                                    b.top.toInt(),
                                    b.right.toInt(),
                                    b.bottom.toInt()
                                )
                                if (mainPillRect != r) {
                                    mainPillRect = r
                                    onCompactRectsChange(listOfNotNull(r, if (isDual) auxBubbleRect else null))
                                }
                            }
                        }
                        .graphicsLayer {
                            alpha = compactPillAlpha
                            if (settings.isTeardropCutout) {
                                transformOrigin = TransformOrigin(0.5f, 0f)
                            }
                            translationY = idleBounceY.value.dp.toPx()
                            scaleX = idleBounceScale.value
                            scaleY = teardropScaleY
                        }
                        .clip(compactPillShape)
                        .background(if (animatedCompactWidth > 2.dp) Color.Black else Color.Transparent)
                        .then(
                            if (settings.showOutline && animatedCompactWidth > 8.dp) {
                                val outlineAlpha = primaryOutlineColor.alpha * compactPillAlpha
                                if (settings.isTeardropCutout) {
                                    Modifier.drawWithContent {
                                        drawContent()
                                        drawTeardropOutline(
                                            topEarRadius = teardropEarRadius,
                                            bottomCornerRadius = settings.cornerRadiusDp.dp,
                                            strokeWidth = settings.outlineThicknessDp.dp,
                                            color = primaryOutlineColor.copy(alpha = outlineAlpha)
                                        )
                                    }
                                } else {
                                    Modifier.border(
                                        width = settings.outlineThicknessDp.dp,
                                        color = primaryOutlineColor.copy(alpha = outlineAlpha),
                                        shape = RoundedCornerShape(settings.cornerRadiusDp.dp)
                                    )
                                }
                            } else Modifier
                        )
                        .combinedClickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = {
                                if (!isExpanded && (isOccupied || hasPersistent) && !isUnlock && !isBluetooth && !isRingerMode) {
                                    triggerHaptic()
                                    IslandStateManager.expand(primaryPriority)
                                } else if (!isExpanded && !isOccupied && !hasPersistent && settings.alwaysShowPill) {
                                    if (settings.hapticsEnabled) {
                                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                                            view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
                                        } else {
                                            view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                                        }
                                    }
                                    coroutineScope.launch {
                                        launch {
                                            idleBounceY.snapTo(0f)
                                            idleBounceY.animateTo(
                                                targetValue = 3.5f,
                                                animationSpec = tween(70, easing = FastOutSlowInEasing)
                                            )
                                            idleBounceY.animateTo(
                                                targetValue = -1.5f,
                                                animationSpec = tween(90, easing = FastOutSlowInEasing)
                                            )
                                            idleBounceY.animateTo(
                                                targetValue = 0f,
                                                animationSpec = spring(
                                                    dampingRatio = Spring.DampingRatioMediumBouncy,
                                                    stiffness = Spring.StiffnessLow
                                                )
                                            )
                                        }
                                        launch {
                                            idleBounceScale.snapTo(1f)
                                            idleBounceScale.animateTo(
                                                targetValue = 0.95f,
                                                animationSpec = tween(70, easing = FastOutSlowInEasing)
                                            )
                                            idleBounceScale.animateTo(
                                                targetValue = 1.04f,
                                                animationSpec = tween(90, easing = FastOutSlowInEasing)
                                            )
                                            idleBounceScale.animateTo(
                                                targetValue = 1.0f,
                                                animationSpec = spring(
                                                    dampingRatio = Spring.DampingRatioMediumBouncy,
                                                    stiffness = Spring.StiffnessLow
                                                )
                                            )
                                        }
                                    }
                                }
                            },
                            onLongClick = {
                                if (!isExpanded && !isUnlock && !isBluetooth && !isRingerMode) {
                                    triggerHaptic()
                                    openAppForPriority(
                                        context = context,
                                        priority = primaryPriority,
                                        media = currentMedia,
                                        notification = currentNotif,
                                        progress = currentProgress,
                                        activeCall = activeCall,
                                        onDismissNotification = {
                                            IslandStateManager.dismissCurrentNotification()
                                        }
                                    )
                                }
                            }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    val compactAlpha = if (isExpanded) {
                        (1f - (expandProgress * 3.0f)).coerceIn(0f, 1f)
                    } else {
                        (1f - (expandProgress * 1.8f)).coerceIn(0f, 1f)
                    }

                    // 1. Compact Content or Charging Content: strictly clear 36dp center over punch-hole
                    if (isCharging || chargingContentAlpha > 0.01f) {
                        val wattage = charging?.wattage
                        val chargingLabel = if (wattage != null && wattage >= 0.5) {
                            "${wattage.roundToInt().coerceAtLeast(1)}W"
                        } else {
                            "Charging"
                        }
                        val mintChargingGreen = Color(0xFF7CE8A0)

                        if (settings.isLeftCutout) {
                            Row(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(start = 38.dp, end = 10.dp)
                                    .graphicsLayer { alpha = chargingContentAlpha },
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End)
                            ) {
                                Text(
                                    text = chargingLabel,
                                    color = Color.White,
                                    fontSize = 13.5.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.weight(1f, fill = false)
                                )
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.End
                                ) {
                                    Text(
                                        text = "${charging?.percentage ?: 100}%",
                                        color = mintChargingGreen,
                                        fontSize = 13.5.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        maxLines = 1,
                                        softWrap = false
                                    )
                                    Spacer(modifier = Modifier.width(3.dp))
                                    Box(
                                        modifier = Modifier.size(22.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(22.dp)
                                                .background(
                                                    Brush.radialGradient(
                                                        colors = listOf(
                                                            mintChargingGreen.copy(alpha = 0.52f),
                                                            mintChargingGreen.copy(alpha = 0.20f),
                                                            Color.Transparent
                                                        )
                                                    ),
                                                    shape = CircleShape
                                                )
                                        )
                                        Icon(
                                            imageVector = Icons.Default.Bolt,
                                            contentDescription = "Charging",
                                            tint = Color(0xFFB8FFD0),
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }
                        } else {
                            Row(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(start = 15.dp + teardropEarRadius, end = 10.dp + teardropEarRadius)
                                    .graphicsLayer { alpha = chargingContentAlpha },
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Left: Rounded Wattage in crisp white (e.g. "90W")
                                Box(
                                    modifier = Modifier.weight(1f),
                                    contentAlignment = Alignment.CenterStart
                                ) {
                                    Text(
                                        text = chargingLabel,
                                        color = Color.White,
                                        fontSize = 13.5.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }

                                // Center: 36dp deadzone strictly over camera cutout
                                Spacer(modifier = Modifier.width(36.dp))

                                // Right: Current battery percentage in mint green + glowing lightning bolt
                                Box(
                                    modifier = Modifier.weight(1f),
                                    contentAlignment = Alignment.CenterEnd
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.End
                                    ) {
                                        Text(
                                            text = "${charging?.percentage ?: 100}%",
                                            color = mintChargingGreen,
                                            fontSize = 13.5.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            maxLines = 1,
                                            softWrap = false
                                        )
                                        Spacer(modifier = Modifier.width(3.dp))
                                        Box(
                                            modifier = Modifier.size(22.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(22.dp)
                                                    .background(
                                                        Brush.radialGradient(
                                                            colors = listOf(
                                                                mintChargingGreen.copy(alpha = 0.52f),
                                                                mintChargingGreen.copy(alpha = 0.20f),
                                                                Color.Transparent
                                                            )
                                                        ),
                                                        shape = CircleShape
                                                    )
                                            )
                                            Icon(
                                                imageVector = Icons.Default.Bolt,
                                                contentDescription = "Charging",
                                                tint = Color(0xFFB8FFD0),
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    } else if (isUnlock || unlockContentAlpha > 0.01f) {
                        if (settings.isLeftCutout) {
                            Row(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(start = 38.dp, end = 12.dp)
                                    .graphicsLayer { alpha = unlockContentAlpha },
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.LockOpen,
                                    contentDescription = "Unlocked",
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                                if (unlockCheckmarkAlpha > 0.01f) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = "Success",
                                        tint = Color.White,
                                        modifier = Modifier
                                            .graphicsLayer {
                                                alpha = unlockCheckmarkAlpha
                                                scaleX = unlockCheckmarkScale
                                                scaleY = unlockCheckmarkScale
                                            }
                                            .size(18.dp)
                                    )
                                }
                            }
                        } else {
                            Row(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(horizontal = 8.dp + teardropEarRadius)
                                    .graphicsLayer { alpha = unlockContentAlpha },
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Left: Unlocked lock icon at the slight edge of the island
                                Box(
                                    modifier = Modifier.weight(1f),
                                    contentAlignment = Alignment.CenterStart
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.LockOpen,
                                        contentDescription = "Unlocked",
                                        tint = Color.White,
                                        modifier = Modifier.size(19.dp)
                                    )
                                }

                                // Center: 36dp deadzone strictly over camera cutout
                                Spacer(modifier = Modifier.width(36.dp))

                                // Right: Checkmark at the slight edge of the island with smooth bounce growth animation
                                Box(
                                    modifier = Modifier.weight(1f),
                                    contentAlignment = Alignment.CenterEnd
                                ) {
                                    if (unlockCheckmarkAlpha > 0.01f) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = "Success",
                                            tint = Color.White,
                                            modifier = Modifier
                                                .graphicsLayer {
                                                    alpha = unlockCheckmarkAlpha
                                                    scaleX = unlockCheckmarkScale
                                                    scaleY = unlockCheckmarkScale
                                                }
                                                .size(19.dp)
                                        )
                                    }
                                }
                            }
                        }
                    } else if (isBluetooth || bluetoothContentAlpha > 0.01f) {
                        if (settings.isLeftCutout) {
                            Row(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(start = 38.dp, end = 12.dp)
                                    .graphicsLayer { alpha = bluetoothContentAlpha },
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Bluetooth,
                                    contentDescription = "Bluetooth",
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                                if (bluetoothCheckmarkAlpha > 0.01f) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = "Connected",
                                        tint = Color.White,
                                        modifier = Modifier
                                            .graphicsLayer {
                                                alpha = bluetoothCheckmarkAlpha
                                                scaleX = bluetoothCheckmarkScale
                                                scaleY = bluetoothCheckmarkScale
                                            }
                                            .size(18.dp)
                                    )
                                }
                            }
                        } else {
                            Row(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(horizontal = 8.dp + teardropEarRadius)
                                    .graphicsLayer { alpha = bluetoothContentAlpha },
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Left: Bluetooth icon at the slight edge of the island
                                Box(
                                    modifier = Modifier.weight(1f),
                                    contentAlignment = Alignment.CenterStart
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Bluetooth,
                                        contentDescription = "Bluetooth",
                                        tint = Color.White,
                                        modifier = Modifier.size(19.dp)
                                    )
                                }

                                // Center: 36dp deadzone strictly over camera cutout
                                Spacer(modifier = Modifier.width(36.dp))

                                // Right: Checkmark at the slight edge of the island with smooth bounce growth animation
                                Box(
                                    modifier = Modifier.weight(1f),
                                    contentAlignment = Alignment.CenterEnd
                                ) {
                                    if (bluetoothCheckmarkAlpha > 0.01f) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = "Connected",
                                            tint = Color.White,
                                            modifier = Modifier
                                                .graphicsLayer {
                                                    alpha = bluetoothCheckmarkAlpha
                                                    scaleX = bluetoothCheckmarkScale
                                                    scaleY = bluetoothCheckmarkScale
                                                }
                                                .size(19.dp)
                                        )
                                    }
                                }
                            }
                        }
                    } else if (isRingerMode || ringerContentAlpha > 0.01f) {
                        val icon = when (displayRingerMode) {
                            RingerMode.SILENT -> Icons.Default.NotificationsOff
                            RingerMode.VIBRATE -> Icons.Default.Vibration
                            RingerMode.RING -> Icons.Default.Notifications
                        }
                        val iconTint = if (displayRingerMode == RingerMode.SILENT) {
                            Color(0xFFFF3B30)
                        } else {
                            Color.White
                        }
                        val labelText = when (displayRingerMode) {
                            RingerMode.SILENT -> "Silent"
                            RingerMode.VIBRATE -> "Vibrate"
                            RingerMode.RING -> "Ring"
                        }
                        val labelColor = if (displayRingerMode == RingerMode.SILENT) {
                            Color(0xFFFF3B30)
                        } else {
                            Color.White
                        }

                        if (settings.isLeftCutout) {
                            Row(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(start = 38.dp, end = 12.dp)
                                    .graphicsLayer { alpha = ringerContentAlpha },
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End)
                            ) {
                                Icon(
                                    imageVector = icon,
                                    contentDescription = displayRingerMode.name,
                                    tint = iconTint,
                                    modifier = Modifier
                                        .graphicsLayer {
                                            if (displayRingerMode == RingerMode.RING) {
                                                transformOrigin = TransformOrigin(0.5f, 0.0f)
                                                rotationZ = bellShakeRotation.value
                                            } else if (displayRingerMode == RingerMode.VIBRATE) {
                                                transformOrigin = TransformOrigin.Center
                                                translationX = vibrateShakeOffset.value.dp.toPx()
                                            } else {
                                                transformOrigin = TransformOrigin.Center
                                            }
                                        }
                                        .size(18.dp)
                                )
                                if (ringerSecondaryAlpha > 0.01f) {
                                    Text(
                                        text = labelText,
                                        color = labelColor,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        maxLines = 1,
                                        modifier = Modifier.graphicsLayer {
                                            alpha = ringerSecondaryAlpha
                                            scaleX = ringerSecondaryScale
                                            scaleY = ringerSecondaryScale
                                        }
                                    )
                                }
                            }
                        } else {
                            Row(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(horizontal = 8.dp + teardropEarRadius)
                                    .graphicsLayer { alpha = ringerContentAlpha },
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Left: Ringer icon (Bell, Slashed Bell, or Vibration)
                                Box(
                                    modifier = Modifier.weight(1f),
                                    contentAlignment = Alignment.CenterStart
                                ) {
                                    Icon(
                                        imageVector = icon,
                                        contentDescription = displayRingerMode.name,
                                        tint = iconTint,
                                        modifier = Modifier
                                            .graphicsLayer {
                                                if (displayRingerMode == RingerMode.RING) {
                                                    transformOrigin = TransformOrigin(0.5f, 0.0f)
                                                    rotationZ = bellShakeRotation.value
                                                } else if (displayRingerMode == RingerMode.VIBRATE) {
                                                    transformOrigin = TransformOrigin.Center
                                                    translationX = vibrateShakeOffset.value.dp.toPx()
                                                } else {
                                                    transformOrigin = TransformOrigin.Center
                                                }
                                            }
                                            .size(19.dp)
                                    )
                                }

                                // Center: 36dp deadzone strictly over camera cutout
                                Spacer(modifier = Modifier.width(36.dp))

                                // Right: Text label (Silent, Vibrate, Ring) with smooth bounce scaling & alpha
                                Box(
                                    modifier = Modifier.weight(1f),
                                    contentAlignment = Alignment.CenterEnd
                                ) {
                                    if (ringerSecondaryAlpha > 0.01f) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.End,
                                            modifier = Modifier.graphicsLayer {
                                                alpha = ringerSecondaryAlpha
                                                scaleX = ringerSecondaryScale
                                                scaleY = ringerSecondaryScale
                                            }
                                        ) {
                                            Text(
                                                text = labelText,
                                                color = labelColor,
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                maxLines = 1
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    } else if (compactAlpha > 0.01f && animatedCompactWidth > 24.dp) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .graphicsLayer { alpha = compactAlpha },
                            contentAlignment = Alignment.Center
                        ) {
                            CompactContent(
                                priority = primaryPriority,
                                media = currentMedia,
                                notification = if (primaryPriority == IslandPriority.PROGRESS) currentProgress else currentNotif,
                                recording = recording,
                                activeCall = activeCall,
                                isCompactDual = isDual,
                                isLeftCutout = settings.isLeftCutout,
                                isTeardropCutout = settings.isTeardropCutout,
                                teardropEarInset = teardropEarRadius,
                                spinningAlbumArt = settings.spinningAlbumArt,
                                flashlightLevel = flashlightLevel,
                                maxFlashlightLevel = maxFlashlightLevel,
                                fpsInfo = fpsInfo,
                                weatherInfo = weatherInfo,
                                networkSpeedInfo = networkSpeedInfo,
                                secondaryPriority = secondaryPriority
                            )
                        }
                    }
                }
            }

            // 3. Parentheses Accents: Neat parentheses-shaped brackets on each side of the island
            // Shorter than the height of the island, smoothly positioning symmetrically around 1 or 2 activities, and neatly close to the edges.
            val targetParenthesisAlpha = if (!isOccupied && !hasPersistent) 0f else ((1f - expandProgress * 3.5f).coerceIn(0f, 1f))
            val parenthesisAlphaAnim by animateFloatAsState(
                targetValue = targetParenthesisAlpha,
                animationSpec = tween(durationMillis = (130 / speed).roundToInt(), easing = FastOutSlowInEasing),
                label = "parenthesisAlpha"
            )

            // Fades in a split second earlier as the island begins emerging from the punch hole
            val parenthesisEmergenceAlpha = (animatedCompactWidth / 14.dp).coerceIn(0f, 1f)
            val effectiveParenthesisAlpha = parenthesisAlphaAnim * parenthesisEmergenceAlpha

            if (settings.bubbleEffectEnabled && effectiveParenthesisAlpha > 0.01f && animatedCompactWidth > 3.dp) {
                val parenthesisHeight = (settings.heightDp.dp * 0.52f).coerceIn(16.dp, 21.dp)
                val extraShootDp = parenthesisExtraShoot.value.coerceAtLeast(0f).dp
                val parenthesisWidth = 5.0.dp + (extraShootDp * 0.20f)
                val parenthesisGap = 3.5.dp
                val parenthesisStroke = 1.8.dp
                val density = LocalDensity.current

                val pYPx = with(density) { ((compactPillHeight - parenthesisHeight) / 2f).roundToPx() }
                val tipOffsetFromCenter = (parenthesisWidth - parenthesisStroke) / 2f

                // Left parenthesis `(`: tips facing right are at centerX + tipOffsetFromCenter.
                // Sits at islandLeft - parenthesisGap - extraShootDp.
                val islandLeftDp = when {
                    settings.isLeftCutout -> -settings.heightDp.dp / 2f
                    settings.isTeardropCutout -> -(animatedCompactWidth / 2f - teardropEarRadius) + animatedDualMainShiftX
                    else -> -animatedCompactWidth / 2f + animatedDualMainShiftX
                }
                val leftCenterXPx = with(density) {
                    (islandLeftDp - parenthesisGap - extraShootDp - tipOffsetFromCenter).roundToPx()
                }

                // Right parenthesis `)`: tips facing left are at centerX - tipOffsetFromCenter.
                // Sits at rightContentEdgeDp + parenthesisGap + extraShootDp.
                // When emerging, curve shoots out slightly further from the island and settles back.
                val mainPillRightEdge = when {
                    settings.isLeftCutout -> animatedCompactWidth - settings.heightDp.dp / 2f
                    settings.isTeardropCutout -> (animatedCompactWidth / 2f - teardropEarRadius) + animatedDualMainShiftX
                    else -> animatedCompactWidth / 2f + animatedDualMainShiftX
                }
                val bubbleRightEdge = if (settings.isLeftCutout) {
                    animatedBubbleOffsetX
                } else {
                    animatedBubbleOffsetX + (bubbleSize / 2f)
                }
                val rightContentEdgeDp = if (!isCharging && !isUnlock && !isBluetooth && !isRingerMode && animatedBubbleOffsetX > 1.dp) {
                    maxOf(mainPillRightEdge, bubbleRightEdge)
                } else {
                    mainPillRightEdge
                }
                val rightCenterXPx = with(density) {
                    (rightContentEdgeDp + parenthesisGap + extraShootDp + tipOffsetFromCenter).roundToPx()
                }

                val bracketColor = Color.Black

                // Left Parenthesis `(`
                ParenthesisBracket(
                    isLeft = true,
                    width = parenthesisWidth,
                    height = parenthesisHeight,
                    strokeWidth = parenthesisStroke,
                    color = bracketColor,
                    modifier = Modifier
                        .offset { IntOffset(x = leftCenterXPx, y = pYPx) }
                        .graphicsLayer { alpha = effectiveParenthesisAlpha }
                )

                // Right Parenthesis `)`
                ParenthesisBracket(
                    isLeft = false,
                    width = parenthesisWidth,
                    height = parenthesisHeight,
                    strokeWidth = parenthesisStroke,
                    color = bracketColor,
                    modifier = Modifier
                        .offset { IntOffset(x = rightCenterXPx, y = pYPx) }
                        .graphicsLayer { alpha = effectiveParenthesisAlpha }
                )
            }
        }
    }
    }
}

fun launchAppOrIntent(
    context: android.content.Context,
    packageName: String?,
    fallbackPendingIntent: PendingIntent? = null
): Boolean {
    // 1. If it is an app, open it directly via its launcher intent with NEW_TASK
    if (!packageName.isNullOrBlank()) {
        try {
            val pm = context.packageManager
            val launchIntent = pm.getLaunchIntentForPackage(packageName)
            if (launchIntent != null) {
                launchIntent.addFlags(
                    Intent.FLAG_ACTIVITY_NEW_TASK or
                    Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED
                )
                context.startActivity(launchIntent)
                return true
            }
        } catch (e: Throwable) {
            Log.e("IslandView", "Failed to launch app by package name: $packageName", e)
        }
    }

    // 2. If it is not a launchable app or launchIntent failed, try the PendingIntent with background activity start allowed
    if (fallbackPendingIntent != null) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                val options = ActivityOptions.makeBasic().apply {
                    pendingIntentBackgroundActivityStartMode = ActivityOptions.MODE_BACKGROUND_ACTIVITY_START_ALLOWED
                }.toBundle()
                fallbackPendingIntent.send(context, 0, null, null, null, null, options)
            } else {
                fallbackPendingIntent.send()
            }
            return true
        } catch (e: Throwable) {
            Log.e("IslandView", "Failed to send pendingIntent", e)
        }
    }

    return false
}

fun openAppForPriority(
    context: android.content.Context,
    priority: IslandPriority?,
    media: MediaInfo?,
    notification: NotificationInfo?,
    progress: NotificationInfo?,
    activeCall: ActiveCallState? = null,
    onDismissNotification: (() -> Unit)? = null
) {
    when (priority) {
        IslandPriority.CALL -> {
            if (activeCall != null) {
                launchAppOrIntent(context, activeCall.packageName, activeCall.contentIntent)
            }
        }
        IslandPriority.POP_NOTIFICATION -> {
            if (notification != null) {
                launchAppOrIntent(context, notification.packageName, notification.contentIntent)
                onDismissNotification?.invoke()
            }
        }
        IslandPriority.PROGRESS -> {
            if (progress != null) {
                launchAppOrIntent(context, progress.packageName, progress.contentIntent)
            }
        }
        IslandPriority.MEDIA -> {
            if (media != null) {
                launchAppOrIntent(context, media.packageName, media.sessionActivity)
            }
        }
        IslandPriority.FPS -> {
            runCatching {
                context.startActivity(Intent(android.provider.Settings.ACTION_DISPLAY_SETTINGS).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                })
            }
        }
        IslandPriority.NETWORK_SPEED -> {
            runCatching {
                context.startActivity(Intent(android.provider.Settings.ACTION_WIFI_SETTINGS).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                })
            }
        }
        else -> {}
    }
}

class TeardropIslandShape(
    private val topEarRadius: Dp,
    private val bottomCornerRadius: Dp
) : Shape {
    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density
    ): Outline {
        val w = size.width
        val h = size.height
        val earPx = with(density) { topEarRadius.toPx() }.coerceIn(0f, (w * 0.24f).coerceAtLeast(0f))
        val innerW = (w - 2f * earPx).coerceAtLeast(0f)
        val bottomR = with(density) { bottomCornerRadius.toPx() }
            .coerceIn(0f, minOf(innerW / 2f, (h - earPx).coerceAtLeast(0f)))

        val path = Path().apply {
            moveTo(0f, 0f)
            // Top-left outward-rounded corner (concave flare from top bezel down into left side)
            quadraticTo(earPx, 0f, earPx, earPx)
            // Left vertical side down to bottom-left corner
            lineTo(earPx, h - bottomR)
            // Bottom-left rounded corner (convex)
            quadraticTo(earPx, h, earPx + bottomR, h)
            // Bottom horizontal edge
            lineTo(w - earPx - bottomR, h)
            // Bottom-right rounded corner (convex)
            quadraticTo(w - earPx, h, w - earPx, h - bottomR)
            // Right vertical side up to top-right ear
            lineTo(w - earPx, earPx)
            // Top-right outward-rounded corner (concave flare back up to top bezel)
            quadraticTo(w - earPx, 0f, w, 0f)
            close()
        }
        return Outline.Generic(path)
    }
}

fun androidx.compose.ui.graphics.drawscope.DrawScope.drawTeardropOutline(
    topEarRadius: Dp,
    bottomCornerRadius: Dp,
    strokeWidth: Dp,
    color: Color
) {
    val w = size.width
    val h = size.height
    val strokePx = strokeWidth.toPx()
    val halfStroke = strokePx / 2f
    val earPx = topEarRadius.toPx().coerceIn(0f, (w * 0.24f).coerceAtLeast(0f))
    val innerW = (w - 2f * earPx).coerceAtLeast(0f)
    val bottomR = bottomCornerRadius.toPx()
        .coerceIn(0f, minOf(innerW / 2f, (h - earPx - halfStroke).coerceAtLeast(0f)))

    val path = Path().apply {
        moveTo(0f, halfStroke)
        quadraticTo(earPx, halfStroke, earPx, earPx)
        lineTo(earPx, h - bottomR - halfStroke)
        quadraticTo(earPx, h - halfStroke, earPx + bottomR, h - halfStroke)
        lineTo(w - earPx - bottomR, h - halfStroke)
        quadraticTo(w - earPx, h - halfStroke, w - earPx, h - bottomR - halfStroke)
        lineTo(w - earPx, earPx)
        quadraticTo(w - earPx, halfStroke, w, halfStroke)
    }
    drawPath(
        path = path,
        color = color,
        style = Stroke(width = strokePx, cap = StrokeCap.Round)
    )
}

@Composable
fun CompactSpinningVinylCover(
    bitmap: Bitmap?,
    isPlaying: Boolean,
    size: Dp,
    modifier: Modifier = Modifier
) {
    val vinylRotation = remember { Animatable(0f) }
    LaunchedEffect(isPlaying) {
        if (isPlaying) {
            while (isActive) {
                val startRot = vinylRotation.value % 360f
                vinylRotation.snapTo(startRot)
                vinylRotation.animateTo(
                    targetValue = startRot + 360f,
                    animationSpec = tween(durationMillis = 9000, easing = LinearEasing)
                )
            }
        }
    }

    Box(
        modifier = modifier
            .size(size)
            .graphicsLayer {
                rotationZ = vinylRotation.value
            }
            .clip(CircleShape)
            .background(Color(0xFF1C1C1E)),
        contentAlignment = Alignment.Center
    ) {
        if (bitmap != null) {
            Image(
                bitmap = bitmap.asImageBitmap(),
                contentDescription = "Vinyl Cover",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            Icon(
                imageVector = Icons.Default.MusicNote,
                contentDescription = null,
                tint = Color(0xFFA1A1AA),
                modifier = Modifier.size(size * 0.52f)
            )
        }

        Canvas(modifier = Modifier.fillMaxSize()) {
            val r = this.size.minDimension / 2f
            drawCircle(
                color = Color.White.copy(alpha = 0.24f),
                radius = r - 0.6.dp.toPx(),
                style = Stroke(width = 0.9.dp.toPx())
            )
            drawCircle(
                color = Color.Black.copy(alpha = 0.30f),
                radius = r * 0.66f,
                style = Stroke(width = 0.75.dp.toPx())
            )
            drawCircle(
                color = Color(0xFF141416).copy(alpha = 0.92f),
                radius = r * 0.24f
            )
            drawCircle(
                color = Color.White.copy(alpha = 0.72f),
                radius = r * 0.09f
            )
        }
    }
}

@Composable
fun ParenthesisBracket(
    isLeft: Boolean,
    width: Dp,
    height: Dp,
    strokeWidth: Dp,
    color: Color,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier.size(width, height)) {
        val w = size.width
        val h = size.height
        val strokePx = strokeWidth.toPx()
        val path = Path()

        if (isLeft) {
            // Left parenthesis `(`: curves outward to the left
            path.moveTo(w - strokePx / 2f, strokePx / 2f)
            path.cubicTo(
                x1 = strokePx / 2f, y1 = h * 0.22f,
                x2 = strokePx / 2f, y2 = h * 0.78f,
                x3 = w - strokePx / 2f, y3 = h - strokePx / 2f
            )
        } else {
            // Right parenthesis `)`: curves outward to the right
            path.moveTo(strokePx / 2f, strokePx / 2f)
            path.cubicTo(
                x1 = w - strokePx / 2f, y1 = h * 0.22f,
                x2 = w - strokePx / 2f, y2 = h * 0.78f,
                x3 = strokePx / 2f, y3 = h - strokePx / 2f
            )
        }

        drawPath(
            path = path,
            color = color,
            style = Stroke(
                width = strokePx,
                cap = StrokeCap.Round
            )
        )
    }
}

@Composable
fun AuxiliaryBubbleContent(
    priority: IslandPriority?,
    media: MediaInfo?,
    notification: NotificationInfo?,
    recording: ScreenRecordingState,
    fpsInfo: FpsInfo = FpsInfo(),
    weatherInfo: WeatherInfo = WeatherInfo(),
    networkSpeedInfo: NetworkSpeedInfo = NetworkSpeedInfo(),
    spinningAlbumArt: Boolean = false
) {
    when (priority) {
        IslandPriority.CALL -> {
            Icon(
                imageVector = Icons.Default.Call,
                contentDescription = "Active Call",
                tint = Color(0xFF30D158),
                modifier = Modifier.size(16.dp)
            )
        }
        IslandPriority.SCREEN_RECORDING -> {
            PulsingRecordingDot()
        }
        IslandPriority.POP_NOTIFICATION -> {
            if (notification?.iconBitmap != null) {
                Image(
                    bitmap = notification.iconBitmap.asImageBitmap(),
                    contentDescription = "Notification Icon",
                    modifier = Modifier
                        .size(18.dp)
                        .clip(CircleShape)
                )
            } else {
                Icon(
                    imageVector = Icons.Default.Notifications,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
        IslandPriority.PROGRESS -> {
            val pct = notification?.progressFraction ?: 0f
            val isIndet = notification?.isIndeterminate == true
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                if (isIndet) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp,
                        color = Color(0xFFE5E5EA),
                        trackColor = Color(0xFF2C2C2E)
                    )
                } else {
                    CircularProgressIndicator(
                        progress = { pct },
                        modifier = Modifier.size(17.dp),
                        strokeWidth = 2.dp,
                        color = Color(0xFFE5E5EA),
                        trackColor = Color(0xFF2C2C2E)
                    )
                    Text(
                        text = "${(pct * 100).roundToInt()}",
                        color = Color.White,
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
        IslandPriority.MEDIA -> {
            val displayArt = media?.albumArt ?: media?.appIcon
            if (spinningAlbumArt) {
                CompactSpinningVinylCover(
                    bitmap = displayArt,
                    isPlaying = media?.isPlaying == true,
                    size = 24.dp
                )
            } else if (displayArt != null) {
                Image(
                    bitmap = displayArt.asImageBitmap(),
                    contentDescription = "Media Art",
                    modifier = Modifier
                        .size(18.dp)
                        .clip(CircleShape)
                )
            } else {
                EqualizerVisualizer(isPlaying = media?.isPlaying == true)
            }
        }
        IslandPriority.FLASHLIGHT -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .background(
                            Brush.radialGradient(
                                colors = listOf(
                                    Color.White.copy(alpha = 0.45f),
                                    Color.White.copy(alpha = 0.15f),
                                    Color.Transparent
                                )
                            ),
                            shape = CircleShape
                        )
                )
                Icon(
                    imageVector = Icons.Default.FlashlightOn,
                    contentDescription = "Flashlight",
                    tint = Color.White,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
        IslandPriority.FPS -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    text = "${fpsInfo.fps}",
                    color = Color(0xFF30D158),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Black
                )
            }
        }
        IslandPriority.WEATHER -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    text = "${weatherInfo.temperatureC}°",
                    color = Color(0xFFFF9F0A),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
        IslandPriority.NETWORK_SPEED -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Default.ArrowDownward,
                    contentDescription = null,
                    tint = Color(0xFF64D2FF),
                    modifier = Modifier.size(14.dp)
                )
            }
        }
        else -> Unit
    }
}

@Composable
fun CompactContent(
    priority: IslandPriority,
    media: MediaInfo?,
    notification: NotificationInfo?,
    recording: ScreenRecordingState,
    activeCall: ActiveCallState = ActiveCallState(),
    isCompactDual: Boolean = false,
    isLeftCutout: Boolean = false,
    isTeardropCutout: Boolean = false,
    teardropEarInset: Dp = 0.dp,
    spinningAlbumArt: Boolean = false,
    flashlightLevel: Int = 1,
    maxFlashlightLevel: Int = 1,
    fpsInfo: FpsInfo = FpsInfo(),
    weatherInfo: WeatherInfo = WeatherInfo(),
    networkSpeedInfo: NetworkSpeedInfo = NetworkSpeedInfo(),
    secondaryPriority: IslandPriority? = null
) {
    val leadingElement = @Composable {
        when (priority) {
            IslandPriority.CALL -> {
                Icon(
                    imageVector = Icons.Default.Call,
                    contentDescription = "Active Call",
                    tint = Color(0xFF30D158),
                    modifier = Modifier.size(17.dp)
                )
            }

            IslandPriority.SCREEN_RECORDING -> {
                PulsingRecordingDot()
            }

            IslandPriority.FLASHLIGHT -> {
                Box(
                    modifier = if (isLeftCutout || isCompactDual) Modifier else Modifier.offset(x = (-4).dp),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(26.dp)
                            .background(
                                Brush.radialGradient(
                                    colors = listOf(
                                        Color.White.copy(alpha = 0.50f),
                                        Color.White.copy(alpha = 0.16f),
                                        Color.Transparent
                                    )
                                ),
                                shape = CircleShape
                            )
                    )
                    Icon(
                        imageVector = Icons.Default.FlashlightOn,
                        contentDescription = "Flashlight",
                        tint = Color.White,
                        modifier = Modifier.size(17.dp)
                    )
                }
            }

            IslandPriority.POP_NOTIFICATION -> {
                if (notification?.iconBitmap != null) {
                    Image(
                        bitmap = notification.iconBitmap.asImageBitmap(),
                        contentDescription = "Notification Icon",
                        modifier = Modifier
                            .size(20.dp)
                            .clip(CircleShape)
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Notifications,
                        contentDescription = null,
                        tint = Color(0xFFE5E5EA),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            IslandPriority.PROGRESS -> {
                if (notification?.iconBitmap != null) {
                    Image(
                        bitmap = notification.iconBitmap.asImageBitmap(),
                        contentDescription = "Download Icon",
                        modifier = Modifier
                            .size(18.dp)
                            .clip(CircleShape)
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Download,
                        contentDescription = null,
                        tint = Color(0xFFE5E5EA),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            IslandPriority.MEDIA -> {
                val displayArt = media?.albumArt ?: media?.appIcon
                if (spinningAlbumArt) {
                    CompactSpinningVinylCover(
                        bitmap = displayArt,
                        isPlaying = media?.isPlaying == true,
                        size = 24.dp,
                        modifier = if (isLeftCutout || isCompactDual) Modifier else Modifier.offset(x = (-4).dp)
                    )
                } else if (displayArt != null) {
                    Image(
                        bitmap = displayArt.asImageBitmap(),
                        contentDescription = "Media Art",
                        modifier = Modifier
                            .size(20.dp)
                            .clip(RoundedCornerShape(5.dp))
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.MusicNote,
                        contentDescription = null,
                        tint = Color(0xFFA1A1AA),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            IslandPriority.FPS -> {
                Icon(
                    imageVector = Icons.Default.Speed,
                    contentDescription = "FPS",
                    tint = if (fpsInfo.isSmooth) Color(0xFF30D158) else Color(0xFFFFD60A),
                    modifier = Modifier.size(15.dp)
                )
            }

            IslandPriority.WEATHER -> {
                val weatherIcon = when {
                    weatherInfo.condition.contains("Sun", ignoreCase = true) || weatherInfo.condition.contains("Clear", ignoreCase = true) -> Icons.Default.WbSunny
                    weatherInfo.condition.contains("Rain", ignoreCase = true) -> Icons.Default.WaterDrop
                    else -> Icons.Default.Cloud
                }
                Icon(
                    imageVector = weatherIcon,
                    contentDescription = "Weather",
                    tint = Color(0xFFFF9F0A),
                    modifier = Modifier.size(15.dp)
                )
            }

            IslandPriority.NETWORK_SPEED -> {
                Icon(
                    imageVector = if (networkSpeedInfo.isWifi) Icons.Default.Wifi else Icons.Default.ArrowDownward,
                    contentDescription = "Speed",
                    tint = Color(0xFF64D2FF),
                    modifier = Modifier.size(14.dp)
                )
            }

            IslandPriority.IDLE -> Unit
        }
    }

    val trailingElement = @Composable {
        when (priority) {
            IslandPriority.CALL -> {
                val minutes = activeCall.durationSeconds / 60
                val seconds = activeCall.durationSeconds % 60
                Text(
                    text = String.format(Locale.US, "%02d:%02d", minutes, seconds),
                    color = Color(0xFF30D158),
                    fontSize = if (isCompactDual) 10.5.sp else 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    softWrap = false
                )
            }

            IslandPriority.SCREEN_RECORDING -> {
                val minutes = recording.durationSeconds / 60
                val seconds = recording.durationSeconds % 60
                Text(
                    text = String.format(Locale.US, "%02d:%02d", minutes, seconds),
                    color = Color(0xFFE5E5EA),
                    fontSize = if (isCompactDual) 10.5.sp else 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    softWrap = false
                )
            }

            IslandPriority.FLASHLIGHT -> Unit

            IslandPriority.POP_NOTIFICATION -> {
                Text(
                    text = notification?.title ?: "Notification",
                    color = Color(0xFFE5E5EA),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            IslandPriority.PROGRESS -> {
                val pct = notification?.progressFraction ?: 0f
                val isIndet = notification?.isIndeterminate == true
                if (isIndet) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(14.dp),
                        strokeWidth = 2.dp,
                        color = Color(0xFFE5E5EA),
                        trackColor = Color(0xFF2C2C2E)
                    )
                } else {
                    Text(
                        text = "${(pct * 100).roundToInt()}%",
                        color = Color(0xFFE5E5EA),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        softWrap = false
                    )
                }
            }

            IslandPriority.MEDIA -> {
                val displayArt = media?.albumArt ?: media?.appIcon
                val compactAccent = remember(displayArt) { extractMediaAccentColor(displayArt) }
                val compactEqTint = if (compactAccent != Color.Transparent) {
                    lerp(Color(0xFFE5E5EA), compactAccent, 0.50f)
                } else {
                    Color(0xFFE5E5EA)
                }
                EqualizerVisualizer(isPlaying = media?.isPlaying == true, barColor = compactEqTint)
            }

            IslandPriority.FPS -> {
                Text(
                    text = "${fpsInfo.fps} FPS",
                    color = if (fpsInfo.isSmooth) Color(0xFF30D158) else Color(0xFFFFD60A),
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    softWrap = false
                )
            }

            IslandPriority.WEATHER -> {
                Text(
                    text = "${weatherInfo.temperatureC}°",
                    color = Color.White,
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    softWrap = false
                )
            }

            IslandPriority.NETWORK_SPEED -> {
                Text(
                    text = "↓ ${networkSpeedInfo.downloadSpeedFormatted}",
                    color = Color(0xFF64D2FF),
                    fontSize = 10.5.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    softWrap = false
                )
            }

            IslandPriority.IDLE -> Unit
        }
    }

    if (isLeftCutout) {
        // Left-sided camera cut-out mode:
        // Leave left 36dp clear for the camera cut-out, and group both minimised elements together on the right side with slight spacing!
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(start = 38.dp, end = if (isCompactDual) 11.dp else 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(7.dp, Alignment.End)
        ) {
            leadingElement()
            trailingElement()
        }
    } else {
        val leftWeight = 1f
        val rightWeight = 1f
        val startPadding = (if (isCompactDual) 5.5.dp else 8.dp) + teardropEarInset
        val endPadding = (if (isCompactDual) 10.dp else 8.dp) + teardropEarInset
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    start = startPadding,
                    end = endPadding,
                    top = if (isTeardropCutout) 2.dp else 0.dp
                ),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Leading content: Pinned strictly to the left of the camera punch hole
            Box(
                modifier = Modifier.weight(leftWeight),
                contentAlignment = Alignment.CenterStart
            ) {
                leadingElement()
            }

            // Camera punch-hole deadzone: STRICTLY 24dp EMPTY SPACE IN THE MIDDLE!
            // No UI elements will EVER enter or overlap the camera cutout area
            Spacer(modifier = Modifier.width(24.dp))

            // Trailing content: Pinned strictly to the right of the camera punch hole
            Box(
                modifier = Modifier.weight(rightWeight),
                contentAlignment = Alignment.CenterEnd
            ) {
                trailingElement()
            }
        }
    }
}

@Composable
fun ExpandedCardContent(
    priority: IslandPriority,
    media: MediaInfo?,
    notification: NotificationInfo?,
    recording: ScreenRecordingState,
    activeCall: ActiveCallState = ActiveCallState(),
    flashlightLevel: Int = 1,
    maxFlashlightLevel: Int = 1,
    fpsInfo: FpsInfo = FpsInfo(),
    weatherInfo: WeatherInfo = WeatherInfo(),
    networkSpeedInfo: NetworkSpeedInfo = NetworkSpeedInfo(),
    scale: Float = 1.0f,
    spinningAlbumArt: Boolean = false,
    wavyProgressBar: Boolean = true,
    onCollapse: () -> Unit
) {
    when (priority) {
        IslandPriority.CALL -> {
            ExpandedCallView(activeCall, onCollapse, scale)
        }
        IslandPriority.SCREEN_RECORDING -> {
            ExpandedRecordingView(recording, onCollapse, scale)
        }
        IslandPriority.POP_NOTIFICATION -> {
            ExpandedNotificationView(notification, onCollapse, scale)
        }
        IslandPriority.PROGRESS -> {
            ExpandedProgressView(notification, onCollapse, scale)
        }
        IslandPriority.MEDIA -> {
            ExpandedMediaView(
                media = media,
                onCollapse = onCollapse,
                scale = scale,
                spinningAlbumArt = spinningAlbumArt,
                wavyProgressBar = wavyProgressBar
            )
        }
        IslandPriority.FLASHLIGHT -> {
            ExpandedFlashlightView(
                level = flashlightLevel,
                onSetLevel = { newLevel ->
                    IslandStateManager.setFlashlightBrightness(newLevel)
                },
                onTurnOff = {
                    IslandStateManager.turnOffFlashlight()
                },
                onCollapse = onCollapse
            )
        }
        IslandPriority.FPS -> {
            ExpandedFpsView(fpsInfo = fpsInfo, onCollapse = onCollapse, scale = scale)
        }
        IslandPriority.WEATHER -> {
            ExpandedWeatherView(weatherInfo = weatherInfo, onCollapse = onCollapse, scale = scale)
        }
        IslandPriority.NETWORK_SPEED -> {
            ExpandedNetworkSpeedView(networkSpeedInfo = networkSpeedInfo, onCollapse = onCollapse, scale = scale)
        }
        IslandPriority.IDLE -> {
            LaunchedEffect(Unit) {
                onCollapse()
            }
            ExpandedIdleView(onCollapse)
        }
    }
}

@Composable
fun ExpandedFlashlightView(
    level: Int,
    onSetLevel: (Int) -> Unit,
    onTurnOff: () -> Unit,
    onCollapse: () -> Unit
) {
    val view = LocalView.current

    // Current brightness level 1..4
    var currentLevel by remember(level) { mutableIntStateOf(level.coerceIn(1, 4)) }
    LaunchedEffect(level) {
        currentLevel = level.coerceIn(1, 4)
    }

    val currentLevelState = rememberUpdatedState(currentLevel)
    val onSetLevelState = rememberUpdatedState(onSetLevel)

    // Animated fraction for the light beam height (1 -> 0.25f, 4 -> 1.0f)
    val targetFraction = (currentLevel / 4f)
    val animatedFraction by animateFloatAsState(
        targetValue = targetFraction,
        animationSpec = spring(
            dampingRatio = 0.8f,
            stiffness = 450f
        ),
        label = "flashlightBeamFraction"
    )

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Upper section: The light-beam triangular blur vertical slider
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .pointerInput(Unit) {
                    awaitEachGesture {
                        val down = awaitFirstDown(requireUnconsumed = false)
                        val h = size.height.toFloat()
                        if (h > 0) {
                            val relY = (down.position.y / h).coerceIn(0f, 1f)
                            val newLevel = (((1f - relY) * 3.999f).toInt() + 1).coerceIn(1, 4)
                            if (newLevel != currentLevelState.value) {
                                currentLevel = newLevel
                                view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
                                onSetLevelState.value(newLevel)
                            }
                        }
                        while (true) {
                            val event = awaitPointerEvent()
                            val change = event.changes.firstOrNull() ?: break
                            if (!change.pressed) break
                            change.consume()
                            val currentH = size.height.toFloat()
                            if (currentH > 0) {
                                val relY = (change.position.y / currentH).coerceIn(0f, 1f)
                                val newLevel = (((1f - relY) * 3.999f).toInt() + 1).coerceIn(1, 4)
                                if (newLevel != currentLevelState.value) {
                                    currentLevel = newLevel
                                    view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
                                    onSetLevelState.value(newLevel)
                                }
                            }
                        }
                    }
                },
            contentAlignment = Alignment.BottomCenter
        ) {
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 4.dp, bottom = 0.dp)
                    .blur(radius = 8.dp, edgeTreatment = BlurredEdgeTreatment.Unbounded)
            ) {
                val w = size.width
                val h = size.height
                val centerX = w / 2f

                val blurpleTint = Color(0xFFD2D6FF)

                // Geometry:
                // Maximum widths at full brightness (level 4):
                val maxSecTopHalfW = (w * 0.18f).coerceAtMost(52.dp.toPx())
                val secBaseHalfW = 7.dp.toPx()

                val maxPrimTopHalfW = (w * 0.09f).coerceAtMost(26.dp.toPx())
                val primBaseHalfW = 3.5.dp.toPx()

                // Width expands dynamically with brightness, maxing out at current width for level 4!
                val widthFactor = 0.35f + 0.65f * animatedFraction
                val currentSecTopHalfW = maxSecTopHalfW * widthFactor
                val currentPrimTopHalfW = maxPrimTopHalfW * widthFactor

                // 1. Background guide cone: subtle blurple tint, fades away at the top!
                val guideOuterPath = Path().apply {
                    moveTo(centerX - secBaseHalfW - 3.dp.toPx(), h)
                    lineTo(centerX - maxSecTopHalfW - 6.dp.toPx(), 0f)
                    quadraticTo(centerX, -4.dp.toPx(), centerX + maxSecTopHalfW + 6.dp.toPx(), 0f)
                    lineTo(centerX + secBaseHalfW + 3.dp.toPx(), h)
                    close()
                }
                drawPath(
                    path = guideOuterPath,
                    brush = Brush.verticalGradient(
                        colorStops = arrayOf(
                            0.0f to Color.Transparent, // Fades away at the top!
                            0.20f to blurpleTint.copy(alpha = 0.03f),
                            0.60f to blurpleTint.copy(alpha = 0.05f),
                            1.0f to Color.Transparent
                        ),
                        startY = 0f,
                        endY = h
                    )
                )

                val guidePath = Path().apply {
                    moveTo(centerX - secBaseHalfW, h)
                    lineTo(centerX - maxSecTopHalfW, 0f)
                    quadraticTo(centerX, -4.dp.toPx(), centerX + maxSecTopHalfW, 0f)
                    lineTo(centerX + secBaseHalfW, h)
                    close()
                }
                drawPath(
                    path = guidePath,
                    brush = Brush.verticalGradient(
                        colorStops = arrayOf(
                            0.0f to Color.Transparent, // Fades away at the top!
                            0.18f to blurpleTint.copy(alpha = 0.08f),
                            0.55f to blurpleTint.copy(alpha = 0.13f),
                            1.0f to blurpleTint.copy(alpha = 0.04f)
                        ),
                        startY = 0f,
                        endY = h
                    )
                )

                // 2. Active illuminated beams filling up to animatedFraction (1..4)
                val activeH = h * animatedFraction
                val activeTopY = h - activeH
                val activeSecTopHalfW = secBaseHalfW + (currentSecTopHalfW - secBaseHalfW) * animatedFraction
                val activePrimTopHalfW = primBaseHalfW + (currentPrimTopHalfW - primBaseHalfW) * animatedFraction

                // Dynamic fade distance at top: fades away at top, but fade reduces when less bright
                val fadeLength = (28.dp.toPx() * animatedFraction).coerceAtMost(activeH * 0.40f).coerceAtLeast(4.dp.toPx())
                val fadeStop = (fadeLength / activeH).coerceIn(0.04f, 0.45f)

                // Diffused outer atmospheric halo
                val haloSpread = 5.dp.toPx() * animatedFraction
                val haloPath = Path().apply {
                    moveTo(centerX - secBaseHalfW - 2.dp.toPx(), h)
                    lineTo(centerX - activeSecTopHalfW - haloSpread, activeTopY)
                    quadraticTo(centerX, activeTopY - 4.dp.toPx(), centerX + activeSecTopHalfW + haloSpread, activeTopY)
                    lineTo(centerX + secBaseHalfW + 2.dp.toPx(), h)
                    close()
                }
                drawPath(
                    path = haloPath,
                    brush = Brush.verticalGradient(
                        colorStops = arrayOf(
                            0.0f to Color.Transparent,
                            fadeStop to Color.White.copy(alpha = 0.12f * animatedFraction),
                            1.0f to Color.Transparent
                        ),
                        startY = activeTopY,
                        endY = h
                    )
                )

                // 3. Secondary beam: slightly less bright, same width as now (realistic spill beam)
                val secBeamPath = Path().apply {
                    moveTo(centerX - secBaseHalfW, h)
                    lineTo(centerX - activeSecTopHalfW, activeTopY)
                    quadraticTo(centerX, activeTopY - 3.dp.toPx(), centerX + activeSecTopHalfW, activeTopY)
                    lineTo(centerX + secBaseHalfW, h)
                    close()
                }
                val secIntensity = 0.32f + 0.22f * animatedFraction
                drawPath(
                    path = secBeamPath,
                    brush = Brush.verticalGradient(
                        colorStops = arrayOf(
                            0.0f to Color.Transparent, // Fades away at the top!
                            fadeStop to Color.White.copy(alpha = 0.35f * secIntensity),
                            0.60f to Color.White.copy(alpha = 0.24f * secIntensity),
                            1.0f to Color.White.copy(alpha = 0.08f * secIntensity)
                        ),
                        startY = activeTopY,
                        endY = h
                    )
                )

                // 4. Primary core beam: half as thin again, brilliant hotspot, fades away at the top
                val primBeamPath = Path().apply {
                    moveTo(centerX - primBaseHalfW, h)
                    lineTo(centerX - activePrimTopHalfW, activeTopY)
                    quadraticTo(centerX, activeTopY - 2.dp.toPx(), centerX + activePrimTopHalfW, activeTopY)
                    lineTo(centerX + primBaseHalfW, h)
                    close()
                }
                val primIntensity = 0.70f + 0.30f * animatedFraction
                drawPath(
                    path = primBeamPath,
                    brush = Brush.verticalGradient(
                        colorStops = arrayOf(
                            0.0f to Color.Transparent, // Fades away at the top!
                            fadeStop to Color.White.copy(alpha = 0.96f * primIntensity),
                            0.65f to Color.White.copy(alpha = 0.78f * primIntensity),
                            1.0f to Color.White.copy(alpha = 0.45f * primIntensity)
                        ),
                        startY = activeTopY,
                        endY = h
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Bottom section: Large centered flashlight icon (NO dark circle background, NO radial glow)
        // Tapping on this icon turns the flashlight off!
        val (flashBtnInteraction, flashBtnBounce) = rememberBounceButtonState()
        Box(
            modifier = Modifier
                .padding(bottom = 8.dp)
                .size(64.dp)
                .then(flashBtnBounce)
                .clickable(
                    interactionSource = flashBtnInteraction,
                    indication = null
                ) {
                    onTurnOff()
                    onCollapse()
                },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.FlashlightOn,
                contentDescription = "Turn off flashlight",
                tint = Color.White,
                modifier = Modifier.size(46.dp)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpandedMediaView(
    media: MediaInfo?,
    onCollapse: () -> Unit,
    scale: Float = 1.0f,
    spinningAlbumArt: Boolean = false,
    wavyProgressBar: Boolean = true
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var isSeeking by remember { mutableStateOf(false) }
    var scrubFraction by remember { mutableFloatStateOf(0f) }
    var isSeekPending by remember { mutableStateOf(false) }

    val rawDuration = media?.durationMs ?: 0L
    val hasValidDuration = rawDuration > 0L
    val duration = if (hasValidDuration) rawDuration else 0L

    var livePosition by remember(media?.packageName, media?.title) {
        mutableLongStateOf(media?.computeCurrentPosition() ?: 0L)
    }

    LaunchedEffect(media?.isPlaying, media?.positionMs, media?.lastPositionUpdateTimeMs, isSeeking, isSeekPending) {
        if (!isSeeking && !isSeekPending) {
            livePosition = media?.computeCurrentPosition() ?: 0L
            if (media?.isPlaying == true) {
                while (isActive) {
                    kotlinx.coroutines.delay(250)
                    if (!isSeeking && !isSeekPending) {
                        livePosition = media?.computeCurrentPosition() ?: 0L
                    }
                }
            }
        }
    }

    val currentPosition = when {
        isSeeking -> if (hasValidDuration) (scrubFraction * duration).toLong().coerceIn(0L, duration) else livePosition
        else -> livePosition
    }

    val sliderProgress = when {
        isSeeking -> scrubFraction
        hasValidDuration -> (livePosition.toFloat() / duration).coerceIn(0f, 1f)
        else -> 0f
    }

    val displayArt = media?.albumArt ?: media?.appIcon
    val mediaAccentColor = remember(displayArt) { extractMediaAccentColor(displayArt) }

    val controlsTint = if (mediaAccentColor != Color.Transparent) {
        lerp(Color(0xFFE5E5EA), mediaAccentColor, 0.40f)
    } else {
        Color(0xFFE5E5EA)
    }

    val playButtonBg = if (mediaAccentColor != Color.Transparent) {
        lerp(Color.White, mediaAccentColor, 0.28f)
    } else {
        Color.White
    }

    val playButtonIconTint = if (mediaAccentColor != Color.Transparent) {
        lerp(Color.Black, mediaAccentColor, 0.15f)
    } else {
        Color.Black
    }

    val eqTint = if (mediaAccentColor != Color.Transparent) {
        lerp(Color(0xFFE5E5EA), mediaAccentColor, 0.50f)
    } else {
        Color(0xFFE5E5EA)
    }

    val sliderActiveTint = if (mediaAccentColor != Color.Transparent) {
        lerp(Color(0xFFE5E5EA), mediaAccentColor, 0.55f)
    } else {
        Color(0xFFE5E5EA)
    }

    val sliderInactiveTint = if (mediaAccentColor != Color.Transparent) {
        lerp(Color(0xFF2C2C2E), mediaAccentColor, 0.22f)
    } else {
        Color(0xFF2C2C2E)
    }

    val durationTextTint = if (mediaAccentColor != Color.Transparent) {
        lerp(Color(0xFF8E8E93), mediaAccentColor, 0.35f)
    } else {
        Color(0xFF8E8E93)
    }

    val artSize = (54.dp * scale).coerceIn(40.dp, 64.dp)
    val headerGap = (14.dp * scale).coerceIn(8.dp, 16.dp)
    val titleSize = (17 * scale).coerceIn(14f, 19f).sp
    val artistSize = (13 * scale).coerceIn(11.5f, 15f).sp
    val sliderGapTop = (8.dp * scale).coerceIn(4.dp, 10.dp)
    val sliderGapBottom = (4.dp * scale).coerceIn(2.dp, 8.dp)
    val prevNextSize = (42.dp * scale).coerceIn(34.dp, 48.dp)
    val prevNextIcon = (24.dp * scale).coerceIn(18.dp, 28.dp)
    val playBtnSize = (50.dp * scale).coerceIn(40.dp, 58.dp)
    val playBtnIcon = (30.dp * scale).coerceIn(22.dp, 36.dp)
    val controlsGap = (20.dp * scale).coerceIn(12.dp, 26.dp)

    val isPlaying = media?.isPlaying == true
    val vinylFraction by animateFloatAsState(
        targetValue = if (spinningAlbumArt && isPlaying) 1f else 0f,
        animationSpec = spring(dampingRatio = 0.78f, stiffness = 300f),
        label = "vinylFraction"
    )
    val artCornerRadius = 12.dp + ((artSize / 2f) - 12.dp) * vinylFraction
    val vinylRotation = remember { Animatable(0f) }
    LaunchedEffect(spinningAlbumArt, isPlaying) {
        if (spinningAlbumArt && isPlaying) {
            while (isActive) {
                val startRot = vinylRotation.value % 360f
                vinylRotation.snapTo(startRot)
                vinylRotation.animateTo(
                    targetValue = startRot + 360f,
                    animationSpec = tween(durationMillis = 9000, easing = LinearEasing)
                )
            }
        }
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Header: Album cover / app icon + Title & Artist + Equalizer
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Album cover (larger, tap to open app and collapse island)
            Box(
                modifier = Modifier
                    .size(artSize)
                    .graphicsLayer {
                        if (spinningAlbumArt || vinylFraction > 0.01f) {
                            rotationZ = vinylRotation.value * vinylFraction
                        }
                    }
                    .clip(RoundedCornerShape(artCornerRadius))
                    .background(Color(0xFF212121))
                    .clickable {
                        launchAppOrIntent(context, media?.packageName, media?.sessionActivity)
                        onCollapse()
                    },
                contentAlignment = Alignment.Center
            ) {
                if (displayArt != null) {
                    Image(
                        bitmap = displayArt.asImageBitmap(),
                        contentDescription = "Cover Art",
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Radio,
                        contentDescription = null,
                        tint = Color(0xFFA1A1AA),
                        modifier = Modifier.size((28.dp * scale).coerceIn(20.dp, 32.dp))
                    )
                }

                if (vinylFraction > 0.02f) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val r = size.minDimension / 2f
                        drawCircle(
                            color = Color.White.copy(alpha = 0.22f * vinylFraction),
                            radius = r - 1.dp.toPx(),
                            style = Stroke(width = 1.2.dp.toPx())
                        )
                        drawCircle(
                            color = Color.Black.copy(alpha = 0.28f * vinylFraction),
                            radius = r * 0.68f,
                            style = Stroke(width = 1.dp.toPx())
                        )
                        drawCircle(
                            color = Color(0xFF141416).copy(alpha = 0.90f * vinylFraction),
                            radius = 6.dp.toPx() * vinylFraction
                        )
                        drawCircle(
                            color = Color.White.copy(alpha = 0.65f * vinylFraction),
                            radius = 2.2.dp.toPx() * vinylFraction
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(headerGap))

            Column(modifier = Modifier.weight(1f)) {
                // Title with infinite marquee slide
                Text(
                    text = media?.title?.ifBlank { "No Media Playing" } ?: "No Media Playing",
                    color = Color.White,
                    fontSize = titleSize,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    modifier = Modifier.basicMarquee(
                        iterations = Int.MAX_VALUE,
                        initialDelayMillis = 1000,
                        velocity = 30.dp
                    )
                )
                Spacer(modifier = Modifier.height(3.dp))
                // Artist with marquee slide
                Text(
                    text = media?.artist?.ifBlank { "Unknown Artist" } ?: "Unknown Artist",
                    color = Color(0xFFA1A1AA),
                    fontSize = artistSize,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    modifier = Modifier.basicMarquee(
                        iterations = Int.MAX_VALUE,
                        initialDelayMillis = 1500,
                        velocity = 26.dp
                    )
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            EqualizerVisualizer(isPlaying = media?.isPlaying == true, barColor = eqTint)
        }

        Spacer(modifier = Modifier.height(sliderGapTop))

        // Progress Bar & Duration on Left & Right
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = formatDuration(currentPosition),
                color = durationTextTint,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.width(10.dp))

            if (wavyProgressBar) {
                WavyMediaScrubber(
                    progress = sliderProgress,
                    enabled = hasValidDuration,
                    isPlaying = isPlaying && !isSeeking,
                    activeColor = sliderActiveTint,
                    inactiveColor = sliderInactiveTint,
                    onValueChange = { frac ->
                        isSeeking = true
                        scrubFraction = frac
                    },
                    onValueChangeFinished = {
                        if (hasValidDuration) {
                            val seekTargetMs = (scrubFraction * duration).toLong().coerceIn(0L, duration)
                            livePosition = seekTargetMs
                            isSeekPending = true
                            isSeeking = false
                            IslandStateManager.onSeekTo?.invoke(seekTargetMs)
                            coroutineScope.launch {
                                kotlinx.coroutines.delay(650)
                                isSeekPending = false
                            }
                        } else {
                            isSeeking = false
                        }
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(22.dp)
                )
            } else {
                Slider(
                    value = sliderProgress,
                    enabled = hasValidDuration,
                    onValueChange = { frac ->
                        isSeeking = true
                        scrubFraction = frac
                    },
                    onValueChangeFinished = {
                        if (hasValidDuration) {
                            val seekTargetMs = (scrubFraction * duration).toLong().coerceIn(0L, duration)
                            livePosition = seekTargetMs
                            isSeekPending = true
                            isSeeking = false
                            IslandStateManager.onSeekTo?.invoke(seekTargetMs)
                            coroutineScope.launch {
                                kotlinx.coroutines.delay(650)
                                isSeekPending = false
                            }
                        } else {
                            isSeeking = false
                        }
                    },
                    thumb = {
                        if (hasValidDuration) {
                            Box(
                                modifier = Modifier
                                    .width(3.dp)
                                    .height(14.dp)
                                    .clip(RoundedCornerShape(1.5.dp))
                                    .background(sliderActiveTint)
                            )
                        }
                    },
                    track = { sliderState ->
                        SliderDefaults.Track(
                            sliderState = sliderState,
                            drawStopIndicator = null,
                            drawTick = { _, _ -> },
                            colors = SliderDefaults.colors(
                                activeTrackColor = sliderActiveTint,
                                inactiveTrackColor = sliderInactiveTint,
                                disabledActiveTrackColor = sliderInactiveTint.copy(alpha = 0.5f),
                                disabledInactiveTrackColor = sliderInactiveTint.copy(alpha = 0.3f)
                            )
                        )
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            Text(
                text = if (hasValidDuration) formatDuration(duration) else "--:--",
                color = durationTextTint,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold
            )
        }

        Spacer(modifier = Modifier.height(sliderGapBottom))

        // Playback Controls (moved upward, no big gap)
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            // Centered Controls: Previous, Play/Pause, Next
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                BouncyIconButton(
                    onClick = { IslandStateManager.onSkipPrevious?.invoke() },
                    modifier = Modifier.size(prevNextSize)
                ) {
                    Icon(
                        imageVector = Icons.Default.FastRewind,
                        contentDescription = "Previous",
                        tint = controlsTint,
                        modifier = Modifier.size(prevNextIcon)
                    )
                }

                Spacer(modifier = Modifier.width(controlsGap))

                BouncyIconButton(
                    onClick = { IslandStateManager.onPlayPause?.invoke() },
                    modifier = Modifier
                        .size(playBtnSize)
                        .clip(CircleShape)
                        .background(playButtonBg)
                ) {
                    Icon(
                        imageVector = if (media?.isPlaying == true) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = "Play/Pause",
                        tint = playButtonIconTint,
                        modifier = Modifier.size(playBtnIcon)
                    )
                }

                Spacer(modifier = Modifier.width(controlsGap))

                BouncyIconButton(
                    onClick = { IslandStateManager.onSkipNext?.invoke() },
                    modifier = Modifier.size(prevNextSize)
                ) {
                    Icon(
                        imageVector = Icons.Default.FastForward,
                        contentDescription = "Next",
                        tint = controlsTint,
                        modifier = Modifier.size(prevNextIcon)
                    )
                }
            }


        }
    }
}

@Composable
fun ExpandedNotificationView(notification: NotificationInfo?, onCollapse: () -> Unit, scale: Float = 1.0f) {
    val context = LocalContext.current
    var isReplying by remember { mutableStateOf(false) }
    var replyText by remember { mutableStateOf("") }

    val replyAction = remember(notification) {
        notification?.actions?.firstOrNull { it.isReply || it.title.contains("Reply", ignoreCase = true) }
    }

    fun openNotification() {
        IslandStateManager.setKeyboardFocused(false)
        val pendingIntent = notification?.contentIntent
        var launched = false
        if (pendingIntent != null) {
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                    val options = ActivityOptions.makeBasic().apply {
                        pendingIntentBackgroundActivityStartMode = ActivityOptions.MODE_BACKGROUND_ACTIVITY_START_ALLOWED
                    }.toBundle()
                    pendingIntent.send(context, 0, null, null, null, null, options)
                } else {
                    pendingIntent.send()
                }
                launched = true
            } catch (e: Exception) {
                Log.e("ExpandedNotification", "Failed to send contentIntent", e)
            }
        }
        if (!launched && !notification?.packageName.isNullOrBlank()) {
            val launchIntent = context.packageManager.getLaunchIntentForPackage(notification.packageName)
            if (launchIntent != null) {
                launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED)
                context.startActivity(launchIntent)
            }
        }
        onCollapse()
    }

    fun sendReply(text: String) {
        if (text.isBlank() || notification == null) return
        IslandStateManager.onSendReply?.invoke(notification.key, text.trim())
        IslandStateManager.setKeyboardFocused(false)
        IslandStateManager.dismissCurrentNotification()
        onCollapse()
    }

    val iconSize = (40.dp * scale).coerceIn(32.dp, 48.dp)
    val iconGap = (12.dp * scale).coerceIn(8.dp, 16.dp)
    val titleSize = (17 * scale).coerceIn(14f, 19f).sp
    val appNameSize = (13 * scale).coerceIn(11.5f, 15f).sp
    val bodySize = (15 * scale).coerceIn(13f, 17f).sp
    val bodyLineHeight = (20 * scale).coerceIn(17f, 22f).sp
    val bodyMaxLines = if (scale < 0.88f) 2 else 3
    val buttonHeight = (38.dp * scale).coerceIn(32.dp, 44.dp)
    val buttonFontSize = (13.5f * scale).coerceIn(12f, 15f).sp

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Top section: Header + Body text
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // App icon (tap to open app)
                Box(
                    modifier = Modifier
                        .size(iconSize)
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0xFF2C2C2E))
                        .clickable {
                            launchAppOrIntent(context, notification?.packageName, notification?.contentIntent)
                            onCollapse()
                        },
                    contentAlignment = Alignment.Center
                ) {
                    if (notification?.iconBitmap != null) {
                        Image(
                            bitmap = notification.iconBitmap.asImageBitmap(),
                            contentDescription = "App Icon",
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Notifications,
                            contentDescription = null,
                            tint = Color(0xFFE5E5EA),
                            modifier = Modifier.size((24.dp * scale).coerceIn(18.dp, 28.dp))
                        )
                    }
                }

                Spacer(modifier = Modifier.width(iconGap))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = notification?.title ?: "Notification",
                        color = Color.White,
                        fontSize = titleSize,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        modifier = Modifier.basicMarquee(iterations = Int.MAX_VALUE)
                    )
                    Text(
                        text = notification?.appName ?: "",
                        color = Color(0xFFA1A1AA),
                        fontSize = appNameSize,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                BouncyIconButton(
                    onClick = {
                        IslandStateManager.setKeyboardFocused(false)
                        IslandStateManager.dismissCurrentNotification()
                        onCollapse()
                    },
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = "Dismiss",
                        tint = Color(0xFF8E8E93),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            if (!notification?.text.isNullOrBlank()) {
                Spacer(modifier = Modifier.height((6.dp * scale).coerceIn(3.dp, 8.dp)))

                Text(
                    text = notification?.text ?: "",
                    color = Color(0xFFF4F4F5),
                    fontSize = bodySize,
                    lineHeight = bodyLineHeight,
                    fontWeight = FontWeight.Normal,
                    maxLines = bodyMaxLines,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { openNotification() }
                )
            }
        }

        // Action Section: Either Inline Reply Row or Action Buttons
        if (isReplying) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // Smart reply choices chips
                val smartChoices = remember(replyAction) {
                    val choices = replyAction?.remoteInputChoices ?: emptyList()
                    if (choices.isNotEmpty()) choices else listOf("OK", "Sure", "Thanks!")
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    smartChoices.take(3).forEach { choice ->
                        val (chipInteraction, chipBounce) = rememberBounceButtonState()
                        Box(
                            modifier = Modifier
                                .then(chipBounce)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFF2C2C2E))
                                .clickable(
                                    interactionSource = chipInteraction,
                                    indication = null
                                ) { sendReply(choice) }
                                .padding(horizontal = 10.dp, vertical = 5.dp)
                        ) {
                            Text(
                                text = choice,
                                color = Color(0xFFE5E5EA),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }

                // Inline Reply Text Input Row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(42.dp)
                        .clip(RoundedCornerShape(21.dp))
                        .background(Color(0xFF212121))
                        .padding(horizontal = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    BasicTextField(
                        value = replyText,
                        onValueChange = { replyText = it },
                        modifier = Modifier
                            .weight(1f)
                            .onFocusChanged { focusState ->
                                if (focusState.isFocused) {
                                    IslandStateManager.setKeyboardFocused(true)
                                }
                            },
                        textStyle = TextStyle(
                            color = Color.White,
                            fontSize = 14.sp
                        ),
                        singleLine = true,
                        cursorBrush = SolidColor(Color(0xFFE5E5EA)),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                        keyboardActions = KeyboardActions(onSend = { sendReply(replyText) }),
                        decorationBox = { innerTextField ->
                            if (replyText.isEmpty()) {
                                Text(
                                    text = replyAction?.remoteInputLabel ?: "Type a reply...",
                                    color = Color(0xFF71717A),
                                    fontSize = 14.sp
                                )
                            }
                            innerTextField()
                        }
                    )

                    BouncyIconButton(
                        onClick = { sendReply(replyText) },
                        enabled = replyText.isNotBlank(),
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Send,
                            contentDescription = "Send Reply",
                            tint = if (replyText.isNotBlank()) Color.White else Color(0xFF52525B),
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    BouncyIconButton(
                        onClick = {
                            isReplying = false
                            IslandStateManager.setKeyboardFocused(false)
                        },
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Cancel",
                            tint = Color(0xFF71717A),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        } else {
            // Action Buttons: Taller, dynamically dividing available width equally, spaced by 4dp,
            // with inner corners facing adjacent buttons being squarish
            val actionItems = remember(notification, replyAction) {
                val list = mutableListOf<NotificationActionItem>()
                val notifActions = notification?.actions ?: emptyList()
                notifActions.take(2).forEach { action ->
                    val isThisReply = action == replyAction || action.isReply || action.title.contains("Reply", ignoreCase = true)
                    list.add(
                        NotificationActionItem(
                            title = action.title,
                            isPrimary = false,
                            onClick = {
                                if (isThisReply) {
                                    isReplying = true
                                    IslandStateManager.setKeyboardFocused(true)
                                } else {
                                    runCatching { action.actionIntent?.send() }
                                    IslandStateManager.dismissCurrentNotification()
                                    onCollapse()
                                }
                            }
                        )
                    )
                }
                list.add(
                    NotificationActionItem(
                        title = "Open",
                        isPrimary = true,
                        onClick = { openNotification() }
                    )
                )
                list
            }

            val totalButtons = actionItems.size
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                actionItems.forEachIndexed { index, item ->
                    val shape = when {
                        totalButtons <= 1 -> RoundedCornerShape(14.dp)
                        index == 0 -> RoundedCornerShape(
                            topStart = 14.dp,
                            bottomStart = 14.dp,
                            topEnd = 4.dp,
                            bottomEnd = 4.dp
                        )
                        index == totalButtons - 1 -> RoundedCornerShape(
                            topStart = 4.dp,
                            bottomStart = 4.dp,
                            topEnd = 14.dp,
                            bottomEnd = 14.dp
                        )
                        else -> RoundedCornerShape(4.dp)
                    }

                    BouncyFilledTonalButton(
                        onClick = item.onClick,
                        modifier = Modifier
                            .weight(1f)
                            .height(buttonHeight),
                        shape = shape,
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp),
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = if (item.isPrimary) Color(0xFFE5E5EA) else Color(0xFF2C2C2E),
                            contentColor = if (item.isPrimary) Color.Black else Color(0xFFF4F4F5)
                        )
                    ) {
                        Text(
                            text = item.title,
                            fontSize = buttonFontSize,
                            fontWeight = if (item.isPrimary) FontWeight.Bold else FontWeight.SemiBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }
}

private data class NotificationActionItem(
    val title: String,
    val isPrimary: Boolean,
    val onClick: () -> Unit
)

@Composable
fun ExpandedProgressView(notification: NotificationInfo?, onCollapse: () -> Unit, scale: Float = 1.0f) {
    val context = LocalContext.current
    val progressFraction = notification?.progressFraction ?: 0f
    val isIndet = notification?.isIndeterminate == true
    val percentageText = if (isIndet) "" else "${(progressFraction * 100).roundToInt()}%"

    fun openProgressApp() {
        launchAppOrIntent(context, notification?.packageName, notification?.contentIntent)
        onCollapse()
    }

    val iconSize = (38.dp * scale).coerceIn(30.dp, 46.dp)
    val iconGap = (10.dp * scale).coerceIn(8.dp, 14.dp)
    val titleSize = (15.5f * scale).coerceIn(13.5f, 18f).sp
    val subtitleSize = (12.5f * scale).coerceIn(11f, 14.5f).sp
    val circleButtonSize = (46.dp * scale).coerceIn(38.dp, 54.dp)
    val actionIconSize = (21.dp * scale).coerceIn(17.dp, 25.dp)

    val rawProgressText = notification?.text ?: ""
    val displayProgressText = remember(rawProgressText, notification?.appName) {
        var s = rawProgressText.trim()
        while (s.startsWith("•") || s.startsWith("·") || s.startsWith("-")) {
            s = s.substring(1).trim()
        }
        while (s.endsWith("•") || s.endsWith("·") || s.endsWith("-")) {
            s = s.substring(0, s.length - 1).trim()
        }
        s.ifBlank { notification?.appName?.ifBlank { "In progress..." } ?: "In progress..." }
    }

    val primaryAction = remember(notification) {
        notification?.actions?.firstOrNull()
    }

    val isDestructiveAction = remember(primaryAction) {
        val t = primaryAction?.title ?: ""
        t.contains("cancel", ignoreCase = true) ||
                t.contains("stop", ignoreCase = true) ||
                t.contains("abort", ignoreCase = true) ||
                t.contains("delete", ignoreCase = true) ||
                t.contains("discard", ignoreCase = true) ||
                t.contains("dismiss", ignoreCase = true)
    }

    val primaryActionIcon = remember(primaryAction) {
        val t = primaryAction?.title ?: ""
        when {
            t.contains("pause", ignoreCase = true) -> Icons.Default.Pause
            t.contains("resume", ignoreCase = true) ||
                    t.contains("play", ignoreCase = true) ||
                    t.contains("start", ignoreCase = true) ||
                    t.contains("continue", ignoreCase = true) -> Icons.Default.PlayArrow
            t.contains("retry", ignoreCase = true) ||
                    t.contains("restart", ignoreCase = true) -> Icons.Default.Refresh
            t.contains("stop", ignoreCase = true) -> Icons.Default.Stop
            t.contains("cancel", ignoreCase = true) ||
                    t.contains("abort", ignoreCase = true) ||
                    t.contains("close", ignoreCase = true) ||
                    t.contains("dismiss", ignoreCase = true) -> Icons.Default.Close
            else -> Icons.Default.Check
        }
    }

    Row(
        modifier = Modifier.fillMaxSize(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // Left section: App Icon + Title/Subtitle + Thin Progress Bar
        Row(
            modifier = Modifier
                .weight(1f)
                .padding(end = 10.dp)
                .clickable { openProgressApp() },
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(iconSize)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0xFF212121)),
                contentAlignment = Alignment.Center
            ) {
                if (notification?.iconBitmap != null) {
                    Image(
                        bitmap = notification.iconBitmap.asImageBitmap(),
                        contentDescription = "App Icon",
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Download,
                        contentDescription = null,
                        tint = Color(0xFFE5E5EA),
                        modifier = Modifier.size((22.dp * scale).coerceIn(17.dp, 26.dp))
                    )
                }
            }

            Spacer(modifier = Modifier.width(iconGap))

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = notification?.title?.ifBlank { "Downloading" } ?: "Downloading",
                    color = Color.White,
                    fontSize = titleSize,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.basicMarquee(iterations = Int.MAX_VALUE)
                )

                Spacer(modifier = Modifier.height(2.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Start
                ) {
                    if (!isIndet && percentageText.isNotEmpty()) {
                        Text(
                            text = percentageText,
                            color = Color(0xFF64D2FF),
                            fontSize = subtitleSize,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1
                        )
                        Text(
                            text = " • ",
                            color = Color(0xFF71717A),
                            fontSize = subtitleSize,
                            fontWeight = FontWeight.Medium
                        )
                    }
                    Text(
                        text = displayProgressText,
                        color = Color(0xFFA1A1AA),
                        fontSize = subtitleSize,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                if (isIndet) {
                    LinearProgressIndicator(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(4.dp)
                            .clip(RoundedCornerShape(2.dp)),
                        color = Color(0xFFE5E5EA),
                        trackColor = Color(0xFF2C2C2E)
                    )
                } else {
                    LinearProgressIndicator(
                        progress = { progressFraction },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(4.dp)
                            .clip(RoundedCornerShape(2.dp)),
                        color = Color(0xFFE5E5EA),
                        trackColor = Color(0xFF2C2C2E)
                    )
                }
            }
        }

        // Right section: Circular Icon Buttons matching Screen Recording and Call cards
        Row(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (primaryAction != null) {
                BouncyFilledTonalButton(
                    onClick = {
                        runCatching { primaryAction.actionIntent?.send() }
                        if (isDestructiveAction) {
                            onCollapse()
                        }
                    },
                    modifier = Modifier.size(circleButtonSize),
                    shape = CircleShape,
                    contentPadding = PaddingValues(0.dp),
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = if (isDestructiveAction) Color(0xFFFF453A).copy(alpha = 0.22f) else Color(0xFF2C2C2E),
                        contentColor = if (isDestructiveAction) Color(0xFFFF453A) else Color(0xFFF4F4F5)
                    )
                ) {
                    Icon(
                        imageVector = primaryActionIcon,
                        contentDescription = primaryAction.title,
                        tint = if (isDestructiveAction) Color(0xFFFF453A) else Color(0xFFF4F4F5),
                        modifier = Modifier.size(actionIconSize)
                    )
                }
            } else {
                BouncyFilledTonalButton(
                    onClick = {
                        IslandStateManager.dismissProgress()
                        onCollapse()
                    },
                    modifier = Modifier.size(circleButtonSize),
                    shape = CircleShape,
                    contentPadding = PaddingValues(0.dp),
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = Color(0xFF2C2C2E),
                        contentColor = Color(0xFFF4F4F5)
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Dismiss",
                        modifier = Modifier.size(actionIconSize)
                    )
                }
            }

            BouncyFilledTonalButton(
                onClick = { openProgressApp() },
                modifier = Modifier.size(circleButtonSize),
                shape = CircleShape,
                contentPadding = PaddingValues(0.dp),
                colors = ButtonDefaults.filledTonalButtonColors(
                    containerColor = Color(0xFFE5E5EA),
                    contentColor = Color.Black
                )
            ) {
                Icon(
                    imageVector = Icons.Default.OpenInNew,
                    contentDescription = "Open",
                    tint = Color.Black,
                    modifier = Modifier.size(actionIconSize)
                )
            }
        }
    }
}

@Composable
fun ExpandedRecordingView(recording: ScreenRecordingState, onCollapse: () -> Unit, scale: Float = 1.0f) {
    val minutes = recording.durationSeconds / 60
    val seconds = recording.durationSeconds % 60
    val timerText = String.format(Locale.US, "%02d:%02d", minutes, seconds)
    val timerSize = (22 * scale).coerceIn(18f, 26f).sp
    val circleButtonSize = (46.dp * scale).coerceIn(38.dp, 54.dp)
    val iconSize = (22.dp * scale).coerceIn(18.dp, 26.dp)

    Row(
        modifier = Modifier.fillMaxSize(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start
        ) {
            PulsingRecordingDot(modifier = Modifier.size(11.dp))
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = timerText,
                color = Color.White,
                fontSize = timerSize,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.5.sp,
                maxLines = 1
            )
        }

        Row(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            BouncyFilledTonalButton(
                onClick = {
                    IslandStateManager.onTogglePauseRecording?.invoke()
                },
                modifier = Modifier.size(circleButtonSize),
                shape = CircleShape,
                contentPadding = PaddingValues(0.dp),
                colors = ButtonDefaults.filledTonalButtonColors(
                    containerColor = Color(0xFF2C2C2E),
                    contentColor = Color(0xFFF4F4F5)
                )
            ) {
                Icon(
                    imageVector = if (recording.isPaused) Icons.Default.PlayArrow else Icons.Default.Pause,
                    contentDescription = if (recording.isPaused) "Resume" else "Pause",
                    modifier = Modifier.size(iconSize)
                )
            }

            BouncyFilledTonalButton(
                onClick = {
                    IslandStateManager.onStopRecording?.invoke()
                    onCollapse()
                },
                modifier = Modifier.size(circleButtonSize),
                shape = CircleShape,
                contentPadding = PaddingValues(0.dp),
                colors = ButtonDefaults.filledTonalButtonColors(
                    containerColor = Color(0xFFFF453A).copy(alpha = 0.22f),
                    contentColor = Color(0xFFFF453A)
                )
            ) {
                Icon(
                    imageVector = Icons.Default.Stop,
                    contentDescription = "Stop",
                    tint = Color(0xFFFF453A),
                    modifier = Modifier.size(iconSize)
                )
            }
        }
    }
}

@Composable
fun ExpandedCallView(activeCall: ActiveCallState, onCollapse: () -> Unit, scale: Float = 1.0f) {
    val context = LocalContext.current
    val minutes = activeCall.durationSeconds / 60
    val seconds = activeCall.durationSeconds % 60
    val timerText = String.format(Locale.US, "%02d:%02d", minutes, seconds)
    val callerSize = (16 * scale).coerceIn(14f, 19f).sp
    val timerSize = (13.5f * scale).coerceIn(12f, 16f).sp
    val circleButtonSize = (46.dp * scale).coerceIn(38.dp, 54.dp)
    val iconSize = (22.dp * scale).coerceIn(18.dp, 26.dp)

    Row(
        modifier = Modifier.fillMaxSize(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(end = 10.dp)
                .clickable {
                    launchAppOrIntent(context, activeCall.packageName, activeCall.contentIntent)
                    onCollapse()
                },
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = activeCall.callerName.ifBlank { "Active Call" },
                color = Color.White,
                fontSize = callerSize,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.basicMarquee(iterations = Int.MAX_VALUE)
            )
            Spacer(modifier = Modifier.height(2.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Start
            ) {
                Icon(
                    imageVector = Icons.Default.Call,
                    contentDescription = null,
                    tint = Color(0xFF30D158),
                    modifier = Modifier.size(13.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = timerText,
                    color = Color(0xFF30D158),
                    fontSize = timerSize,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 0.5.sp,
                    maxLines = 1
                )
            }
        }

        Row(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            BouncyFilledTonalButton(
                onClick = {
                    IslandStateManager.onToggleCallMuteOrSpeaker?.invoke()
                },
                modifier = Modifier.size(circleButtonSize),
                shape = CircleShape,
                contentPadding = PaddingValues(0.dp),
                colors = ButtonDefaults.filledTonalButtonColors(
                    containerColor = if (activeCall.isMuted) Color.White else Color(0xFF2C2C2E),
                    contentColor = if (activeCall.isMuted) Color.Black else Color(0xFFF4F4F5)
                )
            ) {
                val secondaryIcon = when {
                    activeCall.isSpeaker -> Icons.Default.VolumeUp
                    activeCall.isMuted -> Icons.Default.MicOff
                    else -> Icons.Default.Mic
                }
                Icon(
                    imageVector = secondaryIcon,
                    contentDescription = if (activeCall.isSpeaker) "Speaker" else if (activeCall.isMuted) "Unmute" else "Mute",
                    modifier = Modifier.size(iconSize)
                )
            }

            BouncyFilledTonalButton(
                onClick = {
                    IslandStateManager.onEndCall?.invoke()
                    onCollapse()
                },
                modifier = Modifier.size(circleButtonSize),
                shape = CircleShape,
                contentPadding = PaddingValues(0.dp),
                colors = ButtonDefaults.filledTonalButtonColors(
                    containerColor = Color(0xFFFF453A),
                    contentColor = Color.White
                )
            ) {
                Icon(
                    imageVector = Icons.Default.CallEnd,
                    contentDescription = "End Call",
                    tint = Color.White,
                    modifier = Modifier.size(iconSize)
                )
            }
        }
    }
}

@Composable
fun ExpandedIdleView(onCollapse: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Droidbubble Island",
            color = Color.White,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "Ready for Media, Notifications & Recording",
            color = Color(0xFFA1A1AA),
            fontSize = 12.sp
        )
        Spacer(modifier = Modifier.height(12.dp))
        BouncyFilledTonalButton(
            onClick = onCollapse,
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.filledTonalButtonColors(
                containerColor = Color(0xFF2C2C2E),
                contentColor = Color.White
            )
        ) {
            Text("Collapse", fontSize = 12.sp)
        }
    }
}

@Composable
fun PulsingRecordingDot(modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition(label = "recording_pulse")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.35f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(650, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_alpha"
    )

    Box(
        modifier = modifier
            .size(11.dp)
            .scale(1f + (alpha * 0.12f))
            .clip(CircleShape)
            .background(Color(0xFFFF3B30).copy(alpha = alpha))
    )
}

@Composable
fun EqualizerVisualizer(isPlaying: Boolean, barColor: Color = Color(0xFFE5E5EA)) {
    if (!isPlaying) {
        // Zero CPU usage when paused: render static equalizer bars without triggering infinite frame recompositions
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(2.dp),
            modifier = Modifier.height(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .width(3.dp)
                    .height((16 * 0.3f).dp)
                    .clip(RoundedCornerShape(1.5.dp))
                    .background(barColor)
            )
            Box(
                modifier = Modifier
                    .width(3.dp)
                    .height((16 * 0.5f).dp)
                    .clip(RoundedCornerShape(1.5.dp))
                    .background(barColor)
            )
            Box(
                modifier = Modifier
                    .width(3.dp)
                    .height((16 * 0.2f).dp)
                    .clip(RoundedCornerShape(1.5.dp))
                    .background(barColor)
            )
        }
        return
    }

    val transition = rememberInfiniteTransition(label = "eq_bars")

    val h1 by transition.animateFloat(
        initialValue = 0.2f,
        targetValue = 0.9f,
        animationSpec = infiniteRepeatable(
            animation = tween(450, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "h1"
    )
    val h2 by transition.animateFloat(
        initialValue = 0.8f,
        targetValue = 0.3f,
        animationSpec = infiniteRepeatable(
            animation = tween(380, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "h2"
    )
    val h3 by transition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(520, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "h3"
    )

    val currentH1 = if (isPlaying) h1 else 0.3f
    val currentH2 = if (isPlaying) h2 else 0.5f
    val currentH3 = if (isPlaying) h3 else 0.2f

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(2.dp),
        modifier = Modifier.height(16.dp)
    ) {
        Box(
            modifier = Modifier
                .width(3.dp)
                .height((16 * currentH1).dp)
                .clip(RoundedCornerShape(1.5.dp))
                .background(barColor)
        )
        Box(
            modifier = Modifier
                .width(3.dp)
                .height((16 * currentH2).dp)
                .clip(RoundedCornerShape(1.5.dp))
                .background(barColor)
        )
        Box(
            modifier = Modifier
                .width(3.dp)
                .height((16 * currentH3).dp)
                .clip(RoundedCornerShape(1.5.dp))
                .background(barColor)
        )
    }
}

private fun formatDuration(millis: Long): String {
    if (millis <= 0L) return "0:00"
    val totalSeconds = (millis / 1000).coerceAtLeast(0)
    val hours = totalSeconds / 3600
    val minutes = (totalSeconds % 3600) / 60
    val seconds = totalSeconds % 60
    return if (hours > 0) {
        String.format(Locale.US, "%d:%02d:%02d", hours, minutes, seconds)
    } else {
        String.format(Locale.US, "%02d:%02d", minutes, seconds)
    }
}

@Composable
fun MediaBottomWaveOverlay(
    accentColor: Color,
    isPlaying: Boolean,
    alphaMultiplier: Float = 1f,
    modifier: Modifier = Modifier
) {
    val waveColor = if (accentColor != Color.Transparent) {
        lerp(accentColor, Color.White, 0.14f)
    } else {
        Color(0xFF64D2FF)
    }

    val ampFraction by animateFloatAsState(
        targetValue = if (isPlaying) 1f else 0.22f,
        animationSpec = spring(dampingRatio = 0.8f, stiffness = 200f),
        label = "waveAmpFraction"
    )

    val phase = if (isPlaying) {
        val transition = rememberInfiniteTransition(label = "media_bottom_wave")
        val p by transition.animateFloat(
            initialValue = 0f,
            targetValue = (2.0 * PI).toFloat(),
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 3600, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            ),
            label = "wavePhase"
        )
        p
    } else {
        0.85f
    }

    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        if (w <= 0f || h <= 0f || alphaMultiplier <= 0.01f) return@Canvas

        val steps = 40
        val stepX = w / steps

        fun buildSineWavePath(
            baseYFraction: Float,
            ampDp: Float,
            freq1: Float,
            phaseMul1: Float,
            freq2: Float,
            phaseMul2: Float
        ): Path {
            val path = Path()
            val baseY = h * baseYFraction
            val ampPx = ampDp.dp.toPx() * ampFraction
            path.moveTo(0f, h)
            for (i in 0..steps) {
                val x = i * stepX
                val normX = i.toFloat() / steps
                val angle1 = normX * freq1 * (2f * PI.toFloat()) + phase * phaseMul1
                val angle2 = normX * freq2 * (2f * PI.toPxOrFloat()) - phase * phaseMul2
                val y = baseY + sin(angle1) * ampPx + sin(angle2) * (ampPx * 0.45f)
                path.lineTo(x, y)
            }
            path.lineTo(w, h)
            path.close()
            return path
        }

        // Back atmospheric wave
        val backWave = buildSineWavePath(
            baseYFraction = 0.54f,
            ampDp = 8.5f,
            freq1 = 1.15f,
            phaseMul1 = 1.0f,
            freq2 = 2.1f,
            phaseMul2 = 1.0f
        )
        drawPath(
            path = backWave,
            brush = Brush.verticalGradient(
                colors = listOf(
                    waveColor.copy(alpha = 0.04f * alphaMultiplier),
                    waveColor.copy(alpha = 0.16f * alphaMultiplier),
                    waveColor.copy(alpha = 0.28f * alphaMultiplier)
                ),
                startY = h * 0.42f,
                endY = h
            )
        )

        // Mid harmonic wave
        val midWave = buildSineWavePath(
            baseYFraction = 0.65f,
            ampDp = 6.5f,
            freq1 = 1.45f,
            phaseMul1 = -1.0f,
            freq2 = 2.6f,
            phaseMul2 = -1.0f
        )
        drawPath(
            path = midWave,
            brush = Brush.verticalGradient(
                colors = listOf(
                    waveColor.copy(alpha = 0.08f * alphaMultiplier),
                    waveColor.copy(alpha = 0.24f * alphaMultiplier),
                    waveColor.copy(alpha = 0.38f * alphaMultiplier)
                ),
                startY = h * 0.54f,
                endY = h
            )
        )

        // Front crest wave with soft luminous rim
        val frontWave = Path()
        val frontCrest = Path()
        val frontBaseY = h * 0.76f
        val frontAmpPx = 5.0.dp.toPx() * ampFraction
        frontWave.moveTo(0f, h)
        for (i in 0..steps) {
            val x = i * stepX
            val normX = i.toFloat() / steps
            val angle1 = normX * 1.85f * (2f * PI.toFloat()) + phase * 2.0f + 1.1f
            val angle2 = normX * 0.9f * (2f * PI.toFloat()) - phase * 1.0f
            val y = frontBaseY + sin(angle1) * frontAmpPx + sin(angle2) * (frontAmpPx * 0.35f)
            frontWave.lineTo(x, y)
            if (i == 0) frontCrest.moveTo(x, y) else frontCrest.lineTo(x, y)
        }
        frontWave.lineTo(w, h)
        frontWave.close()

        drawPath(
            path = frontWave,
            brush = Brush.verticalGradient(
                colors = listOf(
                    waveColor.copy(alpha = 0.14f * alphaMultiplier),
                    waveColor.copy(alpha = 0.34f * alphaMultiplier),
                    waveColor.copy(alpha = 0.48f * alphaMultiplier)
                ),
                startY = h * 0.66f,
                endY = h
            )
        )

        drawPath(
            path = frontCrest,
            color = lerp(waveColor, Color.White, 0.38f).copy(alpha = 0.36f * alphaMultiplier * ampFraction),
            style = Stroke(width = 1.3.dp.toPx(), cap = StrokeCap.Round)
        )
    }
}

private fun Double.toPxOrFloat(): Float = this.toFloat()

@Composable
fun WavyMediaScrubber(
    progress: Float,
    enabled: Boolean,
    isPlaying: Boolean,
    activeColor: Color,
    inactiveColor: Color,
    onValueChange: (Float) -> Unit,
    onValueChangeFinished: () -> Unit,
    modifier: Modifier = Modifier
) {
    val currentOnValueChange by rememberUpdatedState(onValueChange)
    val currentOnValueChangeFinished by rememberUpdatedState(onValueChangeFinished)

    val waveAmpFraction by animateFloatAsState(
        targetValue = if (isPlaying && enabled) 1f else 0f,
        animationSpec = spring(dampingRatio = 0.78f, stiffness = 260f),
        label = "scrubberWaveAmp"
    )

    val phase = if (isPlaying && enabled) {
        val transition = rememberInfiniteTransition(label = "wavy_scrubber_phase")
        val p by transition.animateFloat(
            initialValue = 0f,
            targetValue = (2.0 * PI).toFloat(),
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 1250, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            ),
            label = "scrubberPhase"
        )
        p
    } else {
        0f
    }

    Canvas(
        modifier = modifier
            .then(
                if (enabled) {
                    Modifier.pointerInput(Unit) {
                        awaitEachGesture {
                            val down = awaitFirstDown(requireUnconsumed = false)
                            val w = size.width.toFloat()
                            if (w > 0f) {
                                val frac = (down.position.x / w).coerceIn(0f, 1f)
                                currentOnValueChange(frac)
                            }
                            while (true) {
                                val event = awaitPointerEvent()
                                val change = event.changes.firstOrNull() ?: break
                                if (!change.pressed) break
                                change.consume()
                                val currentW = size.width.toFloat()
                                if (currentW > 0f) {
                                    val frac = (change.position.x / currentW).coerceIn(0f, 1f)
                                    currentOnValueChange(frac)
                                }
                            }
                            currentOnValueChangeFinished()
                        }
                    }
                } else Modifier
            )
    ) {
        val w = size.width
        val h = size.height
        if (w <= 0f || h <= 0f) return@Canvas

        val centerY = h / 2f
        val clampedProgress = progress.coerceIn(0f, 1f)
        val thumbX = w * clampedProgress
        val strokeW = 3.2.dp.toPx()
        val wavelengthPx = 20.dp.toPx()
        val maxAmpPx = 2.7.dp.toPx() * waveAmpFraction

        // Inactive flat track from thumbX to w
        if (thumbX < w - 2.dp.toPx()) {
            drawLine(
                color = if (enabled) inactiveColor else inactiveColor.copy(alpha = 0.35f),
                start = Offset((thumbX + 4.dp.toPx()).coerceAtMost(w), centerY),
                end = Offset(w, centerY),
                strokeWidth = strokeW,
                cap = StrokeCap.Round
            )
        }

        // Active wavy (or flat when paused) track from 0 to thumbX
        if (thumbX > 1.dp.toPx()) {
            if (maxAmpPx < 0.15f) {
                drawLine(
                    color = if (enabled) activeColor else inactiveColor.copy(alpha = 0.5f),
                    start = Offset(0f, centerY),
                    end = Offset(thumbX, centerY),
                    strokeWidth = strokeW,
                    cap = StrokeCap.Round
                )
            } else {
                val wavePath = Path()
                val stepPx = 2.dp.toPx().coerceAtLeast(1.5f)
                var x = 0f
                var first = true
                while (x <= thumbX) {
                    // Taper wave amplitude near the ends so it meets the thumb and start cleanly
                    val distFromStart = (x / 8.dp.toPx()).coerceIn(0f, 1f)
                    val distFromThumb = ((thumbX - x) / 8.dp.toPx()).coerceIn(0f, 1f)
                    val envelope = distFromStart * distFromThumb
                    val angle = (x / wavelengthPx) * (2f * PI.toFloat()) - phase
                    val y = centerY + sin(angle) * maxAmpPx * envelope
                    if (first) {
                        wavePath.moveTo(x, y)
                        first = false
                    } else {
                        wavePath.lineTo(x, y)
                    }
                    x += stepPx
                }
                wavePath.lineTo(thumbX, centerY)
                drawPath(
                    path = wavePath,
                    color = activeColor,
                    style = Stroke(width = strokeW, cap = StrokeCap.Round)
                )
            }
        }

        // Vertical pill thumb
        if (enabled) {
            val thumbW = 3.2.dp.toPx()
            val thumbH = 14.dp.toPx()
            drawRoundRect(
                color = activeColor,
                topLeft = Offset(
                    x = (thumbX - thumbW / 2f).coerceIn(0f, (w - thumbW).coerceAtLeast(0f)),
                    y = centerY - thumbH / 2f
                ),
                size = androidx.compose.ui.geometry.Size(thumbW, thumbH),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(thumbW / 2f, thumbW / 2f)
            )
        }
    }
}

@Composable
fun CellularSignalBars(
    level: Int,
    modifier: Modifier = Modifier
) {
    val activeLevel = level.coerceIn(0, 4)
    Row(
        modifier = modifier.height(11.dp),
        verticalAlignment = Alignment.Bottom,
        horizontalArrangement = Arrangement.spacedBy(1.5.dp)
    ) {
        val barHeights = listOf(3.5.dp, 6.dp, 8.5.dp, 11.dp)
        for (i in 0 until 4) {
            val isLit = activeLevel > i || (activeLevel == 0 && i == 0)
            Box(
                modifier = Modifier
                    .width(2.5.dp)
                    .height(barHeights[i])
                    .clip(RoundedCornerShape(0.8.dp))
                    .background(if (isLit) Color.White else Color.White.copy(alpha = 0.25f))
            )
        }
    }
}

@Composable
fun ExpandedFpsView(
    fpsInfo: FpsInfo,
    onCollapse: () -> Unit,
    scale: Float = 1.0f
) {
    val context = LocalContext.current
    val isSmooth = fpsInfo.isSmooth
    val statusColor = if (isSmooth) Color(0xFF30D158) else Color(0xFFFFD60A)

    val iconSize = (32.dp * scale).coerceIn(26.dp, 38.dp)
    val iconGap = (12.dp * scale).coerceIn(8.dp, 16.dp)
    val numberSize = (24f * scale).coerceIn(20f, 28f).sp
    val subtitleSize = (13f * scale).coerceIn(11.5f, 15f).sp
    val circleButtonSize = (46.dp * scale).coerceIn(38.dp, 54.dp)
    val actionIconSize = (22.dp * scale).coerceIn(18.dp, 26.dp)

    Row(
        modifier = Modifier.fillMaxSize(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            modifier = Modifier
                .weight(1f)
                .padding(end = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Speed,
                contentDescription = "FPS",
                tint = statusColor,
                modifier = Modifier.size(iconSize)
            )

            Spacer(modifier = Modifier.width(iconGap))

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "${fpsInfo.fps}",
                    color = statusColor,
                    fontSize = numberSize,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp,
                    maxLines = 1
                )

                Text(
                    text = "FPS",
                    color = Color(0xFFA1A1AA),
                    fontSize = subtitleSize,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1
                )
            }
        }

        BouncyFilledTonalButton(
            onClick = {
                runCatching {
                    context.startActivity(Intent(android.provider.Settings.ACTION_DISPLAY_SETTINGS).apply {
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    })
                }
                onCollapse()
            },
            modifier = Modifier.size(circleButtonSize),
            shape = CircleShape,
            contentPadding = PaddingValues(0.dp),
            colors = ButtonDefaults.filledTonalButtonColors(
                containerColor = Color(0xFFE5E5EA),
                contentColor = Color.Black
            )
        ) {
            Icon(
                imageVector = Icons.Default.OpenInNew,
                contentDescription = "Display Settings",
                tint = Color.Black,
                modifier = Modifier.size(actionIconSize)
            )
        }
    }
}

@Composable
fun ExpandedWeatherView(
    weatherInfo: WeatherInfo,
    onCollapse: () -> Unit,
    scale: Float = 1.0f
) {
    val weatherIcon = when {
        weatherInfo.condition.contains("Sun", ignoreCase = true) || weatherInfo.condition.contains("Clear", ignoreCase = true) -> Icons.Default.WbSunny
        weatherInfo.condition.contains("Rain", ignoreCase = true) || weatherInfo.condition.contains("Drizzle", ignoreCase = true) || weatherInfo.condition.contains("Shower", ignoreCase = true) -> Icons.Default.WaterDrop
        weatherInfo.condition.contains("Snow", ignoreCase = true) -> Icons.Default.AcUnit
        weatherInfo.condition.contains("Thunder", ignoreCase = true) -> Icons.Default.Bolt
        else -> Icons.Default.Cloud
    }
    val iconTint = if (weatherIcon == Icons.Default.WbSunny) Color(0xFFFFD60A) else Color(0xFF64D2FF)

    val iconSize = (32.dp * scale).coerceIn(26.dp, 38.dp)
    val iconGap = (12.dp * scale).coerceIn(8.dp, 16.dp)
    val tempSize = (24f * scale).coerceIn(20f, 28f).sp
    val subtitleSize = (13f * scale).coerceIn(11.5f, 15f).sp
    val circleButtonSize = (46.dp * scale).coerceIn(38.dp, 54.dp)
    val actionIconSize = (22.dp * scale).coerceIn(18.dp, 26.dp)

    Row(
        modifier = Modifier.fillMaxSize(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            modifier = Modifier
                .weight(1f)
                .padding(end = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = weatherIcon,
                contentDescription = "Weather",
                tint = iconTint,
                modifier = Modifier.size(iconSize)
            )

            Spacer(modifier = Modifier.width(iconGap))

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "${weatherInfo.temperatureC}°C",
                    color = Color(0xFFFF9F0A),
                    fontSize = tempSize,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp,
                    maxLines = 1
                )

                Text(
                    text = "${weatherInfo.cityName.ifBlank { "Local Weather" }} • ${weatherInfo.condition.ifBlank { "Clear" }}",
                    color = Color(0xFFA1A1AA),
                    fontSize = subtitleSize,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.basicMarquee(iterations = Int.MAX_VALUE)
                )
            }
        }

        BouncyFilledTonalButton(
            onClick = {
                IslandStateManager.onRefreshWeather?.invoke()
            },
            modifier = Modifier.size(circleButtonSize),
            shape = CircleShape,
            contentPadding = PaddingValues(0.dp),
            colors = ButtonDefaults.filledTonalButtonColors(
                containerColor = Color(0xFF2C2C2E),
                contentColor = Color(0xFFF4F4F5)
            )
        ) {
            Icon(
                imageVector = Icons.Default.Refresh,
                contentDescription = "Refresh Weather",
                modifier = Modifier.size(actionIconSize)
            )
        }
    }
}

@Composable
fun ExpandedNetworkSpeedView(
    networkSpeedInfo: NetworkSpeedInfo,
    onCollapse: () -> Unit,
    scale: Float = 1.0f
) {
    val context = LocalContext.current
    val iconSize = (32.dp * scale).coerceIn(26.dp, 38.dp)
    val iconGap = (12.dp * scale).coerceIn(8.dp, 16.dp)
    val speedSize = (22f * scale).coerceIn(18.5f, 26f).sp
    val subtitleSize = (13f * scale).coerceIn(11.5f, 15f).sp
    val circleButtonSize = (46.dp * scale).coerceIn(38.dp, 54.dp)
    val actionIconSize = (22.dp * scale).coerceIn(18.dp, 26.dp)
    val cyanAccent = Color(0xFF64D2FF)

    Row(
        modifier = Modifier.fillMaxSize(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            modifier = Modifier
                .weight(1f)
                .padding(end = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = if (networkSpeedInfo.isWifi) Icons.Default.Wifi else Icons.Default.SignalCellularAlt,
                contentDescription = "Network",
                tint = cyanAccent,
                modifier = Modifier.size(iconSize)
            )

            Spacer(modifier = Modifier.width(iconGap))

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "↓ ${networkSpeedInfo.downloadSpeedFormatted}",
                    color = cyanAccent,
                    fontSize = speedSize,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1
                )

                Text(
                    text = "↑ ${networkSpeedInfo.uploadSpeedFormatted} • ${networkSpeedInfo.networkLabel.ifBlank { "Network" }}",
                    color = Color(0xFFA1A1AA),
                    fontSize = subtitleSize,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        BouncyFilledTonalButton(
            onClick = {
                runCatching {
                    context.startActivity(Intent(android.provider.Settings.ACTION_WIFI_SETTINGS).apply {
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    })
                }
                onCollapse()
            },
            modifier = Modifier.size(circleButtonSize),
            shape = CircleShape,
            contentPadding = PaddingValues(0.dp),
            colors = ButtonDefaults.filledTonalButtonColors(
                containerColor = Color(0xFFE5E5EA),
                contentColor = Color.Black
            )
        ) {
            Icon(
                imageVector = Icons.Default.OpenInNew,
                contentDescription = "Wi-Fi Settings",
                tint = Color.Black,
                modifier = Modifier.size(actionIconSize)
            )
        }
    }
}

