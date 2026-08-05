package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import com.example.utils.glassmorphic

private data class NavItem(
    val screen: NavigationScreen,
    val title: String,
    val icon: ImageVector
)

@Composable
fun HudNavigationBar(
    currentScreen: NavigationScreen,
    onNavigate: (NavigationScreen) -> Unit
) {
    val items = listOf(
        NavItem(NavigationScreen.HOME, "Home", Icons.Default.Home),
        NavItem(NavigationScreen.CHAT, "Chat", Icons.AutoMirrored.Filled.Chat),
        NavItem(NavigationScreen.VOICE, "Voice", Icons.Default.Mic),
        NavItem(NavigationScreen.VISION, "Vision", Icons.Default.CameraAlt),
        NavItem(NavigationScreen.SKILLS, "Skills", Icons.Default.Extension),
        NavItem(NavigationScreen.ROUTINES, "Routines", Icons.Default.AutoAwesome),
        NavItem(NavigationScreen.DASHBOARD, "Metrics", Icons.Default.Analytics),
        NavItem(NavigationScreen.SECURITY, "Security", Icons.Default.Shield),
        NavItem(NavigationScreen.SETTINGS, "Settings", Icons.Default.Settings)
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 8.dp)
            .windowInsetsPadding(WindowInsets.navigationBars)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .glassmorphic(cornerRadius = 28.dp, borderColor = GlassBorder)
                .padding(vertical = 6.dp, horizontal = 8.dp)
        ) {
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                items(items, key = { it.screen.name }) { item ->
                    val selected = currentScreen == item.screen
                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(if (selected) ElectricBlue.copy(alpha = 0.3f) else androidx.compose.ui.graphics.Color.Transparent)
                            .clickable { onNavigate(item.screen) }
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                            .testTag("nav_${item.screen.name.lowercase()}")
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = item.icon,
                                contentDescription = item.title,
                                tint = if (selected) NeonCyan else TextMuted,
                                modifier = Modifier.size(20.dp)
                            )
                            if (selected) {
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = item.title.uppercase(),
                                    color = NeonCyan,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
