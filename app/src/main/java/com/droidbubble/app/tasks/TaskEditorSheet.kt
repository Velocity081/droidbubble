package com.droidbubble.app.tasks

import androidx.activity.compose.BackHandler

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.droidbubble.app.island.M3CookieShape
import com.droidbubble.app.ui.main.AppPickerDialog

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
        modifier = modifier.fillMaxSize(),
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
                                .clip(M3CookieShape)
                                .clickable { showIconPicker = true },
                            shape = M3CookieShape,
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

            // 4. Chained Action Blocks List
            if (actions.isEmpty()) {
                item {
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = MaterialTheme.colorScheme.surfaceContainerLow,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
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
                itemsIndexed(actions) { index, action ->
                    Surface(
                        shape = RoundedCornerShape(18.dp),
                        color = MaterialTheme.colorScheme.surfaceContainerHigh,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        ListItem(
                            colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                            headlineContent = {
                                Text(
                                    text = action.getDisplayName(),
                                    fontWeight = FontWeight.SemiBold
                                )
                            },
                            leadingContent = {
                                Surface(
                                    shape = CircleShape,
                                    color = MaterialTheme.colorScheme.primaryContainer,
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                        Text(
                                            text = "${index + 1}",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }
                            },
                            trailingContent = {
                                IconButton(
                                    onClick = {
                                        actions = actions.filterIndexed { i, _ -> i != index }
                                    }
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = "Remove",
                                        tint = MaterialTheme.colorScheme.error
                                    )
                                }
                            }
                        )
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
                                    .clip(M3CookieShape)
                                    .clickable {
                                        selectedIconName = iconName
                                        showIconPicker = false
                                    }
                                    .then(
                                        if (isSelected) {
                                            Modifier.border(2.dp, MaterialTheme.colorScheme.primary, M3CookieShape)
                                        } else Modifier
                                    ),
                                shape = M3CookieShape,
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

    // Modal "Add Action" dialog: STRICTLY only allowed blocks!
    if (showAddActionDialog) {
        AlertDialog(
            onDismissRequest = { showAddActionDialog = false },
            title = { Text("Add Action Block", fontWeight = FontWeight.Bold) },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
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
                }
            },
            confirmButton = {
                TextButton(onClick = { showAddActionDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Sub-action dialog for Toggle, Turn on, Turn off OR Ringer mode options
    if (showSubActionDialog != null) {
        val blockKey = showSubActionDialog!!
        if (blockKey == "ringer_mode") {
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
        } else {
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
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceContainerHighest,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
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

