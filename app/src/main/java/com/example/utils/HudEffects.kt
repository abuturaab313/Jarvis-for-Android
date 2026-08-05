package com.example.utils

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.ui.theme.*
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

// Futuristic Glassmorphism Modifier
fun Modifier.glassmorphic(
    cornerRadius: Dp = 16.dp,
    borderColor: Color = GlassBorder,
    borderWidth: Dp = 1.dp,
    backgroundColor: Color = DarkGlassSurface,
    glowColor: Color = NeonCyan.copy(alpha = 0.15f)
): Modifier = this
    .shadow(
        elevation = 8.dp,
        shape = RoundedCornerShape(cornerRadius),
        ambientColor = glowColor,
        spotColor = glowColor
    )
    .clip(RoundedCornerShape(cornerRadius))
    .background(backgroundColor)
    .border(
        width = borderWidth,
        brush = Brush.linearGradient(
            colors = listOf(
                borderColor,
                borderColor.copy(alpha = 0.1f),
                borderColor.copy(alpha = 0.4f)
            )
        ),
        shape = RoundedCornerShape(cornerRadius)
    )

// Custom Neon Glow Effect
fun Modifier.neonGlow(
    color: Color = NeonCyan,
    alpha: Float = 0.3f
): Modifier = this.drawBehind {
    drawRect(
        brush = Brush.radialGradient(
            colors = listOf(color.copy(alpha = alpha), Color.Transparent),
            center = center,
            radius = size.maxDimension / 1.5f
        )
    )
}

// Particle Engine for Space/Tech background
private data class Particle(
    var x: Float,
    var y: Float,
    var vx: Float,
    var vy: Float,
    var radius: Float,
    var alpha: Float
)

@Composable
fun ParticleBackground(
    modifier: Modifier = Modifier,
    particleCount: Int = 40,
    color: Color = NeonCyan
) {
    val infiniteTransition = rememberInfiniteTransition(label = "particles")
    val animState by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "particleLoop"
    )

    val particles = remember {
        List(particleCount) {
            Particle(
                x = Random.nextFloat(),
                y = Random.nextFloat(),
                vx = (Random.nextFloat() - 0.5f) * 0.002f,
                vy = (Random.nextFloat() - 0.5f) * 0.002f,
                radius = Random.nextFloat() * 3f + 1.5f,
                alpha = Random.nextFloat() * 0.6f + 0.2f
            )
        }
    }

    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height

        // Background Holographic Grid Lines
        val gridStep = 60.dp.toPx()
        var xGrid = 0f
        while (xGrid < width) {
            drawLine(
                color = color.copy(alpha = 0.04f),
                start = Offset(xGrid, 0f),
                end = Offset(xGrid, height),
                strokeWidth = 1f
            )
            xGrid += gridStep
        }
        var yGrid = 0f
        while (yGrid < height) {
            drawLine(
                color = color.copy(alpha = 0.04f),
                start = Offset(0f, yGrid),
                end = Offset(width, yGrid),
                strokeWidth = 1f
            )
            yGrid += gridStep
        }

        // Draw and update moving particles
        particles.forEach { p ->
            p.x = (p.x + p.vx + animState * 0f) % 1f
            p.y = (p.y + p.vy + animState * 0f) % 1f
            if (p.x < 0) p.x += 1f
            if (p.y < 0) p.y += 1f

            val px = p.x * width
            val py = p.y * height

            drawCircle(
                color = color.copy(alpha = p.alpha),
                radius = p.radius.dp.toPx(),
                center = Offset(px, py)
            )
        }
    }
}

// Glowing Arc Reactor AI Core Component
@Composable
fun AnimatedAiCore(
    modifier: Modifier = Modifier,
    isListening: Boolean = false,
    isSpeaking: Boolean = false,
    audioLevel: Float = 0.5f,
    onClick: () -> Unit = {}
) {
    val infiniteTransition = rememberInfiniteTransition(label = "ai_core")

    val outerRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(8000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "outer_rot"
    )

    val innerRotation by infiniteTransition.animateFloat(
        initialValue = 360f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(5000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "inner_rot"
    )

    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    val coreColor = when {
        isSpeaking -> GlowingGreen
        isListening -> DangerRed
        else -> NeonCyan
    }

    Canvas(modifier = modifier) {
        val centerOffset = Offset(size.width / 2, size.height / 2)
        val maxRadius = size.minDimension / 2 * (if (isListening || isSpeaking) 1f + audioLevel * 0.2f else pulseScale)

        // Outer Glow Aura
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    coreColor.copy(alpha = 0.4f),
                    ElectricBlue.copy(alpha = 0.15f),
                    Color.Transparent
                ),
                center = centerOffset,
                radius = maxRadius * 1.3f
            ),
            radius = maxRadius * 1.3f,
            center = centerOffset
        )

        // Outer Rotating HUD Segmented Ring
        rotate(degrees = outerRotation, pivot = centerOffset) {
            val ringRadius = maxRadius * 0.85f
            val segments = 8
            val sweep = 360f / segments - 12f
            for (i in 0 until segments) {
                drawArc(
                    color = coreColor.copy(alpha = 0.7f),
                    startAngle = i * (360f / segments),
                    sweepAngle = sweep,
                    useCenter = false,
                    topLeft = Offset(centerOffset.x - ringRadius, centerOffset.y - ringRadius),
                    size = Size(ringRadius * 2, ringRadius * 2),
                    style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
                )
            }
        }

        // Inner Rotating Dotted Ring
        rotate(degrees = innerRotation, pivot = centerOffset) {
            val innerRingRadius = maxRadius * 0.65f
            val dots = 12
            for (i in 0 until dots) {
                val angleRad = Math.toRadians((i * (360.0 / dots)).toDouble())
                val dotX = centerOffset.x + (innerRingRadius * cos(angleRad)).toFloat()
                val dotY = centerOffset.y + (innerRingRadius * sin(angleRad)).toFloat()
                drawCircle(
                    color = ElectricBlue.copy(alpha = 0.9f),
                    radius = 2.5f.dp.toPx(),
                    center = Offset(dotX, dotY)
                )
            }
        }

        // Arc Reactor Central Core
        val coreRadius = maxRadius * 0.42f
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    ArcReactorCore,
                    coreColor,
                    DeepSpaceBackground
                ),
                center = centerOffset,
                radius = coreRadius
            ),
            radius = coreRadius,
            center = centerOffset
        )

        // Triangle Core Overlay Emblem
        val triRadius = coreRadius * 0.6f
        val path = Path().apply {
            moveTo(centerOffset.x, centerOffset.y - triRadius)
            lineTo(
                centerOffset.x + (triRadius * sin(Math.toRadians(120.0))).toFloat(),
                centerOffset.y - (triRadius * cos(Math.toRadians(120.0))).toFloat()
            )
            lineTo(
                centerOffset.x - (triRadius * sin(Math.toRadians(120.0))).toFloat(),
                centerOffset.y - (triRadius * cos(Math.toRadians(120.0))).toFloat()
            )
            close()
        }
        drawPath(
            path = path,
            color = DeepSpaceBackground.copy(alpha = 0.7f),
            style = Stroke(width = 2.dp.toPx())
        )
    }
}

// Equalizer Waveform Composable for Voice Speaking / Listening
@Composable
fun VoiceWaveEqualizer(
    modifier: Modifier = Modifier,
    barCount: Int = 16,
    isAnimating: Boolean = true,
    color: Color = NeonCyan
) {
    val infiniteTransition = rememberInfiniteTransition(label = "equalizer")
    val animPhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 2f * Math.PI.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "phase"
    )

    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height
        val barWidth = width / (barCount * 1.8f)
        val gap = barWidth * 0.8f

        for (i in 0 until barCount) {
            val factor = if (isAnimating) {
                (sin(animPhase + i * 0.5f) * 0.4f + 0.6f)
            } else 0.2f

            val barHeight = height * factor
            val x = i * (barWidth + gap) + gap / 2
            val y = (height - barHeight) / 2

            drawRoundRect(
                brush = Brush.verticalGradient(
                    colors = listOf(color, ElectricBlue)
                ),
                topLeft = Offset(x, y),
                size = Size(barWidth, barHeight),
                cornerRadius = CornerRadius(barWidth / 2, barWidth / 2)
            )
        }
    }
}
