package com.droidbubble.app.droidbar

import android.content.Context
import android.content.Intent
import android.view.HapticFeedbackConstants
import androidx.compose.animation.core.animateDpAsState
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.coerceAtLeast
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

@Composable
fun DroidbarOverlay(
    context: Context,
    onExpandChanged: (Boolean) -> Unit
) {
    val view = LocalView.current
    var isExpanded by remember { mutableStateOf(false) }
    var isWindowExpanded by remember { mutableStateOf(false) }
    var apps by remember { mutableStateOf<List<BarAppInfo>>(emptyList()) }

    // Initial load of apps
    LaunchedEffect(Unit) {
        val loadedApps = withContext(Dispatchers.IO) {
            MostUsedAppsHelper.getMostUsedApps(context, limit = 6)
        }
        apps = loadedApps
    }

    // Handle expand/collapse lifecycle and window resizing
    LaunchedEffect(isExpanded) {
        if (isExpanded) {
            isWindowExpanded = true
            onExpandChanged(true)
            val loadedApps = withContext(Dispatchers.IO) {
                MostUsedAppsHelper.getMostUsedApps(context, limit = 6)
            }
            apps = loadedApps
        } else {
            // Wait for collapse spring animation to complete before resizing window bounds
            delay(380)
            isWindowExpanded = false
            onExpandChanged(false)
        }
    }

    // Spring animations with Island-like subtle bounce on expand, smooth settle on collapse
    val bounceAnimSpecDp = spring<androidx.compose.ui.unit.Dp>(
        dampingRatio = if (isExpanded) 0.68f else 1.0f,
        stiffness = if (isExpanded) 340f else 450f
    )

    val targetWidth = if (isExpanded) 68.dp else 5.dp
    val targetHeight = if (isExpanded) {
        val appCount = if (apps.isNotEmpty()) apps.size else 6
        (appCount * 56 + 28).dp
    } else {
        44.dp
    }
    val targetCornerRadius = if (isExpanded) 26.dp else 3.dp
    val targetEndMargin = if (isExpanded) 8.dp else 0.dp

    val animatedWidth by animateDpAsState(targetValue = targetWidth, animationSpec = bounceAnimSpecDp, label = "barWidth")
    val animatedHeight by animateDpAsState(targetValue = targetHeight, animationSpec = bounceAnimSpecDp, label = "barHeight")
    val animatedCornerRadius by animateDpAsState(targetValue = targetCornerRadius, animationSpec = bounceAnimSpecDp, label = "barRadius")
    val animatedEndMargin by animateDpAsState(targetValue = targetEndMargin, animationSpec = bounceAnimSpecDp, label = "barMargin")

    val safeEndMargin = animatedEndMargin.coerceAtLeast(0.dp)
    val safeWidth = animatedWidth.coerceAtLeast(4.dp)
    val safeHeight = animatedHeight.coerceAtLeast(40.dp)
    val safeRadius = animatedCornerRadius.coerceAtLeast(0.dp)

    // Content fade & scale
    val contentProgress by animateFloatAsState(
        targetValue = if (isExpanded) 1f else 0f,
        animationSpec = if (isExpanded) tween(240, delayMillis = 60) else tween(160),
        label = "contentAlpha"
    )

    // Backdrop dim fade
    val backdropAlpha by animateFloatAsState(
        targetValue = if (isExpanded) 0.30f else 0f,
        animationSpec = tween(durationMillis = if (isExpanded) 250 else 200),
        label = "backdropAlpha"
    )

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
        // The Droidbar surface: fully opaque black, NO outline, expanding with subtle bounce
        Box(
            modifier = Modifier
                .padding(end = safeEndMargin)
                .width(safeWidth)
                .height(safeHeight)
                .shadow(
                    elevation = if (isExpanded) 12.dp else 4.dp,
                    shape = RoundedCornerShape(
                        topStart = safeRadius,
                        bottomStart = safeRadius,
                        topEnd = if (isExpanded) safeRadius else 0.dp,
                        bottomEnd = if (isExpanded) safeRadius else 0.dp
                    )
                )
                .clip(
                    RoundedCornerShape(
                        topStart = safeRadius,
                        bottomStart = safeRadius,
                        topEnd = if (isExpanded) safeRadius else 0.dp,
                        bottomEnd = if (isExpanded) safeRadius else 0.dp
                    )
                )
                .background(Color.Black) // Fully opaque black, no outline
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
            if (!isExpanded && contentProgress < 0.1f) {
                // Subtle white edge indicator stripe inside the black edge bar for visibility on all wallpapers
                Box(
                    modifier = Modifier
                        .width(2.5.dp)
                        .height(26.dp)
                        .clip(RoundedCornerShape(1.5.dp))
                        .background(Color.White.copy(alpha = 0.70f))
                )
            } else if (contentProgress > 0.05f) {
                // Expanded apps list
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(vertical = 14.dp, horizontal = 10.dp)
                        .graphicsLayer {
                            alpha = contentProgress
                            scaleX = 0.85f + (0.15f * contentProgress)
                            scaleY = 0.85f + (0.15f * contentProgress)
                        },
                    verticalArrangement = Arrangement.SpaceEvenly,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    apps.forEach { app ->
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .clickable {
                                    view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                                    val launchIntent = context.packageManager.getLaunchIntentForPackage(app.packageName)?.apply {
                                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                    }
                                    if (launchIntent != null) {
                                        try {
                                            context.startActivity(launchIntent)
                                        } catch (_: Exception) {}
                                    }
                                    isExpanded = false
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            if (app.iconBitmap != null) {
                                Image(
                                    bitmap = app.iconBitmap.asImageBitmap(),
                                    contentDescription = app.label,
                                    modifier = Modifier.size(40.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
