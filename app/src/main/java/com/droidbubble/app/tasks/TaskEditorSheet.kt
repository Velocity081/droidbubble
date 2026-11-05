package com.droidbubble.app.tasks

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.droidbubble.app.ui.main.AppPickerDialog
import com.droidbubble.app.ui.main.GroupPosition
import com.droidbubble.app.ui.main.getGroupedShape
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskEditorScreen(
    initialShortcut: TaskShortcut?,
    onSave: (TaskShortcut) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var name by remember { mutableStateOf(initialShortcut?.name ?: "") }
    var selectedIconName by remember { mutableStateOf(initialShortcut?.iconName ?: "Bolt") }
    var showInBubble by remember { mutableStateOf(initialShortcut?.showInBubble ?: true) }
    var actions by remember { mutableStateOf(initialShortcut?.actions ?: emptyList()) }

    var showIconPicker by remember { mutableStateOf(false) }
    var showAddActionDialog by remember { mutableStateOf(false) }
    var showSubActionDialog by remember { mutableStateOf<String?>(null) }
    var showAppPicker by remember { mutableStateOf(false) }

    BackHandler {
        when {
            showIconPicker -> showIconPicker = false
            showSubActionDialog != null -> showSubActionDialog = null
            showAddActionDialog -> showAddActionDialog = false
            showAppPicker -> showAppPicker = false
            else -> onBack()
        }
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (initialShortcut == null) "New Shortcut" else "Edit Shortcut",
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    TextButton(
                        onClick = {
                            if (name.isNotBlank()) {
                                onSave(
                                    TaskShortcut(
                                        id = initialShortcut?.id ?: java.util.UUID.randomUUID().toString(),
                                        name = name.trim(),
                                        iconName = selectedIconName,
                                        showInBubble = showInBubble,
                                        actions = actions
                                    )
                                )
                            }
                        },
                        enabled = name.isNotBlank() && actions.isNotEmpty()
                    ) {
                        Text("Save", fontWeight = FontWeight.Bold)
                    }
                }
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 1. Icon & Name Row
            item {
                Surface(
                    shape = RoundedCornerShape(24.dp),
                    color = MaterialTheme.colorScheme.surfaceContainerHigh,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Surface(
                            modifier = Modifier
                                .size(56.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .clickable { showIconPicker = true },
                            shape = RoundedCornerShape(16.dp),
                            color = MaterialTheme.colorScheme.primaryContainer,
                            contentColor = MaterialTheme.colorScheme.primary
                        ) {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = TaskIcons.getIcon(selectedIconName),
                                    contentDescription = "Pick icon",
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                        }

                        OutlinedTextField(
                            value = name,
                            onValueChange = { name = it },
                            label = { Text("Shortcut Name") },
                            singleLine = true,
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // 2. Droidbubble feedback toggle
            item {
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = MaterialTheme.colorScheme.surfaceContainerHigh,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    ListItem(
                        colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                        headlineContent = { Text("Show in Droidbubble", fontWeight = FontWeight.Medium) },
                        leadingContent = {
                            Icon(
                                imageVector = Icons.Default.Widgets,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                        },
                        trailingContent = {
                            Switch(
                                checked = showInBubble,
                                onCheckedChange = { showInBubble = it }
                            )
                        }
                    )
                }
            }

            // 3. Actions Section Header
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Actions (${actions.size})",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            // 4. Chained Action Blocks List (Grouped M3 style with reduced spacing)
            if (actions.isEmpty()) {
                item {
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = MaterialTheme.colorScheme.surfaceContainerLow,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Extension,
                                contentDescription = null,
                                modifier = Modifier.size(36.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                            )
                            Text(
                                text = "No actions chained yet",
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            } else {
                item {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(3.dp)
                    ) {
                        actions.forEachIndexed { index, action ->
                            val position = when {
                                actions.size == 1 -> GroupPosition.SINGLE
                                index == 0 -> GroupPosition.TOP
                                index == actions.size - 1 -> GroupPosition.BOTTOM
                                else -> GroupPosition.MIDDLE
                            }
                            val cardShape = getGroupedShape(position)

                            Surface(
                                shape = cardShape,
                                color = MaterialTheme.colorScheme.surfaceContainerHigh,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.fillMaxWidth()) {
                                    ListItem(
                                        colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                                        headlineContent = {
                                            Text(
                                                text = action.getDisplayName(),
                                                fontWeight = FontWeight.SemiBold,
                                                fontSize = 15.sp
                                            )
                                        },
                                        leadingContent = {
                                            Surface(
                                                shape = RoundedCornerShape(10.dp),
                                                color = MaterialTheme.colorScheme.primaryContainer,
                                                modifier = Modifier.size(36.dp)
                                            ) {
                                                Box(
                                                    modifier = Modifier.fillMaxSize(),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Icon(
                                                        imageVector = action.getIcon(),
                                                        contentDescription = null,
                                                        tint = MaterialTheme.colorScheme.primary,
                                                        modifier = Modifier.size(20.dp)
                                                    )
                                                }
                                            }
                                        },
                                        trailingContent = {
                                            IconButton(
                                                onClick = {
                                                    actions = actions.filterIndexed { i, _ -> i != index }
                                                },
                                                modifier = Modifier.size(32.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Close,
                                                    contentDescription = "Remove",
                                                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.55f),
                                                    modifier = Modifier.size(18.dp)
                                                )
                                            }
                                        }
                                    )

                                    // Inline customization for Send Notification
                                    if (action is TaskAction.SendNotification) {
                                        Column(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(start = 58.dp, end = 16.dp, bottom = 12.dp),
                                            verticalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            OutlinedTextField(
                                                value = action.title,
                                                onValueChange = { newTitle ->
                                                    actions = actions.toMutableList().also { list ->
                                                        list[index] = action.copy(title = newTitle)
                                                    }
                                                },
                                                label = { Text("Notification Title", fontSize = 12.sp) },
                                                singleLine = true,
                                                shape = RoundedCornerShape(12.dp),
                                                modifier = Modifier.fillMaxWidth()
                                            )
                                            OutlinedTextField(
                                                value = action.text,
                                                onValueChange = { newText ->
                                                    actions = actions.toMutableList().also { list ->
                                                        list[index] = action.copy(text = newText)
                                                    }
                                                },
                                                label = { Text("Notification Text", fontSize = 12.sp) },
                                                singleLine = true,
                                                shape = RoundedCornerShape(12.dp),
                                                modifier = Modifier.fillMaxWidth()
                                            )
                                        }
                                    }

                                    // Inline slider for Set Brightness
                                    if (action is TaskAction.SetBrightness) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(start = 58.dp, end = 16.dp, bottom = 10.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Slider(
                                                value = action.level.toFloat(),
                                                onValueChange = { lvl ->
                                                    actions = actions.toMutableList().also { list ->
                                                        list[index] = TaskAction.SetBrightness(lvl.roundToInt())
                                                    }
                                                },
                                                valueRange = 0f..100f,
                                                modifier = Modifier.weight(1f)
                                            )
                                            Spacer(modifier = Modifier.width(10.dp))
                                            Text(
                                                text = "${action.level}%",
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 13.sp,
                                                color = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.width(42.dp)
                                            )
                                        }
                                    }

                                    // Inline slider for Set Volume
                                    if (action is TaskAction.SetVolume) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(start = 58.dp, end = 16.dp, bottom = 10.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Slider(
                                                value = action.level.toFloat(),
                                                onValueChange = { lvl ->
                                                    actions = actions.toMutableList().also { list ->
                                                        list[index] = TaskAction.SetVolume(lvl.roundToInt())
                                                    }
                                                },
                                                valueRange = 0f..100f,
                                                modifier = Modifier.weight(1f)
                                            )
                                            Spacer(modifier = Modifier.width(10.dp))
                                            Text(
                                                text = "${action.level}%",
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 13.sp,
                                                color = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.width(42.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // 5. Add Action Button
            item {
                FilledTonalButton(
                    onClick = { showAddActionDialog = true },
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Add Action", fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }

    // Modal Icon Picker Dialog
    if (showIconPicker) {
        AlertDialog(
            onDismissRequest = { showIconPicker = false },
            title = { Text("Choose Icon", fontWeight = FontWeight.Bold) },
            text = {
                Box(modifier = Modifier.size(width = 300.dp, height = 340.dp)) {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(4),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(TaskIcons.icons) { (iconName, vector) ->
                            val isSelected = iconName == selectedIconName
                            Surface(
                                modifier = Modifier
                                    .size(52.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable {
                                        selectedIconName = iconName
                                        showIconPicker = false
                                    }
                                    .then(
                                        if (isSelected) {
                                            Modifier.border(2.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(12.dp))
                                        } else Modifier
                                    ),
                                shape = RoundedCornerShape(12.dp),
                                color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainerHighest,
                                contentColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                            ) {
                                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = vector,
                                        contentDescription = iconName,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showIconPicker = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Modal "Add Action" dialog: All supported actions
    if (showAddActionDialog) {
        AlertDialog(
            onDismissRequest = { showAddActionDialog = false },
            title = { Text("Add Action Block", fontWeight = FontWeight.Bold) },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 460.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // Block 1: Open an app
                    ActionBlockRow(
                        title = "Open an app",
                        icon = Icons.Default.Apps,
                        onClick = {
                            showAddActionDialog = false
                            showAppPicker = true
                        }
                    )

                    // Block 2: Wi-Fi
                    ActionBlockRow(
                        title = "Wi-Fi",
                        icon = Icons.Default.Wifi,
                        onClick = {
                            showAddActionDialog = false
                            showSubActionDialog = "wifi"
                        }
                    )

                    // Block 3: Mobile data
                    ActionBlockRow(
                        title = "Mobile data",
                        icon = Icons.Default.SignalCellularAlt,
                        onClick = {
                            showAddActionDialog = false
                            showSubActionDialog = "mobile_data"
                        }
                    )

                    // Block 4: Flashlight
                    ActionBlockRow(
                        title = "Flashlight",
                        icon = Icons.Default.FlashlightOn,
                        onClick = {
                            showAddActionDialog = false
                            showSubActionDialog = "flashlight"
                        }
                    )

                    // Block 5: Low power mode
                    ActionBlockRow(
                        title = "Low power mode",
                        icon = Icons.Default.BatteryChargingFull,
                        onClick = {
                            showAddActionDialog = false
                            showSubActionDialog = "low_power"
                        }
                    )

                    // Block 6: Aeroplane mode
                    ActionBlockRow(
                        title = "Aeroplane mode",
                        icon = Icons.Default.AirplanemodeActive,
                        onClick = {
                            showAddActionDialog = false
                            showSubActionDialog = "airplane_mode"
                        }
                    )

                    // Block 7: Bluetooth
                    ActionBlockRow(
                        title = "Bluetooth",
                        icon = Icons.Default.Bluetooth,
                        onClick = {
                            showAddActionDialog = false
                            showSubActionDialog = "bluetooth"
                        }
                    )

                    // Block 8: Ringer mode
                    ActionBlockRow(
                        title = "Ringer mode",
                        icon = Icons.Default.NotificationsActive,
                        onClick = {
                            showAddActionDialog = false
                            showSubActionDialog = "ringer_mode"
                        }
                    )

                    // Block 9: Set brightness
                    ActionBlockRow(
                        title = "Set brightness",
                        icon = Icons.Default.BrightnessMedium,
                        onClick = {
                            actions = actions + TaskAction.SetBrightness(50)
                            showAddActionDialog = false
                        }
                    )

                    // Block 10: Set volume
                    ActionBlockRow(
                        title = "Set volume",
                        icon = Icons.Default.VolumeUp,
                        onClick = {
                            actions = actions + TaskAction.SetVolume(50)
                            showAddActionDialog = false
                        }
                    )

                    // Block 11: Take screenshot
                    ActionBlockRow(
                        title = "Take screenshot",
                        icon = Icons.Default.Screenshot,
                        onClick = {
                            actions = actions + TaskAction.TakeScreenshot
                            showAddActionDialog = false
                        }
                    )

                    // Block 12: Go to home screen
                    ActionBlockRow(
                        title = "Go to home screen",
                        icon = Icons.Default.Home,
                        onClick = {
                            actions = actions + TaskAction.GoHome
                            showAddActionDialog = false
                        }
                    )

                    // Block 13: Media controls
                    ActionBlockRow(
                        title = "Media controls",
                        icon = Icons.Default.PlayCircleOutline,
                        onClick = {
                            showAddActionDialog = false
                            showSubActionDialog = "media_controls"
                        }
                    )

                    // Block 14: Send notification
                    ActionBlockRow(
                        title = "Send notification",
                        icon = Icons.Default.NotificationAdd,
                        onClick = {
                            actions = actions + TaskAction.SendNotification(
                                title = "Notification",
                                text = "Shortcut executed"
                            )
                            showAddActionDialog = false
                        }
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { showAddActionDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Sub-action dialog for Toggle, Turn on, Turn off OR Ringer mode OR Media controls
    if (showSubActionDialog != null) {
        val blockKey = showSubActionDialog!!
        when (blockKey) {
            "ringer_mode" -> {
                AlertDialog(
                    onDismissRequest = { showSubActionDialog = null },
                    title = { Text("Ringer Mode Action", fontWeight = FontWeight.Bold) },
                    text = {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            ActionOptionButton("Set Ring Mode") {
                                actions = actions + TaskAction.RingerMode(RingerActionType.SET_RING)
                                showSubActionDialog = null
                            }
                            ActionOptionButton("Set Vibrate Mode") {
                                actions = actions + TaskAction.RingerMode(RingerActionType.SET_VIBRATE)
                                showSubActionDialog = null
                            }
                            ActionOptionButton("Toggle Ring / Vibrate") {
                                actions = actions + TaskAction.RingerMode(RingerActionType.TOGGLE)
                                showSubActionDialog = null
                            }
                        }
                    },
                    confirmButton = {
                        TextButton(onClick = { showSubActionDialog = null }) {
                            Text("Cancel")
                        }
                    }
                )
            }
            "media_controls" -> {
                AlertDialog(
                    onDismissRequest = { showSubActionDialog = null },
                    title = { Text("Media Action", fontWeight = FontWeight.Bold) },
                    text = {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            ActionOptionButton("Play") {
                                actions = actions + TaskAction.MediaControls(MediaActionType.PLAY)
                                showSubActionDialog = null
                            }
                            ActionOptionButton("Pause") {
                                actions = actions + TaskAction.MediaControls(MediaActionType.PAUSE)
                                showSubActionDialog = null
                            }
                            ActionOptionButton("Toggle Playback") {
                                actions = actions + TaskAction.MediaControls(MediaActionType.TOGGLE_PLAYBACK)
                                showSubActionDialog = null
                            }
                            ActionOptionButton("Skip Next") {
                                actions = actions + TaskAction.MediaControls(MediaActionType.SKIP)
                                showSubActionDialog = null
                            }
                        }
                    },
                    confirmButton = {
                        TextButton(onClick = { showSubActionDialog = null }) {
                            Text("Cancel")
                        }
                    }
                )
            }
            else -> {
                val title = when (blockKey) {
                    "wifi" -> "Wi-Fi Action"
                    "mobile_data" -> "Mobile Data Action"
                    "flashlight" -> "Flashlight Action"
                    "low_power" -> "Low Power Mode Action"
                    "airplane_mode" -> "Aeroplane Mode Action"
                    "bluetooth" -> "Bluetooth Action"
                    else -> "Action"
                }
                AlertDialog(
                    onDismissRequest = { showSubActionDialog = null },
                    title = { Text(title, fontWeight = FontWeight.Bold) },
                    text = {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            ActionOptionButton("Toggle") {
                                val action = createToggleAction(blockKey, ToggleActionType.TOGGLE)
                                if (action != null) actions = actions + action
                                showSubActionDialog = null
                            }
                            ActionOptionButton("Turn On") {
                                val action = createToggleAction(blockKey, ToggleActionType.TURN_ON)
                                if (action != null) actions = actions + action
                                showSubActionDialog = null
                            }
                            ActionOptionButton("Turn Off") {
                                val action = createToggleAction(blockKey, ToggleActionType.TURN_OFF)
                                if (action != null) actions = actions + action
                                showSubActionDialog = null
                            }
                        }
                    },
                    confirmButton = {
                        TextButton(onClick = { showSubActionDialog = null }) {
                            Text("Cancel")
                        }
                    }
                )
            }
        }
    }

    // App Picker Dialog for Open App action
    if (showAppPicker) {
        AppPickerDialog(
            onDismiss = { showAppPicker = false },
            onSelectApp = { barApp ->
                actions = actions + TaskAction.OpenApp(
                    packageName = barApp.packageName,
                    appLabel = barApp.label
                )
                showAppPicker = false
            }
        )
    }
}

private fun createToggleAction(key: String, op: ToggleActionType): TaskAction? {
    return when (key) {
        "wifi" -> TaskAction.Wifi(op)
        "mobile_data" -> TaskAction.MobileData(op)
        "flashlight" -> TaskAction.Flashlight(op)
        "low_power" -> TaskAction.LowPower(op)
        "airplane_mode" -> TaskAction.AirplaneMode(op)
        "bluetooth" -> TaskAction.Bluetooth(op)
        else -> null
    }
}

@Composable
private fun ActionBlockRow(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surfaceContainerHighest,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(22.dp)
            )
            Text(
                text = title,
                fontWeight = FontWeight.Medium,
                fontSize = 15.sp,
                modifier = Modifier.weight(1f)
            )
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

@Composable
private fun ActionOptionButton(
    label: String,
    onClick: () -> Unit
) {
    FilledTonalButton(
        onClick = onClick,
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
    ) {
        Text(text = label, fontWeight = FontWeight.SemiBold)
    }
}
