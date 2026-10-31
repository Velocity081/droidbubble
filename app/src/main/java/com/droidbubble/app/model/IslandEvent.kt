package com.droidbubble.app.model

import android.app.PendingIntent
import android.graphics.Bitmap

data class MediaInfo(
    val packageName: String,
    val title: String,
    val artist: String,
    val albumArt: Bitmap? = null,
    val appIcon: Bitmap? = null,
    val sessionActivity: PendingIntent? = null,
    val isPlaying: Boolean = false,
    val durationMs: Long = 0L,
    val positionMs: Long = 0L,
    val playbackSpeed: Float = 1f,
    val lastPositionUpdateTimeMs: Long = 0L,
    val canLike: Boolean = false,
    val isLiked: Boolean = false
) {
    fun computeCurrentPosition(nowMs: Long = runCatching { android.os.SystemClock.elapsedRealtime() }.getOrDefault(0L)): Long {
        var pos = positionMs
        if (isPlaying && lastPositionUpdateTimeMs > 0L && nowMs > 0L) {
            val delta = (nowMs - lastPositionUpdateTimeMs).coerceAtLeast(0L)
            pos += (delta * playbackSpeed).toLong()
        }
        return if (durationMs > 0L) pos.coerceIn(0L, durationMs) else pos.coerceAtLeast(0L)
    }
}

data class NotificationAction(
    val title: String,
    val actionIntent: PendingIntent?,
    val remoteInputKey: String? = null,
    val remoteInputLabel: String? = null,
    val remoteInputChoices: List<String> = emptyList()
) {
    val isReply: Boolean get() = remoteInputKey != null
}

data class NotificationInfo(
    val key: String,
    val packageName: String,
    val appName: String,
    val title: String,
    val text: String,
    val iconBitmap: Bitmap? = null,
    val contentIntent: PendingIntent? = null,
    val actions: List<NotificationAction> = emptyList(),
    val timestamp: Long = System.currentTimeMillis(),
    val progress: Int = -1,
    val maxProgress: Int = 0,
    val isIndeterminate: Boolean = false,
    val isOngoing: Boolean = false
) {
    val hasProgress: Boolean
        get() = maxProgress > 0 || isIndeterminate || (progress >= 0 && maxProgress > 0)

    val progressFraction: Float
        get() = if (maxProgress > 0 && progress >= 0) (progress.toFloat() / maxProgress).coerceIn(0f, 1f) else 0f
}

data class ScreenRecordingState(
    val isRecording: Boolean = false,
    val durationSeconds: Long = 0L,
    val isPaused: Boolean = false
)

data class ActiveCallState(
    val isActive: Boolean = false,
    val callerName: String = "",
    val packageName: String = "",
    val durationSeconds: Long = 0L,
    val isMuted: Boolean = false,
    val isSpeaker: Boolean = false,
    val contentIntent: PendingIntent? = null
)

enum class IslandPriority {
    CALL,
    SCREEN_RECORDING,
    PROGRESS,
    POP_NOTIFICATION,
    MEDIA,
    FLASHLIGHT,
    CHARGING,
    FPS,
    WEATHER,
    NETWORK_SPEED,
    IDLE
}
