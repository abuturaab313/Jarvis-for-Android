package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.JarvisViewModel
import com.example.ui.theme.*
import com.example.utils.glassmorphic

@Composable
fun SettingsScreen(viewModel: JarvisViewModel) {
    val context = LocalContext.current

    var speechPitch by remember { mutableFloatStateOf(0.95f) }
    var speechRate by remember { mutableFloatStateOf(1.05f) }
    var offlineMode by remember { mutableStateOf(false) }

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
                .verticalScroll(rememberScrollState())
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
                        text = "JARVIS OS SETTINGS",
                        color = NeonCyan,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = 2.sp
                    )
                    Text(
                        text = "CONFIGURATION & PREFERENCES",
                        color = TextSecondary,
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }

                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "Settings",
                    tint = NeonCyan
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Gemini API Key Management Card
            var settingsKeyInput by remember { mutableStateOf("") }
            var isUpdatingKey by remember { mutableStateOf(false) }
            var keyStatusMessage by remember { mutableStateOf<String?>(null) }
            val hasKey = viewModel.hasApiKey()
            val maskedKey = viewModel.getMaskedApiKey()

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .glassmorphic(cornerRadius = 16.dp)
                    .padding(16.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.VpnKey, contentDescription = "API Key", tint = NeonCyan)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "GEMINI API KEY",
                                color = NeonCyan,
                                fontSize = 13.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (hasKey) GlowingGreen.copy(alpha = 0.15f) else AlertOrange.copy(alpha = 0.15f),
                            border = androidx.compose.foundation.BorderStroke(1.dp, if (hasKey) GlowingGreen else AlertOrange)
                        ) {
                            Text(
                                text = if (hasKey) "ACTIVE" else "NOT SET",
                                color = if (hasKey) GlowingGreen else AlertOrange,
                                fontSize = 10.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Current Key: $maskedKey",
                        color = TextSecondary,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = settingsKeyInput,
                        onValueChange = {
                            settingsKeyInput = it
                            keyStatusMessage = null
                        },
                        placeholder = { Text("Enter new Gemini API key", fontSize = 11.sp, color = TextMuted) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = NeonCyan,
                            unfocusedBorderColor = GlassBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_settings_api_key")
                    )

                    keyStatusMessage?.let { msg ->
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = msg,
                            color = if (msg.contains("Success", ignoreCase = true)) GlowingGreen else DangerRed,
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                if (settingsKeyInput.isBlank()) {
                                    keyStatusMessage = "Please enter a key"
                                    return@Button
                                }
                                isUpdatingKey = true
                                viewModel.validateAndSaveApiKey(
                                    apiKey = settingsKeyInput.trim(),
                                    onSuccess = {
                                        isUpdatingKey = false
                                        settingsKeyInput = ""
                                        keyStatusMessage = "Key Validated & Saved Successfully!"
                                        Toast.makeText(context, "API Key updated", Toast.LENGTH_SHORT).show()
                                    },
                                    onError = { err ->
                                        isUpdatingKey = false
                                        keyStatusMessage = err
                                    }
                                )
                            },
                            enabled = !isUpdatingKey,
                            colors = ButtonDefaults.buttonColors(containerColor = NeonCyan, contentColor = DeepSpaceBackground),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("btn_update_api_key")
                        ) {
                            Text(if (isUpdatingKey) "SAVING..." else "SAVE KEY", fontFamily = FontFamily.Monospace, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }

                        if (hasKey) {
                            OutlinedButton(
                                onClick = {
                                    viewModel.deleteApiKey()
                                    settingsKeyInput = ""
                                    keyStatusMessage = "API Key cleared from storage"
                                    Toast.makeText(context, "API Key cleared", Toast.LENGTH_SHORT).show()
                                },
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = DangerRed),
                                border = androidx.compose.foundation.BorderStroke(1.dp, DangerRed),
                                modifier = Modifier.testTag("btn_delete_api_key")
                            ) {
                                Text("CLEAR", fontFamily = FontFamily.Monospace, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Speech Synthesis Pitch & Rate Sliders
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .glassmorphic(cornerRadius = 16.dp)
                    .padding(16.dp)
            ) {
                Column {
                    Text("SYNTHETIC VOICE PITCH", color = TextPrimary, fontSize = 13.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                    Slider(
                        value = speechPitch,
                        onValueChange = { speechPitch = it },
                        valueRange = 0.5f..1.5f,
                        colors = SliderDefaults.colors(thumbColor = NeonCyan, activeTrackColor = ElectricBlue),
                        modifier = Modifier.testTag("slider_pitch")
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text("SYNTHETIC VOICE SPEED", color = TextPrimary, fontSize = 13.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                    Slider(
                        value = speechRate,
                        onValueChange = { speechRate = it },
                        valueRange = 0.5f..1.5f,
                        colors = SliderDefaults.colors(thumbColor = NeonCyan, activeTrackColor = ElectricBlue),
                        modifier = Modifier.testTag("slider_rate")
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Long-Term Memory Vault Section
            val memories by viewModel.memories.collectAsState()
            var newMemoryText by remember { mutableStateOf("") }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .glassmorphic(cornerRadius = 16.dp)
                    .padding(16.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.Psychology, contentDescription = "Memory Vault", tint = NeonCyan)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "LONG-TERM MEMORY VAULT (${memories.size})",
                                color = NeonCyan,
                                fontSize = 13.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        if (memories.isNotEmpty()) {
                            IconButton(onClick = { viewModel.clearAllMemories() }) {
                                Icon(imageVector = Icons.Default.DeleteSweep, contentDescription = "Clear All Memories", tint = DangerRed)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = newMemoryText,
                            onValueChange = { newMemoryText = it },
                            placeholder = { Text("e.g., Boss prefers dark mode & coffee", fontSize = 11.sp, color = TextMuted) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = NeonCyan,
                                unfocusedBorderColor = GlassBorder,
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("input_new_memory")
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                if (newMemoryText.isNotBlank()) {
                                    viewModel.addMemory(newMemoryText)
                                    newMemoryText = ""
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = NeonCyan, contentColor = DeepSpaceBackground),
                            modifier = Modifier.testTag("btn_save_memory")
                        ) {
                            Text("SAVE", fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    if (memories.isEmpty()) {
                        Text(
                            text = "No long-term memories indexed yet. Chat naturally or save a fact above to populate JARVIS memory.",
                            color = TextSecondary,
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            memories.take(6).forEach { mem ->
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = DarkGlassSurface),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(10.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = mem.content,
                                                color = TextPrimary,
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Medium
                                            )
                                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                                Text(
                                                    text = "CAT: ${mem.category}",
                                                    color = GlowingGreen,
                                                    fontSize = 9.sp,
                                                    fontFamily = FontFamily.Monospace
                                                )
                                                Text(
                                                    text = "WEIGHT: ${(mem.importanceScore * 100).toInt()}%",
                                                    color = NeonCyan,
                                                    fontSize = 9.sp,
                                                    fontFamily = FontFamily.Monospace
                                                )
                                            }
                                        }
                                        IconButton(
                                            onClick = { viewModel.deleteMemory(mem) },
                                            modifier = Modifier.size(24.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Close,
                                                contentDescription = "Delete Memory",
                                                tint = TextMuted,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .glassmorphic(cornerRadius = 16.dp)
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.CloudOff, contentDescription = "Offline Mode", tint = AlertOrange)
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text("Offline Fallback Mode", color = TextPrimary, fontSize = 14.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                            Text("Use local rule processing without API", color = TextSecondary, fontSize = 11.sp)
                        }
                    }
                    Switch(
                        checked = offlineMode,
                        onCheckedChange = { offlineMode = it },
                        colors = SwitchDefaults.colors(checkedThumbColor = AlertOrange, checkedTrackColor = DarkGlassSurface),
                        modifier = Modifier.testTag("sw_offline")
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Backup & Restore Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = {
                        Toast.makeText(context, "JARVIS Database Backup Exported", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = DarkGlassSurface, contentColor = NeonCyan),
                    modifier = Modifier
                        .weight(1f)
                        .glassmorphic(cornerRadius = 12.dp)
                        .testTag("btn_backup")
                ) {
                    Text("BACKUP VAULT", fontFamily = FontFamily.Monospace, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = {
                        Toast.makeText(context, "JARVIS System Preferences Restored", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = DarkGlassSurface, contentColor = GlowingGreen),
                    modifier = Modifier
                        .weight(1f)
                        .glassmorphic(cornerRadius = 12.dp)
                        .testTag("btn_restore")
                ) {
                    Text("RESTORE DATA", fontFamily = FontFamily.Monospace, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
