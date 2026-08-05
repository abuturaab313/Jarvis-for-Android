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
import com.example.models.Routine
import com.example.models.TriggerType
import com.example.ui.JarvisViewModel
import com.example.ui.theme.*
import com.example.utils.glassmorphic

@Composable
fun AutomationScreen(viewModel: JarvisViewModel) {
    val routines by viewModel.routines.collectAsState()

    var showAddDialog by remember { mutableStateOf(false) }
    var titleInput by remember { mutableStateOf("") }
    var conditionInput by remember { mutableStateOf("") }
    var actionInput by remember { mutableStateOf("") }
    var selectedTrigger by remember { mutableStateOf(TriggerType.BATTERY) }

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
                        text = "AUTOMATION ROUTINES",
                        color = NeonCyan,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = 2.sp
                    )
                    Text(
                        text = "TRIGGER-ACTION AI EXECUTOR",
                        color = TextSecondary,
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }

                IconButton(
                    onClick = { showAddDialog = true },
                    modifier = Modifier.testTag("btn_add_routine")
                ) {
                    Icon(imageVector = Icons.Default.AddCircle, contentDescription = "Add Routine", tint = GlowingGreen)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Routines List
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(routines, key = { it.id }) { routine ->
                    RoutineCard(
                        routine = routine,
                        onToggle = { viewModel.toggleRoutine(routine) },
                        onDelete = { viewModel.deleteRoutine(routine) }
                    )
                }
            }
        }

        // Add Routine Dialog
        if (showAddDialog) {
            AlertDialog(
                onDismissRequest = { showAddDialog = false },
                containerColor = DarkGlassSurface,
                title = {
                    Text(
                        text = "CREATE AUTOMATION ROUTINE",
                        color = NeonCyan,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedTextField(
                            value = titleInput,
                            onValueChange = { titleInput = it },
                            label = { Text("Routine Title", color = TextMuted) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = NeonCyan,
                                unfocusedBorderColor = GlassBorder,
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("tf_routine_title")
                        )

                        Text("Trigger Type:", color = TextSecondary, fontSize = 12.sp, fontFamily = FontFamily.Monospace)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            TriggerType.entries.take(3).forEach { trigger ->
                                FilterChip(
                                    selected = selectedTrigger == trigger,
                                    onClick = { selectedTrigger = trigger },
                                    label = { Text(trigger.name, fontSize = 10.sp) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = NeonCyan,
                                        selectedLabelColor = DeepSpaceBackground
                                    )
                                )
                            }
                        }

                        OutlinedTextField(
                            value = conditionInput,
                            onValueChange = { conditionInput = it },
                            label = { Text("Trigger Condition (e.g. Battery < 20%, 08:00 AM)", color = TextMuted) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = NeonCyan,
                                unfocusedBorderColor = GlassBorder,
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("tf_routine_cond")
                        )

                        OutlinedTextField(
                            value = actionInput,
                            onValueChange = { actionInput = it },
                            label = { Text("Automated Action (e.g. Enable Flashlight, Dim Screen)", color = TextMuted) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = NeonCyan,
                                unfocusedBorderColor = GlassBorder,
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("tf_routine_act")
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (titleInput.isNotBlank() && conditionInput.isNotBlank()) {
                                viewModel.addRoutine(titleInput, selectedTrigger, conditionInput, actionInput)
                                titleInput = ""
                                conditionInput = ""
                                actionInput = ""
                                showAddDialog = false
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = NeonCyan, contentColor = DeepSpaceBackground),
                        modifier = Modifier.testTag("btn_routine_save")
                    ) {
                        Text("SAVE ROUTINE", fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showAddDialog = false }) {
                        Text("CANCEL", color = TextMuted, fontFamily = FontFamily.Monospace)
                    }
                }
            )
        }
    }
}

@Composable
fun RoutineCard(
    routine: Routine,
    onToggle: () -> Unit,
    onDelete: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .glassmorphic(
                cornerRadius = 16.dp,
                borderColor = if (routine.isEnabled) NeonCyan else GlassBorder
            )
            .padding(16.dp)
            .testTag("routine_${routine.id}")
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = "Routine",
                        tint = if (routine.isEnabled) GlowingGreen else TextMuted,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = routine.title,
                        color = TextPrimary,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Switch(
                        checked = routine.isEnabled,
                        onCheckedChange = { onToggle() },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = GlowingGreen,
                            checkedTrackColor = DarkGlassSurface
                        )
                    )

                    IconButton(onClick = onDelete) {
                        Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete Routine", tint = TextMuted)
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "WHEN: ${routine.triggerCondition}",
                color = ElectricBlue,
                fontSize = 12.sp,
                fontFamily = FontFamily.Monospace
            )

            Text(
                text = "THEN: ${routine.actionCommand}",
                color = TextSecondary,
                fontSize = 12.sp,
                fontFamily = FontFamily.Monospace
            )
        }
    }
}
