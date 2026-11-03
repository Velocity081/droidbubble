package com.droidbubble.app.tasks

import android.view.HapticFeedbackConstants
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.droidbubble.app.droidbar.rememberBounceScale
import com.droidbubble.app.island.M3Cookie6Shape
import com.droidbubble.app.island.M3CookieShape

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TasksPage(
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val view = LocalView.current
    val repository = remember { TasksRepository.getInstance(context) }
    val shortcuts by repository.shortcuts.collectAsState()

    var editingShortcut by remember { mutableStateOf<TaskShortcut?>(null) }
    var isCreatingNew by remember { mutableStateOf(false) }
    var shortcutToDelete by remember { mutableStateOf<TaskShortcut?>(null) }

    BackHandler(onBack = {
        if (isCreatingNew || editingShortcut != null) {
            isCreatingNew = false
            editingShortcut = null
        } else {
            onBack()
        }
    })

    if (isCreatingNew || editingShortcut != null) {
        TaskEditorScreen(
            initialShortcut = editingShortcut,
            onSave = { saved ->
                repository.saveShortcut(saved)
                editingShortcut = null
                isCreatingNew = false
            },
            onBack = {
                editingShortcut = null
                isCreatingNew = false
            }
        )
        return
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text("Tasks", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                    isCreatingNew = true
                },
                shape = M3CookieShape,
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "New Shortcut",
                    modifier = Modifier.size(26.dp)
                )
            }
        }
    ) { innerPadding ->
        if (shortcuts.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.padding(32.dp)
                ) {
                    Surface(
                        modifier = Modifier.size(68.dp),
                        shape = M3CookieShape,
                        color = MaterialTheme.colorScheme.primaryContainer,
                        contentColor = MaterialTheme.colorScheme.primary
                    ) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Bolt,
                                contentDescription = null,
                                modifier = Modifier.size(36.dp)
                            )
                        }
                    }
                    Text(
                        text = "No shortcuts yet",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 88.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(shortcuts, key = { it.id }) { shortcut ->
                    TaskShortcutCard(
                        shortcut = shortcut,
                        onRun = {
                            view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                            runCatching {
                                TaskExecutor.execute(context, shortcut)
                            }.onFailure { e ->
                                android.util.Log.e("TasksPage", "Task error", e)
                            }
                        },
                        onEdit = { editingShortcut = shortcut },
                        onDelete = { shortcutToDelete = shortcut }
                    )
                }
            }
        }
    }

    if (shortcutToDelete != null) {
        val s = shortcutToDelete!!
        AlertDialog(
            onDismissRequest = { shortcutToDelete = null },
            title = { Text("Delete Shortcut") },
            text = { Text("Are you sure you want to delete \"${s.name}\"?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        repository.deleteShortcut(s.id)
                        shortcutToDelete = null
                    }
                ) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { shortcutToDelete = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun TaskShortcutCard(
    shortcut: TaskShortcut,
    onRun: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val (bounceScale, triggerBounce) = rememberBounceScale()
    var showMenu by remember { mutableStateOf(false) }

    Surface(
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer {
                scaleX = bounceScale
                scaleY = bounceScale
            }
            .clip(RoundedCornerShape(20.dp))
            .clickable {
                triggerBounce()
                onRun()
            }
    ) {
        ListItem(
            colors = ListItemDefaults.colors(containerColor = Color.Transparent),
            headlineContent = {
                Text(
                    text = shortcut.name,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 16.sp
                )
            },
            leadingContent = {
                Surface(
                    modifier = Modifier.size(42.dp),
                    shape = M3CookieShape,
                    color = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.primary
                ) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = TaskIcons.getIcon(shortcut.iconName),
                            contentDescription = null,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
            },
            trailingContent = {
                Box {
                    IconButton(onClick = { showMenu = true }) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "Options",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Run") },
                            leadingIcon = { Icon(Icons.Default.PlayArrow, contentDescription = null) },
                            onClick = {
                                showMenu = false
                                triggerBounce()
                                onRun()
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Edit") },
                            leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null) },
                            onClick = {
                                showMenu = false
                                onEdit()
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Delete", color = MaterialTheme.colorScheme.error) },
                            leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error) },
                            onClick = {
                                showMenu = false
                                onDelete()
                            }
                        )
                    }
                }
            }
        )
    }
}

