package com.droidbubble.app.ui.main


import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import android.provider.OpenableColumns
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.Spring
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.zIndex
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.material.icons.filled.Accessibility
import androidx.compose.material.icons.filled.Menu
import android.view.HapticFeedbackConstants
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.draw.alpha
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import java.util.Locale
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.activity.compose.BackHandler
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.Animation
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.CropSquare
import androidx.compose.material.icons.filled.Emergency
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Feedback
import androidx.compose.material.icons.filled.FitScreen
import androidx.compose.material.icons.filled.FlashlightOn
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material.icons.filled.Widgets
import androidx.compose.material.icons.filled.Height
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockReset
import androidx.compose.material.icons.filled.Album
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.LinearScale
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.RoundedCorner
import androidx.compose.material.icons.filled.ScreenRotation
import androidx.compose.material.icons.filled.ScreenSearchDesktop
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Straighten
import androidx.compose.material.icons.filled.SystemUpdate
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Update
import androidx.compose.material.icons.filled.VerticalAlignTop
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.drawable.toBitmap
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.droidbubble.app.model.IslandSettings
import com.droidbubble.app.service.IslandOverlayService
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

enum class MainScreenPage {
    MAIN,
    DIMENSIONS,
    APPEARANCE,
    ACCESSIBILITY
}


@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun MainScreen(
    viewModel: MainScreenViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val view = LocalView.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val settings by viewModel.settings.collectAsState()
    val pendingPermissions by viewModel.pendingPermissions.collectAsState()
    val installedApps by viewModel.installedApps.collectAsState()
    val isLoadingApps by viewModel.isLoadingApps.collectAsState()
    val updateState by viewModel.updateState.collectAsState()
    val showUpdateSheet by viewModel.showUpdateSheet.collectAsState()

    var showAppFilterSheet by remember { mutableStateOf(false) }
    var appFilterQuery by remember { mutableStateOf("") }

    var showDimensionsPage by remember { mutableStateOf(false) }
    var showAppearancePage by remember { mutableStateOf(false) }
    var showAccessibilityPage by remember { mutableStateOf(false) }
    var showFeedbackSheet by remember { mutableStateOf(false) }
    var feedbackType by remember { mutableStateOf(FeedbackType.BUG) }
    var feedbackTitle by remember { mutableStateOf("") }
    var feedbackDescription by remember { mutableStateOf("") }
    var attachmentUri by remember { mutableStateOf<Uri?>(null) }
    var attachmentName by remember { mutableStateOf<String?>(null) }

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            attachmentUri = uri
            attachmentName = getAttachmentFileName(context, uri)
        }
    }

    // Re-check permissions on every onResume
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.checkPermissions(context)
                viewModel.refreshUpdateStateOnResume(context)
                if (settings.isEnabled && PermissionHelper.isOverlayGranted(context)) {
                    IslandOverlayService.start(context)
                }
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    val currentNavPage = when {
        showDimensionsPage -> MainScreenPage.DIMENSIONS
        showAppearancePage -> MainScreenPage.APPEARANCE
        showAccessibilityPage -> MainScreenPage.ACCESSIBILITY
        else -> MainScreenPage.MAIN
    }

    AnimatedContent(
        targetState = currentNavPage,
        transitionSpec = {
            if (targetState != MainScreenPage.MAIN) {
                (slideInHorizontally(
                    initialOffsetX = { fullWidth -> fullWidth },
                    animationSpec = spring(stiffness = Spring.StiffnessMediumLow, dampingRatio = 0.85f)
                ) + fadeIn(animationSpec = tween(220)))
                    .togetherWith(
                        slideOutHorizontally(
                            targetOffsetX = { fullWidth -> -fullWidth / 3 },
                            animationSpec = spring(stiffness = Spring.StiffnessMediumLow, dampingRatio = 0.85f)
                        ) + fadeOut(animationSpec = tween(180))
                    )
            } else {
                (slideInHorizontally(
                    initialOffsetX = { fullWidth -> -fullWidth / 3 },
                    animationSpec = spring(stiffness = Spring.StiffnessMediumLow, dampingRatio = 0.85f)
                ) + fadeIn(animationSpec = tween(220)))
                    .togetherWith(
                        slideOutHorizontally(
                            targetOffsetX = { fullWidth -> fullWidth },
                            animationSpec = spring(stiffness = Spring.StiffnessMediumLow, dampingRatio = 0.85f)
                        ) + fadeOut(animationSpec = tween(180))
                    )
            }
        },
        label = "subPageTransition"
    ) { page ->
        when (page) {
            MainScreenPage.DIMENSIONS -> {
                DimensionsPage(
                    settings = settings,
                    onUpdateSettings = { viewModel.updateSettings(it) },
                    onBack = { showDimensionsPage = false },
                    modifier = modifier
                )
            }
            MainScreenPage.APPEARANCE -> {
                AppearancePage(
                    settings = settings,
                    onUpdateSettings = { viewModel.updateSettings(it) },
                    onBack = { showAppearancePage = false },
                    modifier = modifier
                )
            }
            MainScreenPage.ACCESSIBILITY -> {
                AccessibilityPage(
                    settings = settings,
                    onUpdateSettings = { viewModel.updateSettings(it) },
                    onBack = { showAccessibilityPage = false },
                    modifier = modifier
                )
            }
            MainScreenPage.MAIN -> {
        var isDraggingIslandActivity by remember { mutableStateOf(false) }

        Scaffold(
            modifier = modifier,
            topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.End
            ) {
                IconButton(
                    onClick = { showFeedbackSheet = true },
                    modifier = Modifier.size(44.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Feedback,
                        contentDescription = "Send Feedback",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(26.dp)
                    )
                }
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            userScrollEnabled = !isDraggingIslandActivity,
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // SECTION 1: PERMISSIONS (Hides individual item once granted, hides whole section when all granted!)
            if (pendingPermissions.isNotEmpty()) {
                item {
                    SectionHeader(
                        title = "Required Setup",
                        icon = Icons.Default.Security,
                        badge = "${pendingPermissions.size} Pending",
                        badgeColor = MaterialTheme.colorScheme.error
                    )
                }

                item {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        pendingPermissions.forEachIndexed { index, perm ->
                            val position = when {
                                pendingPermissions.size == 1 -> GroupPosition.SINGLE
                                index == 0 -> GroupPosition.TOP
                                index == pendingPermissions.size - 1 -> GroupPosition.BOTTOM
                                else -> GroupPosition.MIDDLE
                            }
                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                shape = getGroupedShape(position),
                                color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.25f)
                            ) {
                                ListItem(
                                    colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                                    headlineContent = {
                                        Text(perm.title, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                                    },
                                    leadingContent = {
                                        Box(
                                            modifier = Modifier
                                                .size(40.dp)
                                                .clip(CircleShape)
                                                .background(MaterialTheme.colorScheme.errorContainer),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Warning,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.error,
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }
                                    },
                                    trailingContent = {
                                        Button(
                                            onClick = {
                                                runCatching { context.startActivity(perm.createIntent(context)) }
                                            },
                                            shape = RoundedCornerShape(12.dp)
                                        ) {
                                            Text("Grant", fontWeight = FontWeight.SemiBold)
                                        }
                                    }
                                )
                            }
                        }
                    }
                }
            }

            // SECTION 2: MASTER ENABLE SWITCH
            item {
                SettingsCardItem(position = GroupPosition.SINGLE, isActive = settings.isEnabled) {
                    ListItem(
                        colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                        headlineContent = { Text("Enable Island", fontWeight = FontWeight.SemiBold) },
                        leadingContent = {
                            Icon(Icons.Default.PowerSettingsNew, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        },
                        trailingContent = {
                            Switch(
                                checked = settings.isEnabled,
                                onCheckedChange = { enabled ->
                                    viewModel.toggleEnabled(context, enabled)
                                }
                            )
                        }
                    )
                }
            }

            // UPDATE BUTTON: Shaped like a list component in bright accent color
            item {
                val isDownloading = updateState is MainScreenViewModel.UpdateState.Downloading
                val isChecking = updateState is MainScreenViewModel.UpdateState.Checking
                val isAvailable = updateState is MainScreenViewModel.UpdateState.Available

                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(28.dp))
                        .clickable(enabled = !isDownloading && !isChecking) {
                            if (isAvailable) {
                                viewModel.openUpdateSheet()
                            } else {
                                viewModel.checkForUpdates(context)
                            }
                        },
                    shape = RoundedCornerShape(28.dp),
                    color = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    shadowElevation = 2.dp
                ) {
                    ListItem(
                        colors = ListItemDefaults.colors(
                            containerColor = Color.Transparent,
                            headlineColor = MaterialTheme.colorScheme.onPrimary,
                            leadingIconColor = MaterialTheme.colorScheme.onPrimary,
                            trailingIconColor = MaterialTheme.colorScheme.onPrimary
                        ),
                        headlineContent = {
                            Text(
                                text = when (updateState) {
                                    is MainScreenViewModel.UpdateState.Checking -> "Checking for updates..."
                                    is MainScreenViewModel.UpdateState.UpToDate -> "Up to date"
                                    is MainScreenViewModel.UpdateState.Available -> {
                                        val rel = (updateState as MainScreenViewModel.UpdateState.Available).release
                                        "Update available (${rel.version})"
                                    }
                                    is MainScreenViewModel.UpdateState.Downloading -> {
                                        val p = ((updateState as MainScreenViewModel.UpdateState.Downloading).progress * 100).roundToInt()
                                        "Downloading update ($p%)..."
                                    }
                                    is MainScreenViewModel.UpdateState.Error -> "Check for Updates"
                                    else -> "Check for Updates"
                                },
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                        },
                        leadingContent = {
                            when (updateState) {
                                is MainScreenViewModel.UpdateState.Checking -> {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(24.dp),
                                        color = MaterialTheme.colorScheme.onPrimary,
                                        strokeWidth = 2.5.dp
                                    )
                                }
                                is MainScreenViewModel.UpdateState.UpToDate -> {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = "Up to date",
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                                is MainScreenViewModel.UpdateState.Available -> {
                                    Icon(
                                        imageVector = Icons.Default.SystemUpdate,
                                        contentDescription = "Update Available",
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                                else -> {
                                    Icon(
                                        imageVector = Icons.Default.Refresh,
                                        contentDescription = "Check for Updates",
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            }
                        },
                        trailingContent = {
                            if (updateState is MainScreenViewModel.UpdateState.Available) {
                                val rel = (updateState as MainScreenViewModel.UpdateState.Available).release
                                if (rel.isBeta) {
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.22f)
                                    ) {
                                        Text(
                                            text = "BETA",
                                            color = MaterialTheme.colorScheme.onPrimary,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                            }
                        }
                    )
                }
            }

            // TABS: ISLAND DIMENSIONS & APPEARANCE
            item {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    SettingsCardItem(position = GroupPosition.TOP) {
                        ListItem(
                            colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                            modifier = Modifier.clickable { showDimensionsPage = true },
                            headlineContent = { Text("Island Dimensions", fontWeight = FontWeight.SemiBold) },
                            leadingContent = {
                                Icon(Icons.Default.Straighten, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            },
                            trailingContent = {
                                Icon(
                                    Icons.AutoMirrored.Filled.ArrowForwardIos,
                                    contentDescription = "Open Island Dimensions",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        )
                    }

                    SettingsCardItem(position = GroupPosition.MIDDLE) {
                        ListItem(
                            colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                            modifier = Modifier.clickable { showAppearancePage = true },
                            headlineContent = { Text("Appearance & Feedback", fontWeight = FontWeight.SemiBold) },
                            leadingContent = {
                                Icon(Icons.Default.Palette, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            },
                            trailingContent = {
                                Icon(
                                    Icons.AutoMirrored.Filled.ArrowForwardIos,
                                    contentDescription = "Open Appearance & Feedback",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        )
                    }

                    SettingsCardItem(position = GroupPosition.BOTTOM) {
                        ListItem(
                            colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                            modifier = Modifier.clickable { showAccessibilityPage = true },
                            headlineContent = { Text("Accessibility", fontWeight = FontWeight.SemiBold) },
                            leadingContent = {
                                Icon(Icons.Default.Accessibility, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            },
                            trailingContent = {
                                Icon(
                                    Icons.AutoMirrored.Filled.ArrowForwardIos,
                                    contentDescription = "Open Accessibility",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        )
                    }
                }
            }

            // SECTION 5A: ISLAND ACTIVITIES (DRAG TO REORDER PRIORITY)
            item {
                SectionHeader(
                    title = "Island Activities",
                    icon = Icons.Default.Apps
                )
            }

            item {
                val reorderableKeys = remember {
                    val valid = listOf(
                        "CALL",
                        "MEDIA",
                        "NOTIFICATION",
                        "PROGRESS",
                        "RECORDING",
                        "FLASHLIGHT",
                        "FPS",
                        "WEATHER",
                        "NETWORK_SPEED"
                    )
                    val list = settings.featurePriorities.filter { it in valid }.toMutableList()
                    for (v in valid) {
                        if (v !in list) list.add(v)
                    }
                    list.toMutableStateList()
                }
                var draggingKey by remember { mutableStateOf<String?>(null) }
                var dragOffset by remember { mutableStateOf(0f) }
                var itemHeightPx by remember { mutableStateOf(0f) }

                LaunchedEffect(settings.featurePriorities) {
                    if (draggingKey == null) {
                        val valid = listOf(
                            "CALL",
                            "MEDIA",
                            "NOTIFICATION",
                            "PROGRESS",
                            "RECORDING",
                            "FLASHLIGHT",
                            "FPS",
                            "WEATHER",
                            "NETWORK_SPEED"
                        )
                        val list = settings.featurePriorities.filter { it in valid }.toMutableList()
                        for (v in valid) {
                            if (v !in list) list.add(v)
                        }
                        if (reorderableKeys.toList() != list) {
                            reorderableKeys.clear()
                            reorderableKeys.addAll(list)
                        }
                    }
                }

                val density = LocalDensity.current
                val cardGapPx = with(density) { 4.dp.toPx() }
                val effectiveCardHeight = if (itemHeightPx > 0f) itemHeightPx else with(density) { 64.dp.toPx() }

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    reorderableKeys.forEachIndexed { index, featureKey ->
                        key(featureKey) {
                            val currentKey = featureKey
                            val isBeingDragged = draggingKey == currentKey
                            val zIdx = if (isBeingDragged) 10f else 1f

                            val itemPosition = when {
                                index == 0 -> GroupPosition.TOP
                                index == reorderableKeys.size - 1 -> GroupPosition.BOTTOM
                                else -> GroupPosition.MIDDLE
                            }

                            val isFeatureChecked = when (currentKey) {
                                "CALL" -> settings.showCalls
                                "MEDIA" -> settings.showMedia
                                "NOTIFICATION" -> settings.showNotifications
                                "PROGRESS" -> settings.showLiveProgress
                                "RECORDING" -> settings.showScreenRecording
                                "FLASHLIGHT" -> settings.showFlashlight
                                "FPS" -> settings.showFps
                                "WEATHER" -> settings.showWeather
                                "NETWORK_SPEED" -> settings.showNetworkSpeed
                                else -> false
                            }

                            val featureTitle = when (currentKey) {
                                "CALL" -> "Phone Calls"
                                "MEDIA" -> "Media Player"
                                "NOTIFICATION" -> "Incoming Notifications"
                                "PROGRESS" -> "Live Progress & Activities"
                                "RECORDING" -> "Screen Recording Indicator"
                                "FLASHLIGHT" -> "Flashlight Indicator"
                                "FPS" -> "FPS Counter"
                                "WEATHER" -> "Weather & Temperature"
                                "NETWORK_SPEED" -> "Wi-Fi & Network Speed"
                                else -> currentKey
                            }

                            val featureIcon = when (currentKey) {
                                "CALL" -> Icons.Default.Call
                                "MEDIA" -> Icons.Default.MusicNote
                                "NOTIFICATION" -> Icons.Default.Notifications
                                "PROGRESS" -> Icons.Default.Animation
                                "RECORDING" -> Icons.Default.Videocam
                                "FLASHLIGHT" -> Icons.Default.FlashlightOn
                                "FPS" -> Icons.Default.Speed
                                "WEATHER" -> Icons.Default.WbSunny
                                "NETWORK_SPEED" -> Icons.Default.Wifi
                                else -> Icons.Default.Apps
                            }

                            val cardModifier = Modifier
                                .fillMaxWidth()
                                .zIndex(zIdx)
                                .onGloballyPositioned { coords ->
                                    if (!isBeingDragged && coords.size.height > 0) {
                                        itemHeightPx = coords.size.height.toFloat() + cardGapPx
                                    }
                                }
                                .graphicsLayer {
                                    if (isBeingDragged) {
                                        translationY = dragOffset
                                        scaleX = 1.02f
                                        scaleY = 1.02f
                                        shadowElevation = 14.dp.toPx()
                                    }
                                }

                            SettingsCardItem(
                                position = if (isBeingDragged) GroupPosition.SINGLE else itemPosition,
                                modifier = cardModifier,
                                isActive = isFeatureChecked
                            ) {
                                ListItem(
                                    colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                                    headlineContent = { Text(featureTitle, fontWeight = FontWeight.SemiBold) },
                                    leadingContent = {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(36.dp)
                                                    .pointerInput(currentKey) {
                                                        detectVerticalDragGestures(
                                                            onDragStart = {
                                                                isDraggingIslandActivity = true
                                                                draggingKey = currentKey
                                                                dragOffset = 0f
                                                                view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
                                                            },
                                                            onDragEnd = {
                                                                if (draggingKey == currentKey) {
                                                                    val updatedList = reorderableKeys.toList()
                                                                    viewModel.updateSettings { it.copy(featurePriorities = updatedList) }
                                                                    view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
                                                                }
                                                                draggingKey = null
                                                                isDraggingIslandActivity = false
                                                                dragOffset = 0f
                                                            },
                                                            onDragCancel = {
                                                                draggingKey = null
                                                                isDraggingIslandActivity = false
                                                                dragOffset = 0f
                                                            },
                                                            onVerticalDrag = { change, dragAmount ->
                                                                change.consume()
                                                                if (draggingKey != currentKey) return@detectVerticalDragGestures
                                                                dragOffset += dragAmount
                                                                val currentIndex = reorderableKeys.indexOf(currentKey)
                                                                if (currentIndex == -1) return@detectVerticalDragGestures
                                                                val step = if (itemHeightPx > 0f) itemHeightPx else effectiveCardHeight
                                                                val threshold = step * 0.5f
                                                                if (dragOffset > threshold && currentIndex < reorderableKeys.lastIndex) {
                                                                    val item = reorderableKeys.removeAt(currentIndex)
                                                                    reorderableKeys.add(currentIndex + 1, item)
                                                                    dragOffset -= step
                                                                    view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
                                                                } else if (dragOffset < -threshold && currentIndex > 0) {
                                                                    val item = reorderableKeys.removeAt(currentIndex)
                                                                    reorderableKeys.add(currentIndex - 1, item)
                                                                    dragOffset += step
                                                                    view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
                                                                }
                                                            }
                                                        )
                                                    },
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Menu,
                                                    contentDescription = "Drag to reorder",
                                                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.55f),
                                                    modifier = Modifier.size(20.dp)
                                                )
                                            }
                                            Icon(
                                                imageVector = featureIcon,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    },
                                    trailingContent = {
                                        Switch(
                                            checked = isFeatureChecked,
                                            onCheckedChange = { checked ->
                                                viewModel.updateSettings { s ->
                                                    when (currentKey) {
                                                        "CALL" -> s.copy(showCalls = checked)
                                                        "MEDIA" -> s.copy(showMedia = checked)
                                                        "NOTIFICATION" -> s.copy(showNotifications = checked)
                                                        "PROGRESS" -> s.copy(showLiveProgress = checked)
                                                        "RECORDING" -> s.copy(showScreenRecording = checked)
                                                        "FLASHLIGHT" -> s.copy(showFlashlight = checked)
                                                        "FPS" -> s.copy(showFps = checked)
                                                        "WEATHER" -> s.copy(showWeather = checked)
                                                        "NETWORK_SPEED" -> s.copy(showNetworkSpeed = checked)
                                                        else -> s
                                                    }
                                                }
                                            }
                                        )
                                    }
                                )
                            }
                        }
                    }
                }
            }

            // SECTION 5B: SYSTEM ALERTS & INDICATORS (ALWAYS TOP PRIORITY)
            item {
                SectionHeader(
                    title = "System Alerts",
                    icon = Icons.Default.NotificationsActive
                )
            }

            item {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    SettingsCardItem(position = GroupPosition.TOP, isActive = settings.showBluetooth) {
                        ListItem(
                            colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                            headlineContent = { Text("Bluetooth Connecting", fontWeight = FontWeight.SemiBold) },
                            leadingContent = {
                                Icon(Icons.Default.Bluetooth, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                            },
                            trailingContent = {
                                Switch(
                                    checked = settings.showBluetooth,
                                    onCheckedChange = { show ->
                                        viewModel.updateSettings { it.copy(showBluetooth = show) }
                                    }
                                )
                            }
                        )
                    }

                    SettingsCardItem(position = GroupPosition.MIDDLE, isActive = settings.showCharging) {
                        ListItem(
                            colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                            headlineContent = { Text("Charging Indicator", fontWeight = FontWeight.SemiBold) },
                            supportingContent = { Text("Temporary alert when charger is connected") },
                            leadingContent = {
                                Icon(Icons.Default.Bolt, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                            },
                            trailingContent = {
                                Switch(
                                    checked = settings.showCharging,
                                    onCheckedChange = { show ->
                                        viewModel.updateSettings { it.copy(showCharging = show) }
                                    }
                                )
                            }
                        )
                    }

                    SettingsCardItem(position = GroupPosition.MIDDLE, isActive = settings.persistentCharging) {
                        ListItem(
                            colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                            headlineContent = { Text("Persistent Charging Indicator", fontWeight = FontWeight.SemiBold) },
                            supportingContent = { Text("Keep charging indicator active while plugged in") },
                            leadingContent = {
                                Icon(Icons.Default.Bolt, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                            },
                            trailingContent = {
                                Switch(
                                    checked = settings.persistentCharging,
                                    onCheckedChange = { show ->
                                        viewModel.updateSettings { it.copy(persistentCharging = show) }
                                    }
                                )
                            }
                        )
                    }

                    SettingsCardItem(position = GroupPosition.BOTTOM, isActive = settings.showRingerMode) {
                        ListItem(
                            colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                            headlineContent = { Text("Ring Modes", fontWeight = FontWeight.SemiBold) },
                            leadingContent = {
                                Icon(Icons.Default.NotificationsActive, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                            },
                            trailingContent = {
                                Switch(
                                    checked = settings.showRingerMode,
                                    onCheckedChange = { show ->
                                        viewModel.updateSettings { it.copy(showRingerMode = show) }
                                    }
                                )
                            }
                        )
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
        }
    }
}

    // App Filter Modal Bottom Sheet
    if (showAppFilterSheet) {
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        val coroutineScope = rememberCoroutineScope()

        ModalBottomSheet(
            onDismissRequest = { showAppFilterSheet = false },
            sheetState = sheetState
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 8.dp)
            ) {
                Text(
                    text = "Per-App Notification Filter",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = appFilterQuery,
                    onValueChange = { appFilterQuery = it },
                    placeholder = { Text("Search installed apps...") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(12.dp))

                val filteredApps = remember(installedApps, appFilterQuery) {
                    if (appFilterQuery.isBlank()) installedApps
                    else installedApps.filter {
                        it.appName.contains(appFilterQuery, ignoreCase = true) ||
                                it.packageName.contains(appFilterQuery, ignoreCase = true)
                    }
                }

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(400.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    items(filteredApps, key = { it.packageName }) { app ->
                        ListItem(
                            headlineContent = { Text(app.appName, fontWeight = FontWeight.Medium) },
                            leadingContent = {
                                if (app.icon != null) {
                                    val bitmap = remember(app.icon) { app.icon.toBitmap(96, 96) }
                                    Image(
                                        bitmap = bitmap.asImageBitmap(),
                                        contentDescription = null,
                                        modifier = Modifier
                                            .size(36.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                    )
                                } else {
                                    Icon(Icons.Default.Apps, contentDescription = null, modifier = Modifier.size(36.dp))
                                }
                            },
                            trailingContent = {
                                Switch(
                                    checked = !app.isBlocked,
                                    onCheckedChange = {
                                        viewModel.toggleAppBlocked(app.packageName)
                                    }
                                )
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }

    // Update Available Pop-up Shelf / Dialog
    if (showUpdateSheet) {
        val currentUpdate = (updateState as? MainScreenViewModel.UpdateState.Available)?.release
            ?: (updateState as? MainScreenViewModel.UpdateState.Downloading)?.release
        val downloadingProgress = (updateState as? MainScreenViewModel.UpdateState.Downloading)?.progress

        if (currentUpdate != null) {
            ModalBottomSheet(
                onDismissRequest = { viewModel.dismissUpdateSheet() },
                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 24.dp, end = 24.dp, top = 8.dp, bottom = 32.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = "Version ${currentUpdate.version}",
                                    style = MaterialTheme.typography.headlineSmall,
                                    fontWeight = FontWeight.Bold
                                )
                                if (currentUpdate.isBeta) {
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = MaterialTheme.colorScheme.tertiaryContainer
                                    ) {
                                        Text(
                                            text = "BETA",
                                            color = MaterialTheme.colorScheme.onTertiaryContainer,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                            }
                            Text(
                                text = currentUpdate.name.takeIf { it != currentUpdate.version && it != currentUpdate.tagName } ?: "New GitHub Release",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    if (currentUpdate.changelog.isNotBlank()) {
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            color = MaterialTheme.colorScheme.surfaceContainer
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = "What's New",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = currentUpdate.changelog,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }

                    if (downloadingProgress != null) {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Downloading APK...", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text("${(downloadingProgress * 100).roundToInt()}%", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                            LinearProgressIndicator(
                                progress = { downloadingProgress },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(8.dp)
                                    .clip(RoundedCornerShape(4.dp))
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedButton(
                            onClick = { viewModel.dismissUpdateSheet() },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Text("Later")
                        }

                        Button(
                            onClick = {
                                viewModel.downloadAndInstallUpdate(context, currentUpdate)
                            },
                            enabled = downloadingProgress == null,
                            modifier = Modifier.weight(1.5f),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            if (downloadingProgress != null) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(18.dp),
                                    strokeWidth = 2.dp,
                                    color = MaterialTheme.colorScheme.onPrimary
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Downloading...")
                            } else {
                                Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Install Update")
                            }
                        }
                    }
                }
            }
        }
    }

    // Feedback Bottom Sheet Dialog
    if (showFeedbackSheet) {
        ModalBottomSheet(
            onDismissRequest = { showFeedbackSheet = false },
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
            shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 24.dp, end = 24.dp, top = 4.dp, bottom = 32.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = "Send Feedback",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Report a bug or share your feature ideas for Droidbubble.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Category Selection: Bug Report vs Feature Suggestion
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = feedbackType == FeedbackType.BUG,
                        onClick = { feedbackType = FeedbackType.BUG },
                        label = { Text("Bug Report", fontWeight = FontWeight.SemiBold) },
                        leadingIcon = {
                            Icon(
                                Icons.Default.BugReport,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                        },
                        modifier = Modifier.weight(1f)
                    )
                    FilterChip(
                        selected = feedbackType == FeedbackType.FEATURE,
                        onClick = { feedbackType = FeedbackType.FEATURE },
                        label = { Text("Feature Idea", fontWeight = FontWeight.SemiBold) },
                        leadingIcon = {
                            Icon(
                                Icons.Default.Lightbulb,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                        },
                        modifier = Modifier.weight(1f)
                    )
                }

                // Subject / Title input
                OutlinedTextField(
                    value = feedbackTitle,
                    onValueChange = { feedbackTitle = it },
                    label = { Text("Subject") },
                    placeholder = {
                        Text(
                            if (feedbackType == FeedbackType.BUG) "e.g. Media progress slider glitch"
                            else "e.g. Add lock screen island support"
                        )
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                // Detailed Description input
                OutlinedTextField(
                    value = feedbackDescription,
                    onValueChange = { feedbackDescription = it },
                    label = { Text("Description *") },
                    placeholder = {
                        Text(
                            if (feedbackType == FeedbackType.BUG) "Describe what happened and how to reproduce it..."
                            else "Describe your idea and how it should work..."
                        )
                    },
                    minLines = 4,
                    maxLines = 7,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                // Attachment Section (optional)
                if (attachmentUri == null) {
                    OutlinedButton(
                        onClick = { filePickerLauncher.launch("*/*") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.AttachFile, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Add Attachment (Screenshot / Log)", fontSize = 13.sp)
                    }
                } else {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surfaceContainer
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.AttachFile,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = attachmentName ?: "Attached file",
                                style = MaterialTheme.typography.bodyMedium,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f)
                            )
                            IconButton(
                                onClick = {
                                    attachmentUri = null
                                    attachmentName = null
                                },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(
                                    Icons.Default.Close,
                                    contentDescription = "Remove attachment",
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }

                // Buttons: Cancel & Send Email
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = { showFeedbackSheet = false },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Text("Cancel")
                    }

                    Button(
                        onClick = {
                            val subject = "[Droidbubble ${if (feedbackType == FeedbackType.BUG) "Bug" else "Feature"}] ${feedbackTitle.ifBlank { if (feedbackType == FeedbackType.BUG) "Bug Report" else "Feature Suggestion" }}"
                            val body = buildString {
                                appendLine("Type: ${if (feedbackType == FeedbackType.BUG) "Bug Report" else "Feature Suggestion"}")
                                if (feedbackTitle.isNotBlank()) {
                                    appendLine("Subject: $feedbackTitle")
                                }
                                appendLine()
                                appendLine("Description:")
                                appendLine(feedbackDescription.trim())
                                appendLine()
                                appendLine("----------------------------------------")
                                appendLine("Device & App Info:")
                                appendLine("Droidbubble Version: ${com.droidbubble.app.util.UpdateManager.getCurrentVersion(context)} (${context.packageName})")
                                appendLine("Device: ${Build.MANUFACTURER} ${Build.MODEL} (${Build.DEVICE})")
                                appendLine("Android Version: ${Build.VERSION.RELEASE} (SDK ${Build.VERSION.SDK_INT})")
                                appendLine("Island Enabled: ${settings.isEnabled}")
                                appendLine("----------------------------------------")
                            }

                            val hasAttachment = attachmentUri != null
                            val emailIntent = if (hasAttachment) {
                                Intent(Intent.ACTION_SEND).apply {
                                    type = context.contentResolver.getType(attachmentUri!!) ?: "*/*"
                                    putExtra(Intent.EXTRA_EMAIL, arrayOf("velocityniele@gmail.com"))
                                    putExtra(Intent.EXTRA_SUBJECT, subject)
                                    putExtra(Intent.EXTRA_TEXT, body)
                                    putExtra(Intent.EXTRA_STREAM, attachmentUri)
                                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                }
                            } else {
                                Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:velocityniele@gmail.com")).apply {
                                    putExtra(Intent.EXTRA_EMAIL, arrayOf("velocityniele@gmail.com"))
                                    putExtra(Intent.EXTRA_SUBJECT, subject)
                                    putExtra(Intent.EXTRA_TEXT, body)
                                }
                            }

                            // Copy details to clipboard as backup in case user wants to paste elsewhere
                            try {
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
                                clipboard?.setPrimaryClip(ClipData.newPlainText("Droidbubble Feedback", body))
                            } catch (_: Exception) {}

                            val gmailIntent = Intent(emailIntent).setPackage("com.google.android.gm").apply {
                                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                if (hasAttachment) {
                                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                }
                            }

                            try {
                                if (context.packageManager.resolveActivity(gmailIntent, 0) != null) {
                                    context.startActivity(gmailIntent)
                                    Toast.makeText(context, "Opening Gmail to send to velocityniele@gmail.com", Toast.LENGTH_SHORT).show()
                                } else {
                                    val chooser = Intent.createChooser(emailIntent, "Send Feedback via Email").apply {
                                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                        if (hasAttachment) {
                                            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                        }
                                    }
                                    context.startActivity(chooser)
                                    Toast.makeText(context, "Choose email client to send to velocityniele@gmail.com", Toast.LENGTH_SHORT).show()
                                }
                            } catch (_: Exception) {
                                val fallback = Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:velocityniele@gmail.com?subject=" + Uri.encode(subject) + "&body=" + Uri.encode(body))).apply {
                                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                }
                                runCatching {
                                    context.startActivity(fallback)
                                    Toast.makeText(context, "Opening mail client...", Toast.LENGTH_SHORT).show()
                                }
                            }

                            feedbackTitle = ""
                            feedbackDescription = ""
                            attachmentUri = null
                            attachmentName = null
                            showFeedbackSheet = false
                        },
                        enabled = feedbackDescription.isNotBlank(),
                        modifier = Modifier.weight(1.5f),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Icon(Icons.Default.Send, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Send Email")
                    }
                }
            }
        }
    }


}

@Composable
fun SectionHeader(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    badge: String? = null,
    badgeColor: Color = MaterialTheme.colorScheme.primary
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 12.dp, bottom = 4.dp, start = 4.dp, end = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        if (badge != null) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(badgeColor.copy(alpha = 0.2f))
                    .padding(horizontal = 8.dp, vertical = 2.dp)
            ) {
                Text(
                    text = badge,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = badgeColor
                )
            }
        }
    }
}

enum class GroupPosition {
    TOP,
    MIDDLE,
    BOTTOM,
    SINGLE
}

fun getGroupedShape(position: GroupPosition): RoundedCornerShape {
    val outerRadius = 22.dp
    val innerRadius = 4.dp
    return when (position) {
        GroupPosition.TOP -> RoundedCornerShape(
            topStart = outerRadius,
            topEnd = outerRadius,
            bottomStart = innerRadius,
            bottomEnd = innerRadius
        )
        GroupPosition.MIDDLE -> RoundedCornerShape(innerRadius)
        GroupPosition.BOTTOM -> RoundedCornerShape(
            topStart = innerRadius,
            topEnd = innerRadius,
            bottomStart = outerRadius,
            bottomEnd = outerRadius
        )
        GroupPosition.SINGLE -> RoundedCornerShape(outerRadius)
    }
}

@Composable
fun SettingsCardItem(
    position: GroupPosition,
    modifier: Modifier = Modifier,
    isActive: Boolean = false,
    content: @Composable () -> Unit
) {
    val effectiveShape = if (isActive) {
        RoundedCornerShape(20.dp)
    } else {
        getGroupedShape(position)
    }

    val animatedColor by animateColorAsState(
        targetValue = if (isActive) {
            MaterialTheme.colorScheme.surfaceContainerHighest
        } else {
            MaterialTheme.colorScheme.surfaceContainerLow
        },
        label = "itemBgColor"
    )

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = effectiveShape,
        color = animatedColor,
        tonalElevation = if (isActive) 3.dp else 1.dp
    ) {
        content()
    }
}

@Composable
fun SliderSettingItem(
    label: String,
    value: Float,
    unit: String,
    valueRange: ClosedFloatingPointRange<Float>,
    steps: Int = 0,
    onValueChange: (Float) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                label,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f))
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Text(
                    text = "${if (unit == "x" || unit == "dp") String.format(Locale.US, "%.1f", value).replace(".0", "") else value.toInt()} $unit",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = valueRange,
            steps = steps,
            modifier = Modifier.height(24.dp)
        )
    }
}

enum class FeedbackType {
    BUG,
    FEATURE
}

private fun getAttachmentFileName(context: Context, uri: Uri): String {
    var name = "attachment"
    try {
        context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            if (cursor.moveToFirst() && nameIndex >= 0) {
                name = cursor.getString(nameIndex)
            }
        }
    } catch (_: Exception) {
    }
    return name
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DimensionsPage(
    settings: IslandSettings,
    onUpdateSettings: ((IslandSettings) -> IslandSettings) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler(onBack = onBack)

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text("Island Dimensions", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                val currentCutoutMode = when {
                    settings.isTeardropCutout -> CutoutModeOption.TEARDROP
                    settings.isLeftCutout -> CutoutModeOption.LEFT
                    else -> CutoutModeOption.CENTER
                }
                CutoutPositionSelectorCard(
                    selectedMode = currentCutoutMode,
                    onSelectMode = { mode ->
                        onUpdateSettings { s ->
                            when (mode) {
                                CutoutModeOption.CENTER -> s.copy(isLeftCutout = false, isTeardropCutout = false)
                                CutoutModeOption.LEFT -> s.copy(isLeftCutout = true, isTeardropCutout = false)
                                CutoutModeOption.TEARDROP -> s.copy(isLeftCutout = false, isTeardropCutout = true)
                            }
                        }
                    }
                )
            }

            item {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    SettingsCardItem(position = GroupPosition.TOP) {
                        SliderSettingItem(
                            label = "Width",
                            value = settings.widthDp.toFloat(),
                            unit = "dp",
                            valueRange = 50f..260f,
                            onValueChange = { onUpdateSettings { s -> s.copy(widthDp = it.roundToInt()) } }
                        )
                    }

                    SettingsCardItem(position = GroupPosition.MIDDLE) {
                        SliderSettingItem(
                            label = "Height",
                            value = settings.heightDp.toFloat(),
                            unit = "dp",
                            valueRange = 12f..56f,
                            onValueChange = { onUpdateSettings { s -> s.copy(heightDp = it.roundToInt()) } }
                        )
                    }

                    SettingsCardItem(position = GroupPosition.MIDDLE) {
                        SliderSettingItem(
                            label = "Corner Radius",
                            value = settings.cornerRadiusDp.toFloat(),
                            unit = "dp",
                            valueRange = 10f..28f,
                            onValueChange = { onUpdateSettings { s -> s.copy(cornerRadiusDp = it.roundToInt()) } }
                        )
                    }

                    SettingsCardItem(position = GroupPosition.MIDDLE) {
                        SliderSettingItem(
                            label = "Top Offset (Notch Line-up)",
                            value = settings.topOffsetDp.toFloat(),
                            unit = "dp",
                            valueRange = 0f..60f,
                            onValueChange = { onUpdateSettings { s -> s.copy(topOffsetDp = it.roundToInt()) } }
                        )
                    }

                    SettingsCardItem(position = GroupPosition.MIDDLE) {
                        SliderSettingItem(
                            label = "Horizontal Offset",
                            value = settings.horizontalOffsetDp.toFloat(),
                            unit = "dp",
                            valueRange = -50f..50f,
                            onValueChange = { onUpdateSettings { s -> s.copy(horizontalOffsetDp = it.roundToInt()) } }
                        )
                    }

                    SettingsCardItem(position = GroupPosition.MIDDLE) {
                        SliderSettingItem(
                            label = "Expanded Card Scale",
                            value = settings.expandedScale,
                            unit = "x",
                            valueRange = 0.8f..1.3f,
                            steps = 5,
                            onValueChange = { onUpdateSettings { s -> s.copy(expandedScale = (it * 10).roundToInt() / 10f) } }
                        )
                    }

                    SettingsCardItem(position = GroupPosition.BOTTOM) {
                        SliderSettingItem(
                            label = "Animation Speed",
                            value = settings.animationSpeed,
                            unit = "x",
                            valueRange = 0.5f..2.0f,
                            steps = 14,
                            onValueChange = { onUpdateSettings { s -> s.copy(animationSpeed = (it * 10).roundToInt() / 10f) } }
                        )
                    }
                }
            }
        }
    }
}

enum class CutoutModeOption {
    CENTER,
    LEFT,
    TEARDROP
}

@Composable
fun CutoutPositionSelectorCard(
    selectedMode: CutoutModeOption,
    onSelectMode: (CutoutModeOption) -> Unit
) {
    SettingsCardItem(position = GroupPosition.SINGLE) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Camera Cut-out Position",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                CutoutPositionOptionButton(
                    label = "Center",
                    mode = CutoutModeOption.CENTER,
                    isSelected = selectedMode == CutoutModeOption.CENTER,
                    onClick = { onSelectMode(CutoutModeOption.CENTER) },
                    modifier = Modifier.weight(1f)
                )
                CutoutPositionOptionButton(
                    label = "Left",
                    mode = CutoutModeOption.LEFT,
                    isSelected = selectedMode == CutoutModeOption.LEFT,
                    onClick = { onSelectMode(CutoutModeOption.LEFT) },
                    modifier = Modifier.weight(1f)
                )
                CutoutPositionOptionButton(
                    label = "Tear-drop",
                    mode = CutoutModeOption.TEARDROP,
                    isSelected = selectedMode == CutoutModeOption.TEARDROP,
                    onClick = { onSelectMode(CutoutModeOption.TEARDROP) },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun CutoutPositionOptionButton(
    label: String,
    mode: CutoutModeOption,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val containerColor by animateColorAsState(
        targetValue = if (isSelected) {
            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.65f)
        } else {
            MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.55f)
        },
        label = "cutoutBtnBg"
    )
    val borderColor by animateColorAsState(
        targetValue = if (isSelected) {
            MaterialTheme.colorScheme.primary
        } else {
            MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f)
        },
        label = "cutoutBtnBorder"
    )
    val contentColor by animateColorAsState(
        targetValue = if (isSelected) {
            MaterialTheme.colorScheme.onPrimaryContainer
        } else {
            MaterialTheme.colorScheme.onSurfaceVariant
        },
        label = "cutoutBtnContent"
    )
    val bezelStrokeColor = if (isSelected) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
    }
    val screenFillColor = if (isSelected) {
        MaterialTheme.colorScheme.primary.copy(alpha = 0.10f)
    } else {
        MaterialTheme.colorScheme.surfaceContainerLow.copy(alpha = 0.55f)
    }
    val cutoutDotColor = if (isSelected) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }

    Surface(
        onClick = onClick,
        modifier = modifier.height(106.dp),
        shape = RoundedCornerShape(18.dp),
        color = containerColor,
        border = BorderStroke(if (isSelected) 2.dp else 1.dp, borderColor)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 12.dp, bottom = 10.dp, start = 6.dp, end = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Cropped top phone bezel illustration with just the selfie camera circle (or tear-drop notch)
            androidx.compose.foundation.Canvas(
                modifier = Modifier
                    .width(74.dp)
                    .height(50.dp)
            ) {
                val strokePx = 2.3.dp.toPx()
                val cornerPx = 13.dp.toPx()
                val padX = 4.dp.toPx()
                val topY = 4.dp.toPx()
                val bottomY = size.height
                val leftX = padX
                val rightX = size.width - padX

                val bezelFillPath = androidx.compose.ui.graphics.Path().apply {
                    moveTo(leftX, bottomY)
                    lineTo(leftX, topY + cornerPx)
                    quadraticTo(leftX, topY, leftX + cornerPx, topY)
                    lineTo(rightX - cornerPx, topY)
                    quadraticTo(rightX, topY, rightX, topY + cornerPx)
                    lineTo(rightX, bottomY)
                    close()
                }
                drawPath(path = bezelFillPath, color = screenFillColor)

                val bezelOutlinePath = androidx.compose.ui.graphics.Path().apply {
                    moveTo(leftX, bottomY)
                    lineTo(leftX, topY + cornerPx)
                    quadraticTo(leftX, topY, leftX + cornerPx, topY)
                    lineTo(rightX - cornerPx, topY)
                    quadraticTo(rightX, topY, rightX, topY + cornerPx)
                    lineTo(rightX, bottomY)
                }
                drawPath(
                    path = bezelOutlinePath,
                    color = bezelStrokeColor,
                    style = androidx.compose.ui.graphics.drawscope.Stroke(
                        width = strokePx,
                        cap = androidx.compose.ui.graphics.StrokeCap.Round,
                        join = androidx.compose.ui.graphics.StrokeJoin.Round
                    )
                )

                val centerX = size.width / 2f
                when (mode) {
                    CutoutModeOption.CENTER -> {
                        val holeY = topY + 12.dp.toPx()
                        val holeRadius = 4.2.dp.toPx()
                        drawCircle(
                            color = cutoutDotColor,
                            radius = holeRadius,
                            center = androidx.compose.ui.geometry.Offset(centerX, holeY)
                        )
                    }
                    CutoutModeOption.LEFT -> {
                        val holeY = topY + 12.dp.toPx()
                        val holeRadius = 4.2.dp.toPx()
                        val holeX = leftX + 14.dp.toPx()
                        drawCircle(
                            color = cutoutDotColor,
                            radius = holeRadius,
                            center = androidx.compose.ui.geometry.Offset(holeX, holeY)
                        )
                    }
                    CutoutModeOption.TEARDROP -> {
                        // Outward-rounded top corners blending from the top bezel into a centered tear-drop semicircle
                        val dropR = 7.dp.toPx()
                        val earR = 5.dp.toPx()
                        val dropBottomY = topY + dropR * 1.45f
                        val teardropPath = androidx.compose.ui.graphics.Path().apply {
                            moveTo(centerX - dropR - earR, topY)
                            quadraticTo(
                                centerX - dropR, topY,
                                centerX - dropR * 0.88f, topY + dropR * 0.55f
                            )
                            cubicTo(
                                centerX - dropR * 0.65f, dropBottomY,
                                centerX + dropR * 0.65f, dropBottomY,
                                centerX + dropR * 0.88f, topY + dropR * 0.55f
                            )
                            quadraticTo(
                                centerX + dropR, topY,
                                centerX + dropR + earR, topY
                            )
                            close()
                        }
                        drawPath(path = teardropPath, color = cutoutDotColor)
                    }
                }
            }

            Text(
                text = label,
                fontSize = 12.5.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold,
                color = contentColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}



@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppearancePage(
    settings: IslandSettings,
    onUpdateSettings: ((IslandSettings) -> IslandSettings) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler(onBack = onBack)

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text("Appearance & Feedback", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    val outlineActive = settings.showOutline
                    SettingsCardItem(position = if (outlineActive) GroupPosition.TOP else GroupPosition.SINGLE, isActive = settings.showOutline) {
                        ListItem(
                            colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                            headlineContent = { Text("Island Outline", fontWeight = FontWeight.SemiBold) },
                            leadingContent = {
                                Icon(Icons.Default.CropSquare, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                            },
                            trailingContent = {
                                Switch(
                                    checked = settings.showOutline,
                                    onCheckedChange = { show ->
                                        onUpdateSettings { it.copy(showOutline = show) }
                                    }
                                )
                            }
                        )
                    }

                    if (outlineActive) {
                        SettingsCardItem(position = GroupPosition.BOTTOM) {
                            SliderSettingItem(
                                label = "Outline Thickness",
                                value = settings.outlineThicknessDp,
                                unit = "dp",
                                valueRange = 0.5f..4.0f,
                                onValueChange = { onUpdateSettings { s -> s.copy(outlineThicknessDp = (it * 10).roundToInt() / 10f) } }
                            )
                        }
                    }
                }
            }

            item {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    SettingsCardItem(position = GroupPosition.TOP, isActive = settings.bubbleEffectEnabled) {
                        ListItem(
                            colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                            headlineContent = { Text("Bubble Effect", fontWeight = FontWeight.SemiBold) },
                            leadingContent = {
                                Icon(Icons.Default.RoundedCorner, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                            },
                            trailingContent = {
                                Switch(
                                    checked = settings.bubbleEffectEnabled,
                                    onCheckedChange = { enabled ->
                                        onUpdateSettings { it.copy(bubbleEffectEnabled = enabled) }
                                    }
                                )
                            }
                        )
                    }

                    SettingsCardItem(position = GroupPosition.MIDDLE, isActive = settings.showDualActivityInCompact) {
                        ListItem(
                            colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                            headlineContent = { Text("Dual Activity on Minimised Island", fontWeight = FontWeight.SemiBold) },
                            supportingContent = { Text("Shows secondary bubble; when off, shows only priority activity until expanded", style = MaterialTheme.typography.bodySmall) },
                            leadingContent = {
                                Icon(Icons.Default.Widgets, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                            },
                            trailingContent = {
                                Switch(
                                    checked = settings.showDualActivityInCompact,
                                    onCheckedChange = { showDual ->
                                        onUpdateSettings { it.copy(showDualActivityInCompact = showDual) }
                                    }
                                )
                            }
                        )
                    }

                    SettingsCardItem(position = GroupPosition.MIDDLE, isActive = settings.alwaysShowPill) {
                        ListItem(
                            colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                            headlineContent = { Text("Always Show Pill", fontWeight = FontWeight.SemiBold) },
                            leadingContent = {
                                Icon(Icons.Default.FitScreen, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                            },
                            trailingContent = {
                                Switch(
                                    checked = settings.alwaysShowPill,
                                    onCheckedChange = { alwaysShow ->
                                        onUpdateSettings { it.copy(alwaysShowPill = alwaysShow) }
                                    }
                                )
                            }
                        )
                    }

                    SettingsCardItem(position = GroupPosition.MIDDLE, isActive = settings.hapticsEnabled) {
                        ListItem(
                            colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                            headlineContent = { Text("Haptic Feedback", fontWeight = FontWeight.SemiBold) },
                            leadingContent = {
                                Icon(Icons.Default.Vibration, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                            },
                            trailingContent = {
                                Switch(
                                    checked = settings.hapticsEnabled,
                                    onCheckedChange = { haptics ->
                                        onUpdateSettings { it.copy(hapticsEnabled = haptics) }
                                    }
                                )
                            }
                        )
                    }

                    SettingsCardItem(position = GroupPosition.BOTTOM, isActive = settings.showInLandscape) {
                        ListItem(
                            colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                            headlineContent = { Text("Show in Landscape", fontWeight = FontWeight.SemiBold) },
                            leadingContent = {
                                Icon(Icons.Default.ScreenRotation, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                            },
                            trailingContent = {
                                Switch(
                                    checked = settings.showInLandscape,
                                    onCheckedChange = { inLandscape ->
                                        onUpdateSettings { it.copy(showInLandscape = inLandscape) }
                                    }
                                )
                            }
                        )
                    }
                }
            }

            item {
                SectionHeader(
                    title = "Media Player Customisation",
                    icon = Icons.Default.MusicNote
                )
            }

            item {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    SettingsCardItem(position = GroupPosition.TOP, isActive = settings.mediaWaveAnimation) {
                        ListItem(
                            colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                            headlineContent = { Text("Ambient Album Wave", fontWeight = FontWeight.SemiBold) },
                            leadingContent = {
                                Icon(Icons.Default.GraphicEq, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                            },
                            trailingContent = {
                                Switch(
                                    checked = settings.mediaWaveAnimation,
                                    onCheckedChange = { enabled ->
                                        onUpdateSettings { it.copy(mediaWaveAnimation = enabled) }
                                    }
                                )
                            }
                        )
                    }

                    SettingsCardItem(position = GroupPosition.MIDDLE, isActive = settings.wavyProgressBar) {
                        ListItem(
                            colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                            headlineContent = { Text("Wavy Progress Scrubber", fontWeight = FontWeight.SemiBold) },
                            leadingContent = {
                                Icon(Icons.Default.LinearScale, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                            },
                            trailingContent = {
                                Switch(
                                    checked = settings.wavyProgressBar,
                                    onCheckedChange = { enabled ->
                                        onUpdateSettings { it.copy(wavyProgressBar = enabled) }
                                    }
                                )
                            }
                        )
                    }

                    SettingsCardItem(position = GroupPosition.MIDDLE, isActive = settings.spinningAlbumArt) {
                        ListItem(
                            colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                            headlineContent = { Text("Spinning Vinyl Cover", fontWeight = FontWeight.SemiBold) },
                            leadingContent = {
                                Icon(Icons.Default.Album, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                            },
                            trailingContent = {
                                Switch(
                                    checked = settings.spinningAlbumArt,
                                    onCheckedChange = { enabled ->
                                        onUpdateSettings { it.copy(spinningAlbumArt = enabled) }
                                    }
                                )
                            }
                        )
                    }

                    SettingsCardItem(position = GroupPosition.BOTTOM, isActive = settings.blurredAlbumArtBackground) {
                        ListItem(
                            colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                            headlineContent = { Text("Blurred Album Art Background", fontWeight = FontWeight.SemiBold) },
                            leadingContent = {
                                Icon(Icons.Default.MusicNote, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                            },
                            trailingContent = {
                                Switch(
                                    checked = settings.blurredAlbumArtBackground,
                                    onCheckedChange = { blurred ->
                                        onUpdateSettings { it.copy(blurredAlbumArtBackground = blurred) }
                                    }
                                )
                            }
                        )
                    }
                }
            }

            item {
                AnimationCurveEditor(settings = settings, onUpdateSettings = onUpdateSettings)
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccessibilityPage(
    settings: IslandSettings,
    onUpdateSettings: ((IslandSettings) -> IslandSettings) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler(onBack = onBack)

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text("Accessibility", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    SettingsCardItem(position = GroupPosition.TOP, isActive = settings.autoDismissNotifications) {
                        ListItem(
                            colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                            headlineContent = { Text("Auto-Dismiss Notifications", fontWeight = FontWeight.SemiBold) },
                            supportingContent = { Text("Dismiss notification cards automatically after ${settings.notificationAutoDismissSeconds} seconds") },
                            leadingContent = {
                                Icon(Icons.Default.Timer, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                            },
                            trailingContent = {
                                Switch(
                                    checked = settings.autoDismissNotifications,
                                    onCheckedChange = { autoDismiss ->
                                        onUpdateSettings { it.copy(autoDismissNotifications = autoDismiss) }
                                    }
                                )
                            }
                        )
                    }

                    if (settings.autoDismissNotifications) {
                        SettingsCardItem(position = GroupPosition.MIDDLE) {
                            SliderSettingItem(
                                label = "Auto-Dismiss Duration",
                                value = settings.notificationAutoDismissSeconds.toFloat(),
                                unit = "s",
                                valueRange = 1f..10f,
                                steps = 8,
                                onValueChange = { seconds ->
                                    onUpdateSettings { it.copy(notificationAutoDismissSeconds = seconds.roundToInt().coerceIn(1, 10)) }
                                }
                            )
                        }
                    }

                    SettingsCardItem(position = GroupPosition.MIDDLE, isActive = settings.dismissMediaOnPause) {
                        ListItem(
                            colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                            headlineContent = { Text("Dismiss on Pause", fontWeight = FontWeight.SemiBold) },
                            supportingContent = { Text("Automatically hide the media player island when playback is paused") },
                            leadingContent = {
                                Icon(Icons.Default.MusicNote, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                            },
                            trailingContent = {
                                Switch(
                                    checked = settings.dismissMediaOnPause,
                                    onCheckedChange = { dismiss ->
                                        onUpdateSettings { it.copy(dismissMediaOnPause = dismiss) }
                                    }
                                )
                            }
                        )
                    }

                    SettingsCardItem(position = GroupPosition.BOTTOM, isActive = settings.hideOnLockScreen) {
                        ListItem(
                            colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                            headlineContent = { Text("Hide on Lock Screen", fontWeight = FontWeight.SemiBold) },
                            supportingContent = { Text("Do not show the island when the phone is locked, even if an activity is active") },
                            leadingContent = {
                                Icon(Icons.Default.Lock, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                            },
                            trailingContent = {
                                Switch(
                                    checked = settings.hideOnLockScreen,
                                    onCheckedChange = { hide ->
                                        onUpdateSettings { it.copy(hideOnLockScreen = hide) }
                                    }
                                )
                            }
                        )
                    }
                }
            }
        }
    }
}


private fun springStep(damping: Float, stiffness: Float, t: Float): Float {
    val w = Math.sqrt(stiffness.toDouble())
    val z = damping.toDouble()
    return if (z < 1.0) {
        val wd = w * Math.sqrt(1.0 - z * z)
        (1.0 - Math.exp(-z * w * t) * (Math.cos(wd * t) + (z * w / wd) * Math.sin(wd * t))).toFloat()
    } else {
        (1.0 - Math.exp(-w * t) * (1.0 + w * t)).toFloat()
    }
}

@Composable
fun AnimationCurveEditor(
    settings: IslandSettings,
    onUpdateSettings: ((IslandSettings) -> IslandSettings) -> Unit
) {
    val targets = listOf("Expand", "Appear", "Split")
    var selected by remember { mutableStateOf(0) }
    val defaults = listOf(0.74f to 380f, 0.60f to 280f, 0.63f to 320f)
    val damping = when (selected) { 0 -> settings.expandDamping; 1 -> settings.appearDamping; else -> settings.splitDamping }
    val stiffness = when (selected) { 0 -> settings.expandStiffness; 1 -> settings.appearStiffness; else -> settings.splitStiffness }
    val isDefault = damping == defaults[selected].first && stiffness == defaults[selected].second

    fun setCurve(d: Float, k: Float) {
        onUpdateSettings {
            when (selected) {
                0 -> it.copy(expandDamping = d, expandStiffness = k)
                1 -> it.copy(appearDamping = d, appearStiffness = k)
                else -> it.copy(splitDamping = d, splitStiffness = k)
            }
        }
    }

    SettingsCardItem(position = GroupPosition.SINGLE) {
        Column(modifier = Modifier.fillMaxWidth().padding(vertical = 14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.Animation, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.width(16.dp))
                Text("Animation Curves", fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
                TextButton(onClick = { setCurve(defaults[selected].first, defaults[selected].second) }, enabled = !isDefault) {
                    Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Reset")
                }
            }
            Spacer(Modifier.height(10.dp))

            // Expand / Appear / Split segmented buttons matching screen record & live progress cards
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                targets.forEachIndexed { i, label ->
                    val isSelected = selected == i
                    val shape = when (i) {
                        0 -> RoundedCornerShape(
                            topStart = 14.dp,
                            bottomStart = 14.dp,
                            topEnd = 4.dp,
                            bottomEnd = 4.dp
                        )
                        targets.size - 1 -> RoundedCornerShape(
                            topStart = 4.dp,
                            bottomStart = 4.dp,
                            topEnd = 14.dp,
                            bottomEnd = 14.dp
                        )
                        else -> RoundedCornerShape(4.dp)
                    }

                    FilledTonalButton(
                        onClick = { selected = i },
                        modifier = Modifier
                            .weight(1f)
                            .height(38.dp),
                        shape = shape,
                        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 0.dp),
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.6f),
                            contentColor = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    ) {
                        Text(
                            text = label,
                            fontSize = 13.5.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
            Spacer(Modifier.height(14.dp))

            val lineColor = MaterialTheme.colorScheme.primary
            val guideColor = MaterialTheme.colorScheme.outlineVariant
            Box(
                modifier = Modifier
                    .padding(horizontal = 20.dp)
                    .fillMaxWidth()
                    .height(120.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.6f))
            ) {
                androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize()) {
                    val padX = 14.dp.toPx()
                    val padY = 22.dp.toPx()
                    val w = size.width - padX * 2
                    val h = size.height - padY * 2
                    val maxV = 1.5f
                    val dash = androidx.compose.ui.graphics.PathEffect.dashPathEffect(floatArrayOf(10f, 10f))
                    val targetY = padY + h * (1f - 1f / maxV)
                    drawLine(guideColor, androidx.compose.ui.geometry.Offset(padX, targetY), androidx.compose.ui.geometry.Offset(padX + w, targetY), strokeWidth = 1.5f, pathEffect = dash)
                    val baseY = padY + h
                    drawLine(guideColor, androidx.compose.ui.geometry.Offset(padX, baseY), androidx.compose.ui.geometry.Offset(padX + w, baseY), strokeWidth = 1.5f)
                    val path = androidx.compose.ui.graphics.Path()
                    val n = 80
                    for (i in 0..n) {
                        val tt = i / n.toFloat()
                        val v = springStep(damping, stiffness, tt * 1.0f).coerceIn(0f, maxV)
                        val x = padX + w * tt
                        val y = padY + h * (1f - v / maxV)
                        if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
                    }
                    drawPath(
                        path, lineColor,
                        style = androidx.compose.ui.graphics.drawscope.Stroke(
                            width = 3.dp.toPx(),
                            cap = androidx.compose.ui.graphics.StrokeCap.Round,
                            join = androidx.compose.ui.graphics.StrokeJoin.Round
                        )
                    )
                }
            }

            val bounce = ((1f - damping) / 0.7f).coerceIn(0f, 1f)
            SliderSettingItemPct(
                label = "Bounce",
                valueText = "${(bounce * 100).toInt()}%",
                value = bounce,
                valueRange = 0f..1f,
                onValueChange = { b -> setCurve((1f - 0.7f * b), stiffness) }
            )
            SliderSettingItemPct(
                label = "Snap",
                valueText = "${stiffness.toInt()}",
                value = stiffness,
                valueRange = 150f..700f,
                onValueChange = { k -> setCurve(damping, k.roundToInt().toFloat()) }
            )
        }
    }
}

@Composable
private fun SliderSettingItemPct(
    label: String,
    valueText: String,
    value: Float,
    valueRange: ClosedFloatingPointRange<Float>,
    onValueChange: (Float) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 8.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(label, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f))
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Text(valueText, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            }
        }
        Slider(value = value, onValueChange = onValueChange, valueRange = valueRange, modifier = Modifier.height(28.dp))
    }
}
