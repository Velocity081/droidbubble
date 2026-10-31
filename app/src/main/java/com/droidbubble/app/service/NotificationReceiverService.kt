package com.droidbubble.app.service

import android.app.ActivityOptions
import android.app.Notification
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.graphics.drawable.Icon
import android.media.MediaMetadata
import android.media.session.MediaController
import android.media.session.MediaSessionManager
import android.media.session.PlaybackState
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import com.droidbubble.app.data.SettingsRepository
import com.droidbubble.app.island.IslandStateManager
import com.droidbubble.app.model.ActiveCallState
import com.droidbubble.app.model.IslandSettings
import com.droidbubble.app.model.MediaInfo
import com.droidbubble.app.model.NotificationAction
import com.droidbubble.app.model.NotificationInfo
import com.droidbubble.app.model.ScreenRecordingState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class NotificationReceiverService : NotificationListenerService() {

    companion object {
        var instance: NotificationReceiverService? = null
            private set
    }

    private val serviceScope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private lateinit var settingsRepository: SettingsRepository
    private var currentSettings = IslandSettings()

    private var mediaSessionManager: MediaSessionManager? = null
    private var activeMediaController: MediaController? = null
    private val controllerCallbacks = mutableMapOf<MediaController, MediaController.Callback>()
    private val userLikedTracks = java.util.concurrent.ConcurrentHashMap<String, Boolean>()

    private val sessionsChangedListener = MediaSessionManager.OnActiveSessionsChangedListener { controllers ->
        refreshMedia()
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
        settingsRepository = SettingsRepository(this)
        serviceScope.launch {
            settingsRepository.settingsFlow.collectLatest {
                currentSettings = it
                IslandStateManager.dismissMediaOnPause = it.dismissMediaOnPause
                refreshMedia()
            }
        }

        IslandStateManager.onPauseMedia = {
            activeMediaController?.transportControls?.pause()
        }

        IslandStateManager.onPlayPause = {
            activeMediaController?.let { controller ->
                val state = controller.playbackState?.state
                if (state == PlaybackState.STATE_PLAYING) {
                    controller.transportControls.pause()
                } else {
                    controller.transportControls.play()
                }
            }
        }

        IslandStateManager.onSkipNext = {
            activeMediaController?.transportControls?.skipToNext()
        }

        IslandStateManager.onSkipPrevious = {
            activeMediaController?.transportControls?.skipToPrevious()
        }

        IslandStateManager.onToggleLike = {
            val controller = activeMediaController
            val notif = activeNotifications?.firstOrNull { it.packageName == controller?.packageName }
            var handled = false

            // 1. Try CustomActions on playbackState
            val customActions = controller?.playbackState?.customActions
            val likeAction = customActions?.firstOrNull { ca ->
                val a = ca.action.lowercase()
                val n = ca.name?.toString()?.lowercase() ?: ""
                a.contains("like") || a.contains("favorite") || a.contains("thumb") ||
                n.contains("like") || n.contains("favorite") || n.contains("thumb")
            }
            if (likeAction != null && controller != null) {
                try {
                    controller.transportControls.sendCustomAction(likeAction.action, likeAction.extras)
                    handled = true
                } catch (e: Exception) {
                    Log.e("NotificationReceiver", "Error sending custom action like", e)
                }
            }

            // 2. Try Notification Actions
            if (!handled && notif?.notification?.actions != null) {
                val notifLikeAction = notif.notification.actions.firstOrNull { action ->
                    val title = action.title?.toString()?.lowercase() ?: ""
                    title.contains("like") || title.contains("favorite") || title.contains("thumb")
                }
                if (notifLikeAction?.actionIntent != null) {
                    try {
                        notifLikeAction.actionIntent.send()
                        handled = true
                    } catch (e: Exception) {
                        Log.e("NotificationReceiver", "Error sending notif like action", e)
                    }
                }
            }

            // 3. Try TransportControls.setRating
            if (!handled && controller != null) {
                val currentMedia = IslandStateManager.activeMedia.value
                val newLiked = !(currentMedia?.isLiked ?: false)
                try {
                    val rating = android.media.Rating.newHeartRating(newLiked)
                    controller.transportControls.setRating(rating)
                    handled = true
                } catch (e: Exception) {
                    try {
                        val thumb = android.media.Rating.newThumbRating(newLiked)
                        controller.transportControls.setRating(thumb)
                        handled = true
                    } catch (e2: Exception) {
                        Log.e("NotificationReceiver", "Error setting rating", e2)
                    }
                }
            }

            val current = IslandStateManager.activeMedia.value
            if (current != null) {
                val newLiked = !current.isLiked
                val trackKey = "${current.packageName}::${current.title}"
                userLikedTracks[trackKey] = newLiked
                IslandStateManager.updateMedia(current.copy(isLiked = newLiked))
            }
        }

        IslandStateManager.onSeekTo = { pos ->
            try {
                activeMediaController?.transportControls?.seekTo(pos)
            } catch (e: Exception) {
                Log.e("NotificationReceiver", "Error seeking", e)
            }
            val current = IslandStateManager.activeMedia.value
            if (current != null) {
                IslandStateManager.updateMedia(
                    current.copy(
                        positionMs = pos,
                        lastPositionUpdateTimeMs = android.os.SystemClock.elapsedRealtime()
                    )
                )
            }
        }

        IslandStateManager.onDismissNotification = { key ->
            // Intentionally no-op: dismissing only dismisses the island card, not the system notification itself
        }

        IslandStateManager.onSendReply = { key, replyText ->
            val notif = IslandStateManager.activeNotification.value
            if (notif != null && notif.key == key) {
                val replyAction = notif.actions.firstOrNull { it.isReply }
                if (replyAction?.actionIntent != null && replyAction.remoteInputKey != null) {
                    val intent = Intent()
                    val bundle = Bundle()
                    bundle.putCharSequence(replyAction.remoteInputKey, replyText)
                    val remoteInput = android.app.RemoteInput.Builder(replyAction.remoteInputKey).build()
                    android.app.RemoteInput.addResultsToIntent(arrayOf(remoteInput), intent, bundle)
                    try {
                        val options = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                            ActivityOptions.makeBasic().apply {
                                pendingIntentBackgroundActivityStartMode = ActivityOptions.MODE_BACKGROUND_ACTIVITY_START_ALLOWED
                            }.toBundle()
                        } else null
                        if (options != null) {
                            replyAction.actionIntent.send(this@NotificationReceiverService, 0, intent, null, null, null, options)
                        } else {
                            replyAction.actionIntent.send(this@NotificationReceiverService, 0, intent)
                        }
                    } catch (e: Exception) {
                        Log.e("NotificationReceiver", "Error sending reply", e)
                    }
                }
            }
        }
    }

    override fun onListenerConnected() {
        super.onListenerConnected()
        try {
            mediaSessionManager = getSystemService(Context.MEDIA_SESSION_SERVICE) as? MediaSessionManager
            val componentName = ComponentName(this, NotificationReceiverService::class.java)
            mediaSessionManager?.addOnActiveSessionsChangedListener(sessionsChangedListener, componentName)
            val initialControllers = mediaSessionManager?.getActiveSessions(componentName)
            updateActiveMediaController(initialControllers)
        } catch (e: Exception) {
            Log.e("NotificationReceiver", "Error registering media session listener", e)
        }
    }

    override fun onListenerDisconnected() {
        super.onListenerDisconnected()
        try {
            mediaSessionManager?.removeOnActiveSessionsChangedListener(sessionsChangedListener)
            for ((ctrl, cb) in controllerCallbacks) {
                runCatching { ctrl.unregisterCallback(cb) }
            }
            controllerCallbacks.clear()
            activeMediaController = null
        } catch (e: Exception) {
            Log.e("NotificationReceiver", "Error unregistering listener", e)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (instance === this) {
            instance = null
        }
        for ((ctrl, cb) in controllerCallbacks) {
            runCatching { ctrl.unregisterCallback(cb) }
        }
        controllerCallbacks.clear()
        activeMediaController = null
        recordingTimerJob?.cancel()
        callTimerJob?.cancel()
        serviceScope.cancel()
    }

    fun refreshMedia(candidateController: MediaController? = null) {
        try {
            val componentName = ComponentName(this, NotificationReceiverService::class.java)
            val controllers = mediaSessionManager?.getActiveSessions(componentName)?.toMutableList() ?: mutableListOf()
            if (candidateController != null && controllers.none { it.packageName == candidateController.packageName }) {
                controllers.add(candidateController)
            }
            syncControllers(controllers)
            updateActiveMediaController(controllers)
        } catch (_: Exception) {
            updateMediaInfo()
        }
    }

    private fun syncControllers(controllers: List<MediaController>) {
        val toRemove = controllerCallbacks.keys.filter { existing ->
            controllers.none { it.packageName == existing.packageName }
        }
        for (old in toRemove) {
            val cb = controllerCallbacks.remove(old)
            if (cb != null) {
                runCatching { old.unregisterCallback(cb) }
            }
        }

        for (controller in controllers) {
            val already = controllerCallbacks.keys.any { it.packageName == controller.packageName }
            if (!already) {
                val cb = object : MediaController.Callback() {
                    override fun onPlaybackStateChanged(state: PlaybackState?) {
                        refreshMedia()
                    }

                    override fun onMetadataChanged(metadata: MediaMetadata?) {
                        refreshMedia()
                    }

                    override fun onSessionDestroyed() {
                        refreshMedia()
                    }
                }
                runCatching { controller.registerCallback(cb) }
                controllerCallbacks[controller] = cb
            }
        }
    }

    private fun isControllerPlayingOrBuffering(controller: MediaController): Boolean {
        val state = controller.playbackState?.state ?: return false
        return state == PlaybackState.STATE_PLAYING ||
                state == PlaybackState.STATE_BUFFERING ||
                state == PlaybackState.STATE_FAST_FORWARDING ||
                state == PlaybackState.STATE_REWINDING
    }

    private fun getControllerTitle(controller: MediaController): String? {
        val meta = controller.metadata
        val rawTitle = meta?.getString(MediaMetadata.METADATA_KEY_TITLE)?.takeIf { it.isNotBlank() }
            ?: meta?.getString(MediaMetadata.METADATA_KEY_DISPLAY_TITLE)?.takeIf { it.isNotBlank() }
            ?: meta?.getString(MediaMetadata.METADATA_KEY_DISPLAY_DESCRIPTION)?.takeIf { it.isNotBlank() }
            ?: meta?.getString(MediaMetadata.METADATA_KEY_DISPLAY_SUBTITLE)?.takeIf { it.isNotBlank() }
            ?: meta?.getString(MediaMetadata.METADATA_KEY_ALBUM)?.takeIf { it.isNotBlank() }
        if (!rawTitle.isNullOrBlank()) return rawTitle

        val notif = activeNotifications?.firstOrNull { it.packageName == controller.packageName }
        val notifTitle = notif?.notification?.extras?.getCharSequence(Notification.EXTRA_TITLE)?.toString()?.takeIf { it.isNotBlank() }
            ?: notif?.notification?.extras?.getCharSequence(Notification.EXTRA_TITLE_BIG)?.toString()?.takeIf { it.isNotBlank() }
        return notifTitle
    }

    private fun findBestController(controllers: List<MediaController>?): MediaController? {
        if (controllers.isNullOrEmpty()) return null

        if (currentSettings.dismissMediaOnPause) {
            val playingWithMeta = controllers.firstOrNull {
                isControllerPlayingOrBuffering(it) && !getControllerTitle(it).isNullOrBlank()
            }
            if (playingWithMeta != null) return playingWithMeta
            return controllers.firstOrNull { isControllerPlayingOrBuffering(it) }
        }

        // 1. Prioritize any controller that is currently actively playing/buffering with valid title
        val playingWithMeta = controllers.firstOrNull {
            isControllerPlayingOrBuffering(it) && !getControllerTitle(it).isNullOrBlank()
        }
        if (playingWithMeta != null) return playingWithMeta

        // 2. If current active controller is still in the list and playing/buffering, keep it!
        val current = activeMediaController
        if (current != null) {
            val stillPresent = controllers.firstOrNull { it.packageName == current.packageName }
            if (stillPresent != null && isControllerPlayingOrBuffering(stillPresent)) {
                return stillPresent
            }
        }

        // 3. Current active controller if still present and has a valid title (paused)
        if (current != null) {
            val stillPresent = controllers.firstOrNull { it.packageName == current.packageName }
            if (stillPresent != null && !getControllerTitle(stillPresent).isNullOrBlank()) {
                return stillPresent
            }
        }

        // 4. Any controller with valid title (e.g. paused music player like Spotify, YouTube)
        val withMeta = controllers.firstOrNull {
            !getControllerTitle(it).isNullOrBlank()
        }
        if (withMeta != null) return withMeta

        // 5. Any controller that is currently actively playing/buffering (even without title yet)
        val playing = controllers.firstOrNull {
            isControllerPlayingOrBuffering(it)
        }
        if (playing != null) return playing

        // 6. Keep current controller if still in list
        if (current != null) {
            val stillPresent = controllers.firstOrNull { it.packageName == current.packageName }
            if (stillPresent != null) {
                return stillPresent
            }
        }

        return controllers.firstOrNull()
    }

    private fun updateActiveMediaController(controllers: List<MediaController>?) {
        val best = findBestController(controllers)
        if (best == null) {
            activeMediaController = null
            IslandStateManager.updateMedia(null)
            return
        }

        activeMediaController = best
        updateMediaInfo()
    }

    private fun updateMediaInfo() {
        if (!currentSettings.showMedia) {
            IslandStateManager.updateMedia(null)
            return
        }

        val controller = activeMediaController
        if (controller == null) {
            IslandStateManager.updateMedia(null)
            return
        }

        val metadata = controller.metadata
        val playbackState = controller.playbackState

        if (metadata == null && playbackState == null) {
            IslandStateManager.updateMedia(null)
            return
        }

        val isPlaying = isControllerPlayingOrBuffering(controller)
        if (currentSettings.dismissMediaOnPause && !isPlaying) {
            IslandStateManager.updateMedia(null)
            return
        }
        val title = getControllerTitle(controller) ?: if (isPlaying) "Playing Audio" else null
        if (title == null) {
            IslandStateManager.updateMedia(null)
            return
        }

        val rawArtist = metadata?.getString(MediaMetadata.METADATA_KEY_ARTIST)?.takeIf { it.isNotBlank() }
            ?: metadata?.getString(MediaMetadata.METADATA_KEY_ALBUM_ARTIST)?.takeIf { it.isNotBlank() }
            ?: metadata?.getString(MediaMetadata.METADATA_KEY_AUTHOR)?.takeIf { it.isNotBlank() }
            ?: metadata?.getString(MediaMetadata.METADATA_KEY_WRITER)?.takeIf { it.isNotBlank() }
            ?: metadata?.getString(MediaMetadata.METADATA_KEY_COMPOSER)?.takeIf { it.isNotBlank() }
            ?: metadata?.getString(MediaMetadata.METADATA_KEY_DISPLAY_SUBTITLE)?.takeIf { it.isNotBlank() }

        val notif = activeNotifications?.firstOrNull { it.packageName == controller.packageName }
        val notifArtist = notif?.notification?.extras?.getCharSequence(Notification.EXTRA_TEXT)?.toString()?.takeIf { it.isNotBlank() }
            ?: notif?.notification?.extras?.getCharSequence(Notification.EXTRA_BIG_TEXT)?.toString()?.takeIf { it.isNotBlank() }
            ?: notif?.notification?.extras?.getCharSequence(Notification.EXTRA_SUB_TEXT)?.toString()?.takeIf { it.isNotBlank() }

        val artist = rawArtist ?: notifArtist ?: try {
            val pm = packageManager
            val appInfo = pm.getApplicationInfo(controller.packageName, 0)
            pm.getApplicationLabel(appInfo).toString()
        } catch (_: Exception) {
            controller.packageName
        }

        var artBitmap = metadata?.getBitmap(MediaMetadata.METADATA_KEY_ALBUM_ART)
            ?: metadata?.getBitmap(MediaMetadata.METADATA_KEY_ART)
            ?: metadata?.getBitmap(MediaMetadata.METADATA_KEY_DISPLAY_ICON)

        // If bitmap is not directly available, check if artwork URI is provided
        if (artBitmap == null) {
            val uriStr = metadata?.getString(MediaMetadata.METADATA_KEY_ALBUM_ART_URI)
                ?: metadata?.getString(MediaMetadata.METADATA_KEY_ART_URI)
                ?: metadata?.getString(MediaMetadata.METADATA_KEY_DISPLAY_ICON_URI)
            if (!uriStr.isNullOrBlank()) {
                artBitmap = runCatching {
                    val uri = Uri.parse(uriStr)
                    contentResolver.openInputStream(uri)?.use { stream ->
                        BitmapFactory.decodeStream(stream)
                    }
                }.getOrNull()
            }
        }

        // Also check if an active notification from this app provides large icon art
        if (artBitmap == null) {
            artBitmap = runCatching {
                val notifObj = notif?.notification
                notifObj?.getLargeIcon()?.loadDrawable(this)?.let { drawableToBitmap(it) }
                    ?: (notifObj?.extras?.getParcelable(Notification.EXTRA_LARGE_ICON) as? Bitmap)
            }.getOrNull()
        }

        val appIconBitmap = getAppIconBitmap(controller.packageName)

        var duration = metadata?.getLong(MediaMetadata.METADATA_KEY_DURATION) ?: 0L
        if (duration <= 0L && notif != null) {
            val maxProgress = notif.notification.extras?.getInt(Notification.EXTRA_PROGRESS_MAX) ?: 0
            val isIndeterminate = notif.notification.extras?.getBoolean(Notification.EXTRA_PROGRESS_INDETERMINATE, false) ?: false
            if (!isIndeterminate && maxProgress > 0) {
                duration = if (maxProgress > 1000) maxProgress.toLong() else maxProgress * 1000L
            }
        }

        val position = playbackState?.position ?: 0L
        val speed = playbackState?.playbackSpeed?.takeIf { it > 0f } ?: 1f
        val lastUpdate = if (playbackState != null && playbackState.lastPositionUpdateTime > 0L) {
            playbackState.lastPositionUpdateTime
        } else {
            android.os.SystemClock.elapsedRealtime()
        }

        // Determine like capability
        val actions = playbackState?.actions ?: 0L
        val supportsRating = (actions and PlaybackState.ACTION_SET_RATING) != 0L
        val hasCustomLike = playbackState?.customActions?.any { ca ->
            val a = ca.action.lowercase()
            val n = ca.name?.toString()?.lowercase() ?: ""
            a.contains("like") || a.contains("favorite") || a.contains("thumb") ||
            n.contains("like") || n.contains("favorite") || n.contains("thumb")
        } ?: false
        val hasNotifLike = notif?.notification?.actions?.any { action ->
            val t = action.title?.toString()?.lowercase() ?: ""
            t.contains("like") || t.contains("favorite") || t.contains("thumb")
        } ?: false

        val isKnownMusicApp = controller.packageName in setOf(
            "com.google.android.apps.youtube.music",
            "com.spotify.music",
            "com.soundcloud.android",
            "com.apple.android.music",
            "com.amazon.mp3",
            "deezer.android.app",
            "com.aspiro.tidal"
        )

        val canLike = supportsRating || hasCustomLike || hasNotifLike || isKnownMusicApp

        val trackKey = "${controller.packageName}::$title"
        val userExplicitLike = userLikedTracks[trackKey]

        val userRating = metadata?.getRating(MediaMetadata.METADATA_KEY_USER_RATING)
        val isLikedFromRating = if (userRating != null && userRating.isRated) {
            userRating.hasHeart() || userRating.isThumbUp
        } else false

        val isLikedFromAction = playbackState?.customActions?.any { ca ->
            val a = ca.action.lowercase()
            val n = ca.name?.toString()?.lowercase() ?: ""
            (a.contains("unlike") || n.contains("unlike") || a.contains("remove_like") || n.contains("remove_like") || a.contains("remove_favorite")) &&
            !a.contains("like /") && !n.contains("like /")
        } ?: notif?.notification?.actions?.any { action ->
            val t = action.title?.toString()?.lowercase() ?: ""
            (t.contains("unlike") || t.contains("remove like") || t.contains("remove from favorites")) && !t.contains("like /")
        } ?: false

        val isLiked = userExplicitLike ?: (isLikedFromRating || isLikedFromAction)

        IslandStateManager.updateMedia(
            MediaInfo(
                packageName = controller.packageName,
                title = title,
                artist = artist,
                albumArt = artBitmap,
                appIcon = appIconBitmap,
                sessionActivity = controller.sessionActivity,
                isPlaying = isPlaying,
                durationMs = duration,
                positionMs = position,
                playbackSpeed = speed,
                lastPositionUpdateTimeMs = lastUpdate,
                canLike = canLike,
                isLiked = isLiked
            )
        )
    }

    private var systemRecordingKey: String? = null
    private var recordingTimerJob: Job? = null
    private var recordingStartTimeMs: Long = 0L
    private var recordingPausedAtMs: Long = 0L
    private var activeStopAction: Notification.Action? = null
    private var activePauseAction: Notification.Action? = null
    private var activeResumeAction: Notification.Action? = null

    private var systemCallKey: String? = null
    private var callTimerJob: Job? = null
    private var callStartTimeMs: Long = 0L
    private var activeEndCallAction: Notification.Action? = null
    private var activeMuteOrSpeakerAction: Notification.Action? = null

    private fun isOngoingCallNotification(sbn: StatusBarNotification, notification: Notification): Boolean {
        val isOngoing = sbn.isOngoing ||
                (notification.flags and Notification.FLAG_ONGOING_EVENT) != 0 ||
                (notification.flags and Notification.FLAG_NO_CLEAR) != 0
        if (!isOngoing) return false

        val extras = notification.extras ?: return false
        val template = extras.getString(Notification.EXTRA_TEMPLATE) ?: ""
        val isCallCategory = notification.category == Notification.CATEGORY_CALL || template.contains("CallStyle")

        val actions = notification.actions
        val actionTitles = actions?.mapNotNull { it?.title?.toString()?.lowercase(java.util.Locale.ROOT) } ?: emptyList()

        val hasAnswerOrDecline = actionTitles.any {
            it.contains("answer") || it.contains("accept") || it.contains("decline") || it.contains("reject")
        }
        if (hasAnswerOrDecline) {
            // Incoming (ringing) call -> user opted for active/ongoing calls only
            return false
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val callType = extras.getInt("android.callType", -1)
            if (callType == Notification.CallStyle.CALL_TYPE_INCOMING || callType == Notification.CallStyle.CALL_TYPE_SCREENING) {
                return false
            }
            if (callType == Notification.CallStyle.CALL_TYPE_ONGOING) {
                return true
            }
        }

        val hasHangupAction = actionTitles.any {
            it.contains("hang up") || it.contains("hangup") || it.contains("end call") || it == "end" || it.contains("leave") || it.contains("disconnect")
        }

        val pkg = sbn.packageName.lowercase(java.util.Locale.ROOT)
        val isDialerPkg = pkg.contains("dialer") || pkg.contains("incallui") || pkg.contains("telecom") || pkg.contains("phone") ||
                pkg == "com.whatsapp" || pkg == "org.telegram.messenger" || pkg == "com.discord" || pkg == "com.viber.voip" || pkg == "com.facebook.orca"

        val text = (extras.getCharSequence(Notification.EXTRA_TEXT)?.toString() ?: "").lowercase(java.util.Locale.ROOT)
        val subText = (extras.getCharSequence(Notification.EXTRA_SUB_TEXT)?.toString() ?: "").lowercase(java.util.Locale.ROOT)
        val isIncomingText = text.contains("incoming call") || subText.contains("incoming call") || text.contains("ringing")
        if (isIncomingText) return false

        val isOngoingCallText = text.contains("ongoing call") || text.contains("call in progress") || subText.contains("ongoing call")

        return (isCallCategory && (hasHangupAction || isDialerPkg || isOngoingCallText)) || (isDialerPkg && hasHangupAction)
    }

    private fun handleOngoingCallNotification(sbn: StatusBarNotification) {
        val isNewCall = systemCallKey != sbn.key || !IslandStateManager.activeCallState.value.isActive
        systemCallKey = sbn.key
        val notification = sbn.notification ?: return
        val extras = notification.extras

        val rawTitle = extras?.getCharSequence(Notification.EXTRA_TITLE)?.toString()?.takeIf { it.isNotBlank() }
            ?: extras?.getCharSequence(Notification.EXTRA_TITLE_BIG)?.toString()?.takeIf { it.isNotBlank() }
            ?: run {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    val person = extras?.getParcelable("android.callPerson") as? android.app.Person
                    person?.name?.toString()?.takeIf { it.isNotBlank() }
                } else null
            }
            ?: "Active Call"

        activeEndCallAction = null
        activeMuteOrSpeakerAction = null

        val actions = notification.actions
        actions?.forEach { action ->
            val aTitle = (action?.title?.toString() ?: "").lowercase(java.util.Locale.ROOT)
            if (aTitle.contains("hang up") || aTitle.contains("hangup") || aTitle.contains("end") || aTitle.contains("leave") || aTitle.contains("disconnect")) {
                activeEndCallAction = action
            } else if (aTitle.contains("mute") || aTitle.contains("unmute") || aTitle.contains("speaker") || aTitle.contains("audio")) {
                if (activeMuteOrSpeakerAction == null || aTitle.contains("mute")) {
                    activeMuteOrSpeakerAction = action
                }
            }
        }
        if (activeEndCallAction == null && !actions.isNullOrEmpty()) {
            activeEndCallAction = actions.last()
        }
        if (activeMuteOrSpeakerAction == null && actions != null && actions.size >= 2) {
            activeMuteOrSpeakerAction = actions.firstOrNull { it != activeEndCallAction }
        }

        val muteTitleLower = activeMuteOrSpeakerAction?.title?.toString()?.lowercase(java.util.Locale.ROOT) ?: ""
        val isSpeakerButton = muteTitleLower.contains("speaker") || muteTitleLower.contains("audio")
        val isCurrentlyMuted = muteTitleLower.contains("unmute")

        IslandStateManager.onEndCall = {
            try {
                activeEndCallAction?.actionIntent?.send()
            } catch (e: Exception) {
                Log.e("NotificationReceiver", "Error sending end call intent", e)
            }
            callTimerJob?.cancel()
            callTimerJob = null
            systemCallKey = null
            activeEndCallAction = null
            activeMuteOrSpeakerAction = null
            IslandStateManager.updateCallState(ActiveCallState(isActive = false))
        }

        IslandStateManager.onToggleCallMuteOrSpeaker = {
            val current = IslandStateManager.activeCallState.value
            var sent = false
            val actionToInvoke = activeMuteOrSpeakerAction
            if (actionToInvoke?.actionIntent != null) {
                try {
                    actionToInvoke.actionIntent.send()
                    sent = true
                } catch (e: Exception) {
                    Log.e("NotificationReceiver", "Error sending call mute/speaker intent", e)
                }
            }
            if (!sent) {
                try {
                    val am = getSystemService(Context.AUDIO_SERVICE) as? android.media.AudioManager
                    if (am != null) {
                        am.isMicrophoneMute = !current.isMuted
                    }
                } catch (_: Exception) {}
            }
            IslandStateManager.updateCallState(current.copy(isMuted = !current.isMuted))
        }

        val chronometerBase = notification.`when`
        if (isNewCall) {
            callStartTimeMs = if (chronometerBase > 0L && chronometerBase <= System.currentTimeMillis()) {
                chronometerBase
            } else if (sbn.postTime > 0L && sbn.postTime <= System.currentTimeMillis()) {
                sbn.postTime
            } else {
                System.currentTimeMillis()
            }
        } else if (chronometerBase > 0L && chronometerBase <= System.currentTimeMillis()) {
            callStartTimeMs = chronometerBase
        }

        val elapsedSeconds = ((System.currentTimeMillis() - callStartTimeMs) / 1000).coerceAtLeast(0L)
        val prevMuted = IslandStateManager.activeCallState.value.isMuted
        IslandStateManager.updateCallState(
            ActiveCallState(
                isActive = true,
                callerName = rawTitle,
                packageName = sbn.packageName,
                durationSeconds = elapsedSeconds,
                isMuted = if (muteTitleLower.isNotBlank()) isCurrentlyMuted else prevMuted,
                isSpeaker = isSpeakerButton,
                contentIntent = notification.contentIntent
            )
        )

        if (callTimerJob == null || callTimerJob?.isActive != true) {
            callTimerJob = serviceScope.launch {
                while (isActive) {
                    delay(1000)
                    val currentState = IslandStateManager.activeCallState.value
                    if (currentState.isActive) {
                        val elapsed = ((System.currentTimeMillis() - callStartTimeMs) / 1000).coerceAtLeast(0L)
                        IslandStateManager.updateCallState(currentState.copy(durationSeconds = elapsed))
                    }
                }
            }
        }
    }

    private fun isScreenRecordingSavedNotification(sbn: StatusBarNotification, notification: Notification): Boolean {
        val extras = notification.extras
        val title = (extras?.getCharSequence(Notification.EXTRA_TITLE)?.toString() ?: "").lowercase()
        val text = (extras?.getCharSequence(Notification.EXTRA_TEXT)?.toString() ?: "").lowercase()
        val subText = (extras?.getCharSequence(Notification.EXTRA_SUB_TEXT)?.toString() ?: "").lowercase()
        val combined = "$title $text $subText"

        return combined.contains("saved") ||
                combined.contains("finished") ||
                combined.contains("completed") ||
                combined.contains("tap to view") ||
                combined.contains("tap to share") ||
                combined.contains("share") ||
                combined.contains("deleted") ||
                combined.contains("ready")
    }

    private fun isScreenRecordingNotification(sbn: StatusBarNotification, notification: Notification): Boolean {
        // 1. If it's a "saved" / completed notification, it's NOT an active screen recording!
        if (isScreenRecordingSavedNotification(sbn, notification)) {
            return false
        }

        // 2. Active screen recording MUST be ongoing (cannot be dismissed while recording)
        val isOngoing = sbn.isOngoing ||
                (notification.flags and Notification.FLAG_ONGOING_EVENT) != 0 ||
                (notification.flags and Notification.FLAG_NO_CLEAR) != 0
        if (!isOngoing) {
            return false
        }

        val pkg = sbn.packageName.lowercase()
        val channelId = notification.channelId?.lowercase() ?: ""
        val extras = notification.extras
        val title = (extras?.getCharSequence(Notification.EXTRA_TITLE)?.toString() ?: "").lowercase()
        val text = (extras?.getCharSequence(Notification.EXTRA_TEXT)?.toString() ?: "").lowercase()
        val subText = (extras?.getCharSequence(Notification.EXTRA_SUB_TEXT)?.toString() ?: "").lowercase()

        val isRecordingText = title.contains("screen record") ||
                title.contains("recording screen") ||
                title.contains("screen recording") ||
                title.contains("screen recorder") ||
                title.contains("recording display") ||
                text.contains("screen record") ||
                text.contains("recording screen") ||
                text.contains("screen recording") ||
                subText.contains("recording screen")

        val isRecordingPkg = pkg.contains("screenrecorder") ||
                pkg.contains("screen_recorder") ||
                pkg.contains("screenrecord") ||
                pkg.contains("hecorat.screenrecorder") ||
                pkg == "com.android.systemui" ||
                pkg == "com.google.android.systemui"

        val isRecordingChannel = channelId.contains("screen_record") ||
                channelId.contains("screenrecord") ||
                channelId.contains("screen_capture")

        return (isRecordingChannel && (isRecordingText || isRecordingPkg)) ||
                (isRecordingPkg && isRecordingText) ||
                title.contains("recording screen") ||
                title.contains("screen recording") ||
                title.contains("screen recorder") ||
                text.contains("recording screen") ||
                text.contains("screen recording") ||
                text.contains("screen recorder")
    }

    private fun handleScreenRecordingNotification(sbn: StatusBarNotification) {
        systemRecordingKey = sbn.key
        val notification = sbn.notification ?: return

        // Extract actions: Stop, Pause, Resume
        val actions = notification.actions
        actions?.forEach { action ->
            val aTitle = (action.title?.toString() ?: "").lowercase()
            if (aTitle.contains("stop") || aTitle.contains("end") || aTitle.contains("finish")) {
                activeStopAction = action
            } else if (aTitle.contains("pause")) {
                activePauseAction = action
            } else if (aTitle.contains("resume")) {
                activeResumeAction = action
            }
        }
        if (activeStopAction == null && !actions.isNullOrEmpty() && actions.size == 1) {
            activeStopAction = actions[0]
        }

        // Connect functions so island buttons trigger real recording actions
        IslandStateManager.onStopRecording = {
            try {
                activeStopAction?.actionIntent?.send()
            } catch (e: Exception) {
                Log.e("NotificationReceiver", "Error sending stop intent", e)
            }
            recordingTimerJob?.cancel()
            recordingTimerJob = null
            systemRecordingKey = null
            activeStopAction = null
            activePauseAction = null
            activeResumeAction = null
            IslandStateManager.updateRecordingState(ScreenRecordingState(isRecording = false))
        }

        IslandStateManager.onTogglePauseRecording = {
            val currentState = IslandStateManager.screenRecordingState.value
            if (currentState.isPaused) {
                try {
                    activeResumeAction?.actionIntent?.send() ?: activePauseAction?.actionIntent?.send()
                } catch (e: Exception) {
                    Log.e("NotificationReceiver", "Error sending resume intent", e)
                }
                if (recordingPausedAtMs > 0) {
                    recordingStartTimeMs += (System.currentTimeMillis() - recordingPausedAtMs)
                    recordingPausedAtMs = 0L
                }
                IslandStateManager.updateRecordingState(currentState.copy(isPaused = false))
            } else {
                try {
                    activePauseAction?.actionIntent?.send()
                } catch (e: Exception) {
                    Log.e("NotificationReceiver", "Error sending pause intent", e)
                }
                recordingPausedAtMs = System.currentTimeMillis()
                IslandStateManager.updateRecordingState(currentState.copy(isPaused = true))
            }
        }

        if (recordingTimerJob == null || !IslandStateManager.screenRecordingState.value.isRecording) {
            val postTime = sbn.postTime
            recordingStartTimeMs = if (postTime > 0 && postTime <= System.currentTimeMillis()) postTime else System.currentTimeMillis()
            recordingPausedAtMs = 0L
            val initialSeconds = ((System.currentTimeMillis() - recordingStartTimeMs) / 1000).coerceAtLeast(0L)
            IslandStateManager.updateRecordingState(
                ScreenRecordingState(isRecording = true, durationSeconds = initialSeconds, isPaused = false)
            )
            recordingTimerJob = serviceScope.launch {
                while (isActive) {
                    delay(1000)
                    val currentState = IslandStateManager.screenRecordingState.value
                    if (!currentState.isPaused) {
                        val elapsed = ((System.currentTimeMillis() - recordingStartTimeMs) / 1000).coerceAtLeast(0L)
                        IslandStateManager.updateRecordingState(
                            currentState.copy(isRecording = true, durationSeconds = elapsed)
                        )
                    }
                }
            }
        }
    }

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        super.onNotificationPosted(sbn)
        if (sbn == null) return
        if (!currentSettings.showNotifications && !currentSettings.showLiveProgress && !currentSettings.showScreenRecording && !currentSettings.showCalls) return

        val packageName = sbn.packageName
        if (packageName == this.packageName) return // Ignore self
        if (currentSettings.blockedPackages.contains(packageName)) return

        val notification = sbn.notification ?: return
        val extras = notification.extras ?: return

        // 0. If a "screen recording saved" notification arrives, immediately shut down recording state
        if (isScreenRecordingSavedNotification(sbn, notification)) {
            val pkg = sbn.packageName.lowercase()
            val channelId = notification.channelId?.lowercase() ?: ""
            if (pkg.contains("screenrecord") || pkg == "com.android.systemui" || pkg == "com.google.android.systemui" || channelId.contains("screen_record") || systemRecordingKey == sbn.key) {
                systemRecordingKey = null
                recordingTimerJob?.cancel()
                recordingTimerJob = null
                activeStopAction = null
                activePauseAction = null
                activeResumeAction = null
                IslandStateManager.updateRecordingState(ScreenRecordingState(isRecording = false))
                return
            }
        }

        // 1. Detect system screen recording notifications
        if (isScreenRecordingNotification(sbn, notification)) {
            if (currentSettings.showScreenRecording) {
                handleScreenRecordingNotification(sbn)
            }
            return
        }

        // 1B. Detect active ongoing phone call notifications
        if (isOngoingCallNotification(sbn, notification)) {
            if (currentSettings.showCalls) {
                handleOngoingCallNotification(sbn)
            }
            return
        } else if (systemCallKey == sbn.key) {
            // Call notification transitioned out of ongoing state
            systemCallKey = null
            callTimerJob?.cancel()
            callTimerJob = null
            activeEndCallAction = null
            activeMuteOrSpeakerAction = null
            IslandStateManager.updateCallState(ActiveCallState(isActive = false))
        }

        // 2. Skip media notifications (Media controls are handled exclusively via MediaSessionManager with slider & controls)
        if (isMediaNotification(sbn, notification)) {
            val token: android.media.session.MediaSession.Token? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                extras.getParcelable(Notification.EXTRA_MEDIA_SESSION, android.media.session.MediaSession.Token::class.java)
            } else {
                @Suppress("DEPRECATION")
                extras.getParcelable(Notification.EXTRA_MEDIA_SESSION) as? android.media.session.MediaSession.Token
            } ?: run {
                @Suppress("DEPRECATION")
                extras.getParcelable("android.mediaSession") as? android.media.session.MediaSession.Token
            }

            val directController = token?.let {
                runCatching { MediaController(this, it) }.getOrNull()
            }
            refreshMedia(directController)
            return
        }

        // 3. Extract live progress tracking (e.g. Google Play Store, downloads, transfers, navigation)
        val progress = extras.getInt(Notification.EXTRA_PROGRESS, -1)
        val maxProgress = extras.getInt(Notification.EXTRA_PROGRESS_MAX, 0)
        val isIndeterminate = extras.getBoolean(Notification.EXTRA_PROGRESS_INDETERMINATE, false)
        val isOngoing = (notification.flags and Notification.FLAG_ONGOING_EVENT) != 0 || sbn.isOngoing
        val isNavCategory = notification.category == Notification.CATEGORY_NAVIGATION
        val isNavApp = packageName == "com.google.android.apps.maps" || packageName == "com.waze" || packageName.contains("maps")
        val isNavigation = (isNavCategory || isNavApp) && isOngoing
        val hasProgress = maxProgress > 0 || isIndeterminate || (progress >= 0 && maxProgress > 0) || isNavigation

        // Separate check: live progress notifications vs normal notification alerts
        if (hasProgress) {
            if (!currentSettings.showLiveProgress) return
        } else {
            if (!currentSettings.showNotifications) return
        }

        // Skip ongoing notifications UNLESS they report live progress (e.g. downloads, navigation)
        if (isOngoing && !hasProgress) return

        val title = extras.getCharSequence(Notification.EXTRA_TITLE)?.toString()
            ?: extras.getCharSequence(Notification.EXTRA_TITLE_BIG)?.toString()
        val text = extras.getCharSequence(Notification.EXTRA_TEXT)?.toString()
            ?: extras.getCharSequence(Notification.EXTRA_BIG_TEXT)?.toString()
            ?: extras.getCharSequence(Notification.EXTRA_SUB_TEXT)?.toString()

        if (title.isNullOrBlank() && text.isNullOrBlank()) return

        val appName = try {
            val pm = packageManager
            val appInfo = pm.getApplicationInfo(packageName, 0)
            pm.getApplicationLabel(appInfo).toString()
        } catch (e: Exception) {
            packageName
        }

        val iconBitmap = extractNotificationBitmap(notification, packageName)

        val actions = mutableListOf<NotificationAction>()
        notification.actions?.forEach { action ->
            if (action != null && !action.title.isNullOrBlank()) {
                val remoteInput = action.remoteInputs?.firstOrNull()
                val choices = remoteInput?.choices?.map { it.toString() } ?: emptyList()
                actions.add(
                    NotificationAction(
                        title = action.title.toString(),
                        actionIntent = action.actionIntent,
                        remoteInputKey = remoteInput?.resultKey,
                        remoteInputLabel = remoteInput?.label?.toString(),
                        remoteInputChoices = choices
                    )
                )
            }
        }

        val notifInfo = NotificationInfo(
            key = sbn.key,
            packageName = packageName,
            appName = appName,
            title = title ?: appName,
            text = text ?: "",
            iconBitmap = iconBitmap,
            contentIntent = notification.contentIntent,
            actions = actions.take(3),
            timestamp = sbn.postTime,
            progress = progress,
            maxProgress = maxProgress,
            isIndeterminate = isIndeterminate || (isNavigation && maxProgress <= 0),
            isOngoing = isOngoing
        )

        if (hasProgress) {
            IslandStateManager.updateProgress(notifInfo)
        } else {
            IslandStateManager.updateNotification(notifInfo)
        }
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification?) {
        super.onNotificationRemoved(sbn)
        if (sbn == null) return
        if (systemRecordingKey == sbn.key || (sbn.notification != null && isScreenRecordingNotification(sbn, sbn.notification))) {
            systemRecordingKey = null
            recordingTimerJob?.cancel()
            recordingTimerJob = null
            activeStopAction = null
            activePauseAction = null
            activeResumeAction = null
            IslandStateManager.updateRecordingState(ScreenRecordingState(isRecording = false))
        }
        if (systemCallKey == sbn.key || (sbn.notification != null && isOngoingCallNotification(sbn, sbn.notification))) {
            systemCallKey = null
            callTimerJob?.cancel()
            callTimerJob = null
            activeEndCallAction = null
            activeMuteOrSpeakerAction = null
            IslandStateManager.updateCallState(ActiveCallState(isActive = false))
        }
        val notifObj = sbn.notification
        if (notifObj != null && (isMediaNotification(sbn, notifObj) || sbn.packageName == activeMediaController?.packageName)) {
            refreshMedia()
        }
        IslandStateManager.removeNotification(sbn.key)
    }

    private fun extractNotificationBitmap(notification: Notification, packageName: String): Bitmap? {
        val largeIcon: Icon? = notification.getLargeIcon()
        if (largeIcon != null) {
            val drawable = largeIcon.loadDrawable(this)
            if (drawable != null) return drawableToBitmap(drawable)
        }
        val smallIcon: Icon? = notification.smallIcon
        if (smallIcon != null) {
            val drawable = smallIcon.loadDrawable(this)
            if (drawable != null) return drawableToBitmap(drawable)
        }
        return getAppIconBitmap(packageName)
    }

    private val appIconCache = android.util.LruCache<String, Bitmap>(30)

    private fun getAppIconBitmap(packageName: String): Bitmap? {
        appIconCache.get(packageName)?.let { return it }
        return try {
            val drawable = packageManager.getApplicationIcon(packageName)
            val bitmap = drawableToBitmap(drawable)
            appIconCache.put(packageName, bitmap)
            bitmap
        } catch (e: Exception) {
            null
        }
    }

    private fun drawableToBitmap(drawable: Drawable): Bitmap {
        if (drawable is BitmapDrawable && drawable.bitmap != null) {
            return drawable.bitmap
        }
        val width = if (drawable.intrinsicWidth > 0) drawable.intrinsicWidth else 96
        val height = if (drawable.intrinsicHeight > 0) drawable.intrinsicHeight else 96
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        drawable.setBounds(0, 0, canvas.width, canvas.height)
        drawable.draw(canvas)
        return bitmap
    }

    private fun isMediaNotification(sbn: StatusBarNotification, notification: Notification): Boolean {
        // 1. Category is TRANSPORT
        if (notification.category == Notification.CATEGORY_TRANSPORT) return true

        // 2. Extras contains media session token or MediaStyle template
        val extras = notification.extras
        if (extras != null) {
            if (extras.containsKey(Notification.EXTRA_MEDIA_SESSION) ||
                extras.containsKey("android.mediaSession") ||
                extras.getString(Notification.EXTRA_TEMPLATE)?.contains("MediaStyle") == true
            ) {
                return true
            }
        }

        // 3. Sender is currently active media controller
        if (sbn.packageName == activeMediaController?.packageName) return true
        try {
            val componentName = ComponentName(this, NotificationReceiverService::class.java)
            val activeSessions = mediaSessionManager?.getActiveSessions(componentName)
            if (activeSessions?.any { it.packageName == sbn.packageName } == true) return true
        } catch (_: Exception) {}

        // 4. Playback actions present (e.g. Previous, Next, Play, Pause)
        notification.actions?.forEach { action ->
            val actionTitle = action?.title?.toString()?.lowercase(java.util.Locale.ROOT) ?: ""
            if (actionTitle.contains("pause") || actionTitle.contains("play") ||
                actionTitle.contains("skip") || actionTitle.contains("previous") ||
                actionTitle.contains("next") || actionTitle.contains("rewind") ||
                actionTitle.contains("fast forward")
            ) {
                return true
            }
        }

        return false
    }
}
