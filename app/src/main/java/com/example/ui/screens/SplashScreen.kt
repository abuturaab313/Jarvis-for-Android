package com.example.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import com.example.utils.AnimatedAiCore
import com.example.utils.ParticleBackground
import com.example.utils.glassmorphic
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    onBootComplete: () -> Unit
) {
    var bootStep by remember { mutableIntStateOf(0) }
    val bootLogs = listOf(
        "INITIALIZING JARVIS OS CORE v4.2...",
        "SCANNING HARDWARE MODULES...",
        "CONNECTING NEURAL MATRIX (GEMINI 3.5)...",
        "LOADING MOBILE CONTROL SUBSYSTEMS...",
        "ENCRYPTING LOCAL VAULT...",
        "JARVIS SYSTEM ONLINE"
    )

    LaunchedEffect(Unit) {
        for (i in bootLogs.indices) {
            bootStep = i
            delay(500)
        }
        delay(800)
        onBootComplete()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(DeepSpaceBackground, DarkGlassSurface, DeepSpaceBackground)
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        ParticleBackground(modifier = Modifier.fillMaxSize())

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(24.dp)
        ) {
            AnimatedAiCore(
                modifier = Modifier.size(200.dp)
            )

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = "J A R V I S",
                color = NeonCyan,
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                letterSpacing = 6.sp
            )

            Text(
                text = "AI OPERATING SYSTEM",
                color = TextSecondary,
                fontSize = 12.sp,
                fontFamily = FontFamily.Monospace,
                letterSpacing = 2.sp,
                modifier = Modifier.padding(top = 4.dp)
            )

            Spacer(modifier = Modifier.height(48.dp))

            // Boot Status Holographic Box
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.85f)
                    .glassmorphic(cornerRadius = 12.dp)
                    .padding(16.dp)
            ) {
                Column {
                    Text(
                        text = bootLogs.getOrElse(bootStep) { "INITIALIZING..." },
                        color = GlowingGreen,
                        fontSize = 12.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Medium
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Progress Bar
                    val progress = (bootStep + 1) / bootLogs.size.toFloat()
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(4.dp)
                            .background(DarkGlassSurface)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(progress)
                                .fillMaxHeight()
                                .background(
                                    Brush.horizontalGradient(
                                        colors = listOf(NeonCyan, ElectricBlue, GlowingGreen)
                                    )
                                )
                        )
                    }
                }
            }
        }
    }
}
