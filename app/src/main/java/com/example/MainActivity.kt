package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.example.ui.HudNavigationBar
import com.example.ui.JarvisViewModel
import com.example.ui.NavigationScreen
import com.example.ui.screens.*
import com.example.ui.theme.JarvisTheme

class MainActivity : ComponentActivity() {

    private val viewModel: JarvisViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            JarvisTheme {
                val currentScreen by viewModel.currentScreen.collectAsState()

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    containerColor = com.example.ui.theme.DeepSpaceBackground
                ) { padding ->
                    Box(modifier = Modifier.fillMaxSize()) {
                        Crossfade(
                            targetState = currentScreen,
                            label = "screen_transition"
                        ) { screen ->
                            when (screen) {
                                NavigationScreen.SPLASH -> SplashScreen(
                                    onBootComplete = {
                                        if (!viewModel.hasApiKey()) {
                                            viewModel.navigateTo(NavigationScreen.API_KEY_SETUP)
                                        } else {
                                            viewModel.navigateTo(NavigationScreen.HOME)
                                        }
                                    }
                                )
                                NavigationScreen.API_KEY_SETUP -> ApiKeySetupScreen(
                                    viewModel = viewModel,
                                    onSetupComplete = {
                                        viewModel.navigateTo(NavigationScreen.HOME)
                                    }
                                )
                                NavigationScreen.HOME -> HomeScreen(
                                    viewModel = viewModel,
                                    onNavigate = { viewModel.navigateTo(it) }
                                )
                                NavigationScreen.CHAT -> ChatScreen(viewModel = viewModel)
                                NavigationScreen.VOICE -> VoiceScreen(viewModel = viewModel)
                                NavigationScreen.VISION -> VisionScreen(viewModel = viewModel)
                                NavigationScreen.SKILLS -> SkillsScreen(viewModel = viewModel)
                                NavigationScreen.ROUTINES -> AutomationScreen(viewModel = viewModel)
                                NavigationScreen.DASHBOARD -> DashboardScreen(viewModel = viewModel)
                                NavigationScreen.SECURITY -> SecurityScreen(viewModel = viewModel)
                                NavigationScreen.SETTINGS -> SettingsScreen(viewModel = viewModel)
                            }
                        }

                        // Floating HUD Navigation Bar shown when not on Splash or API Key Setup screens
                        if (currentScreen != NavigationScreen.SPLASH && currentScreen != NavigationScreen.API_KEY_SETUP) {
                            Box(
                                modifier = Modifier.align(Alignment.BottomCenter)
                            ) {
                                HudNavigationBar(
                                    currentScreen = currentScreen,
                                    onNavigate = { viewModel.navigateTo(it) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
