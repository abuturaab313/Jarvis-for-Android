package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.models.SystemMetrics
import com.example.ui.JarvisViewModel
import com.example.ui.NavigationScreen
import com.example.ui.theme.*
import com.example.utils.AnimatedAiCore
import com.example.utils.ParticleBackground
import com.example.utils.glassmorphic
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun HomeScreen(
    viewModel: JarvisViewModel,
    onNavigate: (NavigationScreen) -> Unit
) {
    val metrics by viewModel.systemMetrics.collectAsState()
    val isSpeaking by viewModel.voiceEngine.isSpeaking.collectAsState()
    val isListening by viewModel.voiceEngine.isListening.collectAsState()
    val audioLevel by viewModel.voiceEngine.audioWaveLevel.collectAsState()

    val currentTime = remember { SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date()) }
    val currentDate = remember { SimpleDateFormat("EEEE, MMMM d", Locale.getDefault()).format(Date()) }

    val greetingText = remember {
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        when (hour) {
            in 5..11 -> "Good Morning, Boss"
            in 12..17 -> "Good Afternoon, Boss"
            else -> "Good Evening, Boss"
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DeepSpaceBackground)
    ) {
        ParticleBackground(modifier = Modifier.fillMaxSize())

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(bottom = 80.dp)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // HUD Top Bar: Date, Time & Security Status
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .glassmorphic(cornerRadius = 16.dp)
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = currentTime,
                        color = NeonCyan,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        text = currentDate,
                        color = TextSecondary,
                        fontSize = 11.sp
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Shield,
                        contentDescription = "Security Active",
                        tint = GlowingGreen,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "SECURE",
                        color = GlowingGreen,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Greeting & Assistant Prompt
            Text(
                text = greetingText,
                color = TextPrimary,
                fontSize = 20.sp,
                fontWeight = FontWeight.Medium,
                fontFamily = FontFamily.Monospace
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "How may I assist you today?",
                color = NeonCyan,
                fontSize = 14.sp,
                fontFamily = FontFamily.Monospace
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Center Animated AI Core ARC Reactor
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(220.dp)
                    .clickable {
                        onNavigate(NavigationScreen.VOICE)
                    }
                    .testTag("home_ai_core")
            ) {
                AnimatedAiCore(
                    modifier = Modifier.fillMaxSize(),
                    isListening = isListening,
                    isSpeaking = isSpeaking,
                    audioLevel = audioLevel
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Quick HUD Action Pills
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                item {
                    QuickActionButton(
                        title = "Voice AI",
                        icon = Icons.Default.Mic,
                        onClick = { onNavigate(NavigationScreen.VOICE) },
                        testTag = "btn_voice_ai"
                    )
                }
                item {
                    QuickActionButton(
                        title = "AI Chat",
                        icon = Icons.AutoMirrored.Filled.Chat,
                        onClick = { onNavigate(NavigationScreen.CHAT) },
                        testTag = "btn_ai_chat"
                    )
                }
                item {
                    QuickActionButton(
                        title = "Vision HUD",
                        icon = Icons.Default.CameraAlt,
                        onClick = { onNavigate(NavigationScreen.VISION) },
                        testTag = "btn_vision_hud"
                    )
                }
                item {
                    QuickActionButton(
                        title = "Mobile Skills",
                        icon = Icons.Default.Extension,
                        onClick = { onNavigate(NavigationScreen.SKILLS) },
                        testTag = "btn_skills"
                    )
                }
                item {
                    QuickActionButton(
                        title = "Routines",
                        icon = Icons.Default.AutoAwesome,
                        onClick = { onNavigate(NavigationScreen.ROUTINES) },
                        testTag = "btn_routines"
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // System Telemetry Section
            Text(
                text = "SYSTEM TELEMETRY",
                color = TextSecondary,
                fontSize = 12.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.sp,
                modifier = Modifier.align(Alignment.Start)
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Telemetry Grid
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    TelemetryCard(
                        title = "BATTERY",
                        value = "${metrics.batteryLevel}%",
                        subtext = if (metrics.isCharging) "CHARGING" else "DISCHARGING",
                        icon = Icons.Default.BatteryChargingFull,
                        accentColor = if (metrics.batteryLevel > 20) GlowingGreen else DangerRed,
                        modifier = Modifier.weight(1f)
                    )
                    TelemetryCard(
                        title = "NETWORK",
                        value = if (metrics.isWifiConnected) "WI-FI 5G" else "CELLULAR",
                        subtext = metrics.networkType,
                        icon = Icons.Default.Wifi,
                        accentColor = ElectricBlue,
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    TelemetryCard(
                        title = "RAM USAGE",
                        value = "${metrics.ramUsagePct}%",
                        subtext = "OPTIMIZED",
                        icon = Icons.Default.Memory,
                        accentColor = QuantumPurple,
                        modifier = Modifier.weight(1f)
                    )
                    TelemetryCard(
                        title = "CPU LOAD",
                        value = "${metrics.cpuUsagePct}%",
                        subtext = "${metrics.uptimeHours}h UPTIME",
                        icon = Icons.Default.Speed,
                        accentColor = NeonCyan,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
fun QuickActionButton(
    title: String,
    icon: ImageVector,
    onClick: () -> Unit,
    testTag: String
) {
    Box(
        modifier = Modifier
            .glassmorphic(cornerRadius = 20.dp)
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 10.dp)
            .testTag(testTag)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = NeonCyan,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = title,
                color = TextPrimary,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                fontFamily = FontFamily.Monospace
            )
        }
    }
}

@Composable
fun TelemetryCard(
    title: String,
    value: String,
    subtext: String,
    icon: ImageVector,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .glassmorphic(cornerRadius = 16.dp, glowColor = accentColor.copy(alpha = 0.2f))
            .padding(14.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    color = TextMuted,
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 1.sp
                )
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = accentColor,
                    modifier = Modifier.size(16.dp)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = value,
                color = TextPrimary,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )

            Text(
                text = subtext,
                color = accentColor,
                fontSize = 10.sp,
                fontFamily = FontFamily.Monospace
            )
        }
    }
}
