package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.models.TaskItem
import com.example.models.TaskPriority
import com.example.ui.JarvisViewModel
import com.example.ui.theme.*
import com.example.utils.glassmorphic

@Composable
fun DashboardScreen(viewModel: JarvisViewModel) {
    val tasks by viewModel.tasks.collectAsState()
    val logs by viewModel.analyticsLogs.collectAsState()
    val metrics by viewModel.systemMetrics.collectAsState()

    var showAddTaskDialog by remember { mutableStateOf(false) }
    var taskTitleInput by remember { mutableStateOf("") }
    var taskPriorityInput by remember { mutableStateOf(TaskPriority.MEDIUM) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DeepSpaceBackground)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(bottom = 80.dp)
                .padding(16.dp)
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .glassmorphic(cornerRadius = 16.dp)
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "ANALYTICS & TASKS DASHBOARD",
                        color = NeonCyan,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = 2.sp
                    )
                    Text(
                        text = "AI USAGE & SYSTEM HEALTH TIMELINE",
                        color = TextSecondary,
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }

                IconButton(
                    onClick = { showAddTaskDialog = true },
                    modifier = Modifier.testTag("btn_add_task")
                ) {
                    Icon(imageVector = Icons.Default.AddTask, contentDescription = "Add Task", tint = GlowingGreen)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Analytics KPI Cards
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                TelemetryCard(
                    title = "TASKS DONE",
                    value = "${tasks.count { it.isCompleted }}/${tasks.size}",
                    subtext = "COMPLETED",
                    icon = Icons.Default.CheckCircle,
                    accentColor = GlowingGreen,
                    modifier = Modifier.weight(1f)
                )
                TelemetryCard(
                    title = "AI LOGS",
                    value = "${logs.size}",
                    subtext = "EVENTS",
                    icon = Icons.Default.Analytics,
                    accentColor = ElectricBlue,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Task List
            Text(
                text = "SYSTEM TASKS",
                color = TextSecondary,
                fontSize = 12.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(tasks, key = { it.id }) { task ->
                    TaskCard(
                        task = task,
                        onToggle = { viewModel.toggleTask(task) },
                        onDelete = { viewModel.deleteTask(task.id) }
                    )
                }
            }
        }

        // Add Task Dialog
        if (showAddTaskDialog) {
            AlertDialog(
                onDismissRequest = { showAddTaskDialog = false },
                containerColor = DarkGlassSurface,
                title = {
                    Text(
                        text = "NEW SYSTEM TASK",
                        color = NeonCyan,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedTextField(
                            value = taskTitleInput,
                            onValueChange = { taskTitleInput = it },
                            label = { Text("Task Description", color = TextMuted) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = NeonCyan,
                                unfocusedBorderColor = GlassBorder,
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("tf_task_title")
                        )

                        Text("Priority:", color = TextSecondary, fontSize = 12.sp, fontFamily = FontFamily.Monospace)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            TaskPriority.entries.forEach { priority ->
                                FilterChip(
                                    selected = taskPriorityInput == priority,
                                    onClick = { taskPriorityInput = priority },
                                    label = { Text(priority.name, fontSize = 10.sp) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = NeonCyan,
                                        selectedLabelColor = DeepSpaceBackground
                                    )
                                )
                            }
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (taskTitleInput.isNotBlank()) {
                                viewModel.addTask(taskTitleInput, taskPriorityInput)
                                taskTitleInput = ""
                                showAddTaskDialog = false
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = NeonCyan, contentColor = DeepSpaceBackground),
                        modifier = Modifier.testTag("btn_save_task")
                    ) {
                        Text("SAVE TASK", fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showAddTaskDialog = false }) {
                        Text("CANCEL", color = TextMuted, fontFamily = FontFamily.Monospace)
                    }
                }
            )
        }
    }
}

@Composable
fun TaskCard(
    task: TaskItem,
    onToggle: () -> Unit,
    onDelete: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .glassmorphic(cornerRadius = 12.dp)
            .padding(12.dp)
            .testTag("task_${task.id}")
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                Checkbox(
                    checked = task.isCompleted,
                    onCheckedChange = { onToggle() },
                    colors = CheckboxDefaults.colors(checkedColor = GlowingGreen)
                )

                Spacer(modifier = Modifier.width(8.dp))

                Column {
                    Text(
                        text = task.title,
                        color = if (task.isCompleted) TextMuted else TextPrimary,
                        fontSize = 14.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Medium
                    )

                    Text(
                        text = "PRIORITY: ${task.priority.name}",
                        color = when (task.priority) {
                            TaskPriority.CRITICAL -> DangerRed
                            TaskPriority.HIGH -> AlertOrange
                            else -> ElectricBlue
                        },
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            IconButton(onClick = onDelete) {
                Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete Task", tint = TextMuted)
            }
        }
    }
}
