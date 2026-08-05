package com.example.ui.theme

import androidx.compose.ui.graphics.Color

// Elegant Dark Obsidian & Neon Cyan Palette
val DeepSpaceBackground = Color(0xFF020408)
val DarkGlassSurface = Color(0xEB060C17)
val GlassBorder = Color(0x3322D3EE)

val NeonCyan = Color(0xFF22D3EE)
val ElectricBlue = Color(0xFF06B6D4)
val QuantumPurple = Color(0xFF38BDF8)
val ArcReactorCore = Color(0xFFE0F2FE)

val GlowingGreen = Color(0xFF34D399)
val AlertOrange = Color(0xFFF59E0B)
val DangerRed = Color(0xFFEF4444)

val TextPrimary = Color(0xFFF8FAFC)
val TextSecondary = Color(0xFF94A3B8)
val TextMuted = Color(0xFF64748B)

val DarkColorScheme = androidx.compose.material3.darkColorScheme(
    primary = NeonCyan,
    onPrimary = DeepSpaceBackground,
    primaryContainer = Color(0xFF083344),
    onPrimaryContainer = NeonCyan,
    secondary = ElectricBlue,
    onSecondary = TextPrimary,
    secondaryContainer = Color(0xFF062C43),
    onSecondaryContainer = ElectricBlue,
    tertiary = QuantumPurple,
    onTertiary = TextPrimary,
    background = DeepSpaceBackground,
    onBackground = TextPrimary,
    surface = DarkGlassSurface,
    onSurface = TextPrimary,
    surfaceVariant = Color(0xFF0B132B),
    onSurfaceVariant = TextSecondary,
    outline = GlassBorder
)

