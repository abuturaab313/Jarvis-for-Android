package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
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

            // Offline Mode Toggle
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
