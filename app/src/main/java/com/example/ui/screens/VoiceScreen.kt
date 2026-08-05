package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.JarvisViewModel
import com.example.ui.theme.*
import com.example.utils.AnimatedAiCore
import com.example.utils.ParticleBackground
import com.example.utils.VoiceWaveEqualizer
import com.example.utils.glassmorphic

@Composable
fun VoiceScreen(viewModel: JarvisViewModel) {
    val isSpeaking by viewModel.voiceEngine.isSpeaking.collectAsState()
    val isListening by viewModel.voiceEngine.isListening.collectAsState()
    val speechText by viewModel.voiceEngine.speechText.collectAsState()
    val audioLevel by viewModel.voiceEngine.audioWaveLevel.collectAsState()
    val wakeWordActive by viewModel.voiceEngine.continuousWakeWord.collectAsState()

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
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .glassmorphic(cornerRadius = 16.dp)
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "VOICE ENGINE HUD",
                    color = NeonCyan,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 2.sp
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "WAKE WORD",
                        color = if (wakeWordActive) GlowingGreen else TextMuted,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Switch(
                        checked = wakeWordActive,
                        onCheckedChange = { viewModel.voiceEngine.toggleWakeWord(it) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = GlowingGreen,
                            checkedTrackColor = DarkGlassSurface
                        ),
                        modifier = Modifier.testTag("sw_wake_word")
                    )
                }
            }

            // Center Equalizer AI Core
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(260.dp)
                        .clickable {
                            if (isListening) viewModel.voiceEngine.stopListening()
                            else viewModel.voiceEngine.startListening()
                        }
                        .testTag("voice_ai_core")
                ) {
                    AnimatedAiCore(
                        modifier = Modifier.fillMaxSize(),
                        isListening = isListening,
                        isSpeaking = isSpeaking,
                        audioLevel = audioLevel
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))

                // Equalizer Bar Animation
                VoiceWaveEqualizer(
                    modifier = Modifier
                        .fillMaxWidth(0.7f)
                        .height(50.dp),
                    isAnimating = isListening || isSpeaking,
                    color = if (isListening) DangerRed else NeonCyan
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Voice Transcript Box
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .glassmorphic(cornerRadius = 16.dp)
                        .padding(16.dp)
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = when {
                                isSpeaking -> "JARVIS IS SPEAKING..."
                                isListening -> "LISTENING TO PROMPT..."
                                else -> "STANDBY FOR WAKE COMMAND (\"JARVIS\")"
                            },
                            color = if (isListening) DangerRed else NeonCyan,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            letterSpacing = 1.sp
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = speechText.ifEmpty { "Tap AI Core or speak to issue voice command..." },
                            color = TextPrimary,
                            fontSize = 15.sp,
                            fontFamily = FontFamily.Monospace,
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                    }
                }
            }

            // Bottom Controls (Interrupt Speaking / Start Listening)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Interrupt Speaking Button
                Button(
                    onClick = { viewModel.voiceEngine.stopSpeaking() },
                    colors = ButtonDefaults.buttonColors(containerColor = DangerRed.copy(alpha = 0.2f)),
                    modifier = Modifier
                        .glassmorphic(cornerRadius = 24.dp, borderColor = DangerRed)
                        .testTag("btn_interrupt_voice")
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.VolumeOff,
                            contentDescription = "Interrupt",
                            tint = DangerRed,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "INTERRUPT",
                            color = DangerRed,
                            fontSize = 12.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Mic Action Button
                FloatingActionButton(
                    onClick = {
                        if (isListening) viewModel.voiceEngine.stopListening()
                        else viewModel.voiceEngine.startListening()
                    },
                    containerColor = if (isListening) DangerRed else NeonCyan,
                    contentColor = DeepSpaceBackground,
                    modifier = Modifier.testTag("fab_mic_toggle")
                ) {
                    Icon(
                        imageVector = if (isListening) Icons.Default.MicOff else Icons.Default.Mic,
                        contentDescription = "Toggle Mic"
                    )
                }
            }
        }
    }
}
