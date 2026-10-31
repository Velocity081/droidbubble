package com.droidbubble.app.data

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.droidbubble.app.model.IslandSettings
import com.droidbubble.app.model.DEFAULT_FEATURE_PRIORITIES
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "island_settings")

class SettingsRepository(private val context: Context) {

    private object PreferencesKeys {
        val IS_ENABLED = booleanPreferencesKey("is_enabled")
        val WIDTH_DP = intPreferencesKey("width_dp")
        val HEIGHT_DP = intPreferencesKey("height_dp")
        val CORNER_RADIUS_DP = intPreferencesKey("corner_radius_dp")
        val TOP_OFFSET_DP = intPreferencesKey("top_offset_dp")
        val HORIZONTAL_OFFSET_DP = intPreferencesKey("horizontal_offset_dp")
        val IS_LEFT_CUTOUT = booleanPreferencesKey("is_left_cutout")
        val IS_TEARDROP_CUTOUT = booleanPreferencesKey("is_teardrop_cutout")
        val SHOW_OUTLINE = booleanPreferencesKey("show_outline")
        val OUTLINE_COLOR_ARGB = longPreferencesKey("outline_color_argb")
        val OUTLINE_THICKNESS_DP = floatPreferencesKey("outline_thickness_dp")
        val EXPANDED_SCALE = floatPreferencesKey("expanded_scale")
        val HAPTICS_ENABLED = booleanPreferencesKey("haptics_enabled")
        val SHOW_CALLS = booleanPreferencesKey("show_calls")
        val SHOW_MEDIA = booleanPreferencesKey("show_media")
        val SHOW_NOTIFICATIONS = booleanPreferencesKey("show_notifications")
        val SHOW_LIVE_PROGRESS = booleanPreferencesKey("show_live_progress")
        val SHOW_SCREEN_RECORDING = booleanPreferencesKey("show_screen_recording")
        val SHOW_FLASHLIGHT = booleanPreferencesKey("show_flashlight")
        val SHOW_BLUETOOTH = booleanPreferencesKey("show_bluetooth")
        val SHOW_CHARGING = booleanPreferencesKey("show_charging")
        val SHOW_RINGER_MODE = booleanPreferencesKey("show_ringer_mode")
        val SHOW_AIRPLANE_MODE = booleanPreferencesKey("show_airplane_mode")
        val SHOW_IN_LANDSCAPE = booleanPreferencesKey("show_in_landscape")
        val ANIMATION_SPEED_MULTIPLIER = floatPreferencesKey("anim_speed_multiplier")
        val LEGACY_ANIMATION_SPEED = stringPreferencesKey("animation_speed")
        val BLOCKED_PACKAGES = stringSetPreferencesKey("blocked_packages")
        val RESTART_ON_BOOT = booleanPreferencesKey("restart_on_boot")
        val BUBBLE_EFFECT_ENABLED = booleanPreferencesKey("bubble_effect_enabled")
        val DISMISS_MEDIA_ON_PAUSE = booleanPreferencesKey("dismiss_media_on_pause")
        val ALWAYS_SHOW_PILL = booleanPreferencesKey("always_show_pill")
        val SHOW_DUAL_ACTIVITY_IN_COMPACT = booleanPreferencesKey("show_dual_activity_in_compact")
        val SHOW_FPS = booleanPreferencesKey("show_fps")
        val SHOW_WEATHER = booleanPreferencesKey("show_weather")
        val SHOW_NETWORK_SPEED = booleanPreferencesKey("show_network_speed")
        val AUTO_DISMISS_NOTIFICATIONS = booleanPreferencesKey("auto_dismiss_notifications")
        val NOTIFICATION_AUTO_DISMISS_SECONDS = intPreferencesKey("notification_auto_dismiss_seconds")
        val PERSISTENT_CHARGING = booleanPreferencesKey("persistent_charging")
        val NOTIFICATION_MAX_LINES = intPreferencesKey("notification_max_lines")
        val WEATHER_CITY = stringPreferencesKey("weather_city")
        val FONT_KEY = stringPreferencesKey("font_key")
        val SWAP_ACTIVITIES_ON_SWIPE = booleanPreferencesKey("swap_activities_on_swipe")
        val BLURRED_ALBUM_ART_BACKGROUND = booleanPreferencesKey("blurred_album_art_background")
        val GLASS_ENABLED = booleanPreferencesKey("glass_enabled")
        val GLASS_OPACITY = intPreferencesKey("glass_opacity")
        val MEDIA_WAVE_ANIMATION = booleanPreferencesKey("media_wave_animation")
        val SPINNING_ALBUM_ART = booleanPreferencesKey("spinning_album_art")
        val WAVY_PROGRESS_BAR = booleanPreferencesKey("wavy_progress_bar")
        val HIDE_ON_LOCK_SCREEN = booleanPreferencesKey("hide_on_lock_screen")
        val EXPAND_DAMPING = floatPreferencesKey("expand_damping")
        val EXPAND_STIFFNESS = floatPreferencesKey("expand_stiffness")
        val APPEAR_DAMPING = floatPreferencesKey("appear_damping")
        val APPEAR_STIFFNESS = floatPreferencesKey("appear_stiffness")
        val SPLIT_DAMPING = floatPreferencesKey("split_damping")
        val SPLIT_STIFFNESS = floatPreferencesKey("split_stiffness")
        val FEATURE_PRIORITIES = stringPreferencesKey("feature_priorities")
    }

    val settingsFlow: Flow<IslandSettings> = context.dataStore.data.map { preferences ->
        val resolvedSpeed = preferences[PreferencesKeys.ANIMATION_SPEED_MULTIPLIER] ?: run {
            when (preferences[PreferencesKeys.LEGACY_ANIMATION_SPEED]) {
                "FAST" -> 1.5f
                "SLOW" -> 0.7f
                else -> 0.7f
            }
        }

        IslandSettings(
            isEnabled = preferences[PreferencesKeys.IS_ENABLED] ?: true,
            widthDp = preferences[PreferencesKeys.WIDTH_DP] ?: 180,
            heightDp = preferences[PreferencesKeys.HEIGHT_DP] ?: 36,
            cornerRadiusDp = preferences[PreferencesKeys.CORNER_RADIUS_DP] ?: 22,
            topOffsetDp = preferences[PreferencesKeys.TOP_OFFSET_DP] ?: 12,
            horizontalOffsetDp = preferences[PreferencesKeys.HORIZONTAL_OFFSET_DP] ?: 0,
            isLeftCutout = preferences[PreferencesKeys.IS_LEFT_CUTOUT] ?: false,
            isTeardropCutout = preferences[PreferencesKeys.IS_TEARDROP_CUTOUT] ?: false,
            showOutline = preferences[PreferencesKeys.SHOW_OUTLINE] ?: true,
            outlineColorArgb = preferences[PreferencesKeys.OUTLINE_COLOR_ARGB] ?: 0x33FFFFFF,
            outlineThicknessDp = preferences[PreferencesKeys.OUTLINE_THICKNESS_DP] ?: 1.5f,
            expandedScale = preferences[PreferencesKeys.EXPANDED_SCALE] ?: 1.1f,
            hapticsEnabled = preferences[PreferencesKeys.HAPTICS_ENABLED] ?: true,
            showCalls = preferences[PreferencesKeys.SHOW_CALLS] ?: true,
            showMedia = preferences[PreferencesKeys.SHOW_MEDIA] ?: true,
            showNotifications = preferences[PreferencesKeys.SHOW_NOTIFICATIONS] ?: false,
            showLiveProgress = preferences[PreferencesKeys.SHOW_LIVE_PROGRESS] ?: true,
            showScreenRecording = preferences[PreferencesKeys.SHOW_SCREEN_RECORDING] ?: true,
            showFlashlight = preferences[PreferencesKeys.SHOW_FLASHLIGHT] ?: true,
            showBluetooth = preferences[PreferencesKeys.SHOW_BLUETOOTH] ?: true,
            showCharging = preferences[PreferencesKeys.SHOW_CHARGING] ?: true,
            showRingerMode = preferences[PreferencesKeys.SHOW_RINGER_MODE] ?: true,
            showAirplaneMode = preferences[PreferencesKeys.SHOW_AIRPLANE_MODE] ?: true,
            showInLandscape = preferences[PreferencesKeys.SHOW_IN_LANDSCAPE] ?: false,
            animationSpeed = resolvedSpeed,
            blockedPackages = preferences[PreferencesKeys.BLOCKED_PACKAGES] ?: emptySet(),
            restartOnBoot = preferences[PreferencesKeys.RESTART_ON_BOOT] ?: true,
            bubbleEffectEnabled = preferences[PreferencesKeys.BUBBLE_EFFECT_ENABLED] ?: true,
            dismissMediaOnPause = preferences[PreferencesKeys.DISMISS_MEDIA_ON_PAUSE] ?: false,
            alwaysShowPill = preferences[PreferencesKeys.ALWAYS_SHOW_PILL] ?: false,
            showDualActivityInCompact = preferences[PreferencesKeys.SHOW_DUAL_ACTIVITY_IN_COMPACT] ?: true,
            showFps = preferences[PreferencesKeys.SHOW_FPS] ?: false,
            showWeather = preferences[PreferencesKeys.SHOW_WEATHER] ?: false,
            showNetworkSpeed = preferences[PreferencesKeys.SHOW_NETWORK_SPEED] ?: false,
            autoDismissNotifications = preferences[PreferencesKeys.AUTO_DISMISS_NOTIFICATIONS] ?: false,
            notificationAutoDismissSeconds = preferences[PreferencesKeys.NOTIFICATION_AUTO_DISMISS_SECONDS] ?: 3,
            persistentCharging = preferences[PreferencesKeys.PERSISTENT_CHARGING] ?: false,
            notificationMaxLines = preferences[PreferencesKeys.NOTIFICATION_MAX_LINES] ?: 2,
            weatherCity = preferences[PreferencesKeys.WEATHER_CITY] ?: "",
            fontKey = preferences[PreferencesKeys.FONT_KEY] ?: "DEFAULT",
            swapActivitiesOnSwipe = preferences[PreferencesKeys.SWAP_ACTIVITIES_ON_SWIPE] ?: true,
            blurredAlbumArtBackground = preferences[PreferencesKeys.BLURRED_ALBUM_ART_BACKGROUND] ?: false,
            glassEnabled = preferences[PreferencesKeys.GLASS_ENABLED] ?: false,
            glassOpacity = preferences[PreferencesKeys.GLASS_OPACITY] ?: 80,
            mediaWaveAnimation = preferences[PreferencesKeys.MEDIA_WAVE_ANIMATION] ?: true,
            spinningAlbumArt = preferences[PreferencesKeys.SPINNING_ALBUM_ART] ?: false,
            wavyProgressBar = preferences[PreferencesKeys.WAVY_PROGRESS_BAR] ?: true,
            hideOnLockScreen = preferences[PreferencesKeys.HIDE_ON_LOCK_SCREEN] ?: false,
            expandDamping = preferences[PreferencesKeys.EXPAND_DAMPING] ?: 0.74f,
            expandStiffness = preferences[PreferencesKeys.EXPAND_STIFFNESS] ?: 380f,
            appearDamping = preferences[PreferencesKeys.APPEAR_DAMPING] ?: 0.6f,
            appearStiffness = preferences[PreferencesKeys.APPEAR_STIFFNESS] ?: 280f,
            splitDamping = preferences[PreferencesKeys.SPLIT_DAMPING] ?: 0.63f,
            splitStiffness = preferences[PreferencesKeys.SPLIT_STIFFNESS] ?: 320f,
            featurePriorities = preferences[PreferencesKeys.FEATURE_PRIORITIES]
                ?.split(",")
                ?.map { it.trim() }
                ?.filter { it.isNotBlank() && it != "TIMER" }
                ?.let { list ->
                    val set = list.toSet()
                    list + DEFAULT_FEATURE_PRIORITIES.filter { it !in set }
                } ?: DEFAULT_FEATURE_PRIORITIES
        )
    }

    suspend fun updateSettings(update: (IslandSettings) -> IslandSettings) {
        context.dataStore.edit { preferences ->
            val resolvedSpeed = preferences[PreferencesKeys.ANIMATION_SPEED_MULTIPLIER] ?: run {
                when (preferences[PreferencesKeys.LEGACY_ANIMATION_SPEED]) {
                    "FAST" -> 1.5f
                    "SLOW" -> 0.7f
                    else -> 0.7f
                }
            }

            val current = IslandSettings(
                isEnabled = preferences[PreferencesKeys.IS_ENABLED] ?: true,
                widthDp = preferences[PreferencesKeys.WIDTH_DP] ?: 180,
                heightDp = preferences[PreferencesKeys.HEIGHT_DP] ?: 36,
                cornerRadiusDp = preferences[PreferencesKeys.CORNER_RADIUS_DP] ?: 22,
                topOffsetDp = preferences[PreferencesKeys.TOP_OFFSET_DP] ?: 12,
                horizontalOffsetDp = preferences[PreferencesKeys.HORIZONTAL_OFFSET_DP] ?: 0,
                isLeftCutout = preferences[PreferencesKeys.IS_LEFT_CUTOUT] ?: false,
                isTeardropCutout = preferences[PreferencesKeys.IS_TEARDROP_CUTOUT] ?: false,
                showOutline = preferences[PreferencesKeys.SHOW_OUTLINE] ?: true,
                outlineColorArgb = preferences[PreferencesKeys.OUTLINE_COLOR_ARGB] ?: 0x33FFFFFF,
                outlineThicknessDp = preferences[PreferencesKeys.OUTLINE_THICKNESS_DP] ?: 1.5f,
                expandedScale = preferences[PreferencesKeys.EXPANDED_SCALE] ?: 1.1f,
                hapticsEnabled = preferences[PreferencesKeys.HAPTICS_ENABLED] ?: true,
                showCalls = preferences[PreferencesKeys.SHOW_CALLS] ?: true,
                showMedia = preferences[PreferencesKeys.SHOW_MEDIA] ?: true,
                showNotifications = preferences[PreferencesKeys.SHOW_NOTIFICATIONS] ?: false,
                showLiveProgress = preferences[PreferencesKeys.SHOW_LIVE_PROGRESS] ?: true,
                showScreenRecording = preferences[PreferencesKeys.SHOW_SCREEN_RECORDING] ?: true,
            showFlashlight = preferences[PreferencesKeys.SHOW_FLASHLIGHT] ?: true,
                showBluetooth = preferences[PreferencesKeys.SHOW_BLUETOOTH] ?: true,
                showCharging = preferences[PreferencesKeys.SHOW_CHARGING] ?: true,
                showRingerMode = preferences[PreferencesKeys.SHOW_RINGER_MODE] ?: true,
            showAirplaneMode = preferences[PreferencesKeys.SHOW_AIRPLANE_MODE] ?: true,
                showInLandscape = preferences[PreferencesKeys.SHOW_IN_LANDSCAPE] ?: false,
                animationSpeed = resolvedSpeed,
                blockedPackages = preferences[PreferencesKeys.BLOCKED_PACKAGES] ?: emptySet(),
                restartOnBoot = preferences[PreferencesKeys.RESTART_ON_BOOT] ?: true,
                bubbleEffectEnabled = preferences[PreferencesKeys.BUBBLE_EFFECT_ENABLED] ?: true,
                dismissMediaOnPause = preferences[PreferencesKeys.DISMISS_MEDIA_ON_PAUSE] ?: false,
                alwaysShowPill = preferences[PreferencesKeys.ALWAYS_SHOW_PILL] ?: false,
                showDualActivityInCompact = preferences[PreferencesKeys.SHOW_DUAL_ACTIVITY_IN_COMPACT] ?: true,
                showFps = preferences[PreferencesKeys.SHOW_FPS] ?: false,
                showWeather = preferences[PreferencesKeys.SHOW_WEATHER] ?: false,
                showNetworkSpeed = preferences[PreferencesKeys.SHOW_NETWORK_SPEED] ?: false,
                autoDismissNotifications = preferences[PreferencesKeys.AUTO_DISMISS_NOTIFICATIONS] ?: false,
                notificationAutoDismissSeconds = preferences[PreferencesKeys.NOTIFICATION_AUTO_DISMISS_SECONDS] ?: 3,
                persistentCharging = preferences[PreferencesKeys.PERSISTENT_CHARGING] ?: false,
                notificationMaxLines = preferences[PreferencesKeys.NOTIFICATION_MAX_LINES] ?: 2,
                weatherCity = preferences[PreferencesKeys.WEATHER_CITY] ?: "",
                fontKey = preferences[PreferencesKeys.FONT_KEY] ?: "DEFAULT",
                swapActivitiesOnSwipe = preferences[PreferencesKeys.SWAP_ACTIVITIES_ON_SWIPE] ?: true,
            blurredAlbumArtBackground = preferences[PreferencesKeys.BLURRED_ALBUM_ART_BACKGROUND] ?: false,
            glassEnabled = preferences[PreferencesKeys.GLASS_ENABLED] ?: false,
            glassOpacity = preferences[PreferencesKeys.GLASS_OPACITY] ?: 80,
            mediaWaveAnimation = preferences[PreferencesKeys.MEDIA_WAVE_ANIMATION] ?: true,
            spinningAlbumArt = preferences[PreferencesKeys.SPINNING_ALBUM_ART] ?: false,
            wavyProgressBar = preferences[PreferencesKeys.WAVY_PROGRESS_BAR] ?: true,
            hideOnLockScreen = preferences[PreferencesKeys.HIDE_ON_LOCK_SCREEN] ?: false,
            expandDamping = preferences[PreferencesKeys.EXPAND_DAMPING] ?: 0.74f,
            expandStiffness = preferences[PreferencesKeys.EXPAND_STIFFNESS] ?: 380f,
            appearDamping = preferences[PreferencesKeys.APPEAR_DAMPING] ?: 0.6f,
            appearStiffness = preferences[PreferencesKeys.APPEAR_STIFFNESS] ?: 280f,
            splitDamping = preferences[PreferencesKeys.SPLIT_DAMPING] ?: 0.63f,
            splitStiffness = preferences[PreferencesKeys.SPLIT_STIFFNESS] ?: 320f,
                featurePriorities = preferences[PreferencesKeys.FEATURE_PRIORITIES]
                    ?.split(",")
                    ?.map { it.trim() }
                    ?.filter { it.isNotBlank() && it != "TIMER" }
                    ?.let { list ->
                        val set = list.toSet()
                        list + DEFAULT_FEATURE_PRIORITIES.filter { it !in set }
                    } ?: DEFAULT_FEATURE_PRIORITIES
            )

            val newSettings = update(current)

            preferences[PreferencesKeys.IS_ENABLED] = newSettings.isEnabled
            preferences[PreferencesKeys.WIDTH_DP] = newSettings.widthDp
            preferences[PreferencesKeys.HEIGHT_DP] = newSettings.heightDp
            preferences[PreferencesKeys.CORNER_RADIUS_DP] = newSettings.cornerRadiusDp
            preferences[PreferencesKeys.TOP_OFFSET_DP] = newSettings.topOffsetDp
            preferences[PreferencesKeys.HORIZONTAL_OFFSET_DP] = newSettings.horizontalOffsetDp
            preferences[PreferencesKeys.IS_LEFT_CUTOUT] = newSettings.isLeftCutout
            preferences[PreferencesKeys.IS_TEARDROP_CUTOUT] = newSettings.isTeardropCutout
            preferences[PreferencesKeys.SHOW_OUTLINE] = newSettings.showOutline
            preferences[PreferencesKeys.OUTLINE_COLOR_ARGB] = newSettings.outlineColorArgb
            preferences[PreferencesKeys.OUTLINE_THICKNESS_DP] = newSettings.outlineThicknessDp
            preferences[PreferencesKeys.EXPANDED_SCALE] = newSettings.expandedScale
            preferences[PreferencesKeys.HAPTICS_ENABLED] = newSettings.hapticsEnabled
            preferences[PreferencesKeys.SHOW_CALLS] = newSettings.showCalls
            preferences[PreferencesKeys.SHOW_MEDIA] = newSettings.showMedia
            preferences[PreferencesKeys.SHOW_NOTIFICATIONS] = newSettings.showNotifications
            preferences[PreferencesKeys.SHOW_LIVE_PROGRESS] = newSettings.showLiveProgress
            preferences[PreferencesKeys.SHOW_SCREEN_RECORDING] = newSettings.showScreenRecording
            preferences[PreferencesKeys.SHOW_FLASHLIGHT] = newSettings.showFlashlight
            preferences[PreferencesKeys.SHOW_BLUETOOTH] = newSettings.showBluetooth
            preferences[PreferencesKeys.SHOW_CHARGING] = newSettings.showCharging
            preferences[PreferencesKeys.SHOW_RINGER_MODE] = newSettings.showRingerMode
            preferences[PreferencesKeys.SHOW_AIRPLANE_MODE] = newSettings.showAirplaneMode
            preferences[PreferencesKeys.SHOW_IN_LANDSCAPE] = newSettings.showInLandscape
            preferences[PreferencesKeys.ANIMATION_SPEED_MULTIPLIER] = newSettings.animationSpeed
            preferences.remove(PreferencesKeys.LEGACY_ANIMATION_SPEED)
            preferences[PreferencesKeys.BLOCKED_PACKAGES] = newSettings.blockedPackages
            preferences[PreferencesKeys.RESTART_ON_BOOT] = newSettings.restartOnBoot
            preferences[PreferencesKeys.BUBBLE_EFFECT_ENABLED] = newSettings.bubbleEffectEnabled
            preferences[PreferencesKeys.DISMISS_MEDIA_ON_PAUSE] = newSettings.dismissMediaOnPause
            preferences[PreferencesKeys.ALWAYS_SHOW_PILL] = newSettings.alwaysShowPill
            preferences[PreferencesKeys.SHOW_DUAL_ACTIVITY_IN_COMPACT] = newSettings.showDualActivityInCompact
            preferences[PreferencesKeys.SHOW_FPS] = newSettings.showFps
            preferences[PreferencesKeys.SHOW_WEATHER] = newSettings.showWeather
            preferences[PreferencesKeys.SHOW_NETWORK_SPEED] = newSettings.showNetworkSpeed
            preferences[PreferencesKeys.AUTO_DISMISS_NOTIFICATIONS] = newSettings.autoDismissNotifications
            preferences[PreferencesKeys.NOTIFICATION_AUTO_DISMISS_SECONDS] = newSettings.notificationAutoDismissSeconds
            preferences[PreferencesKeys.PERSISTENT_CHARGING] = newSettings.persistentCharging
            preferences[PreferencesKeys.NOTIFICATION_MAX_LINES] = newSettings.notificationMaxLines
            preferences[PreferencesKeys.WEATHER_CITY] = newSettings.weatherCity
            preferences[PreferencesKeys.FONT_KEY] = newSettings.fontKey
            preferences[PreferencesKeys.SWAP_ACTIVITIES_ON_SWIPE] = newSettings.swapActivitiesOnSwipe
            preferences[PreferencesKeys.BLURRED_ALBUM_ART_BACKGROUND] = newSettings.blurredAlbumArtBackground
            preferences[PreferencesKeys.GLASS_ENABLED] = newSettings.glassEnabled
            preferences[PreferencesKeys.GLASS_OPACITY] = newSettings.glassOpacity
            preferences[PreferencesKeys.MEDIA_WAVE_ANIMATION] = newSettings.mediaWaveAnimation
            preferences[PreferencesKeys.SPINNING_ALBUM_ART] = newSettings.spinningAlbumArt
            preferences[PreferencesKeys.WAVY_PROGRESS_BAR] = newSettings.wavyProgressBar
            preferences[PreferencesKeys.HIDE_ON_LOCK_SCREEN] = newSettings.hideOnLockScreen
            preferences[PreferencesKeys.EXPAND_DAMPING] = newSettings.expandDamping
            preferences[PreferencesKeys.EXPAND_STIFFNESS] = newSettings.expandStiffness
            preferences[PreferencesKeys.APPEAR_DAMPING] = newSettings.appearDamping
            preferences[PreferencesKeys.APPEAR_STIFFNESS] = newSettings.appearStiffness
            preferences[PreferencesKeys.SPLIT_DAMPING] = newSettings.splitDamping
            preferences[PreferencesKeys.SPLIT_STIFFNESS] = newSettings.splitStiffness
            preferences[PreferencesKeys.FEATURE_PRIORITIES] = newSettings.featurePriorities.joinToString(",")
        }
    }
}
