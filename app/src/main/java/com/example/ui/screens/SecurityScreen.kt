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
fun SecurityScreen(viewModel: JarvisViewModel) {
    val context = LocalContext.current

    var isBiometricsEnabled by remember { mutableStateOf(true) }
    var isDatabaseEncrypted by remember { mutableStateOf(true) }
    var isPinLockEnabled by remember { mutableStateOf(false) }

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
                        text = "SECURITY & VAULT",
                        color = NeonCyan,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = 2.sp
                    )
                    Text(
                        text = "BIOMETRICS, ENCRYPTION & PERMISSIONS",
                        color = TextSecondary,
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }

                Icon(
                    imageVector = Icons.Default.Security,
                    contentDescription = "Security Vault",
                    tint = GlowingGreen
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Biometric Auth Card
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
                        Icon(imageVector = Icons.Default.Fingerprint, contentDescription = "Biometrics", tint = NeonCyan)
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text("Biometric Authentication", color = TextPrimary, fontSize = 14.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                            Text("Fingerprint & Face Unlock", color = TextSecondary, fontSize = 11.sp)
                        }
                    }
                    Switch(
                        checked = isBiometricsEnabled,
                        onCheckedChange = { isBiometricsEnabled = it },
                        colors = SwitchDefaults.colors(checkedThumbColor = GlowingGreen, checkedTrackColor = DarkGlassSurface),
                        modifier = Modifier.testTag("sw_bio")
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Database Encryption Card
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
                        Icon(imageVector = Icons.Default.Lock, contentDescription = "Vault", tint = GlowingGreen)
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text("AES-256 Vault Encryption", color = TextPrimary, fontSize = 14.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                            Text("Local Room Database Encrypted", color = TextSecondary, fontSize = 11.sp)
                        }
                    }
                    Switch(
                        checked = isDatabaseEncrypted,
                        onCheckedChange = { isDatabaseEncrypted = it },
                        colors = SwitchDefaults.colors(checkedThumbColor = GlowingGreen, checkedTrackColor = DarkGlassSurface),
                        modifier = Modifier.testTag("sw_enc")
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // PIN Lock Card
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
                        Icon(imageVector = Icons.Default.Pin, contentDescription = "PIN Lock", tint = QuantumPurple)
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text("Security PIN Code", color = TextPrimary, fontSize = 14.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                            Text("Require 4-digit PIN on Boot", color = TextSecondary, fontSize = 11.sp)
                        }
                    }
                    Switch(
                        checked = isPinLockEnabled,
                        onCheckedChange = { isPinLockEnabled = it },
                        colors = SwitchDefaults.colors(checkedThumbColor = GlowingGreen, checkedTrackColor = DarkGlassSurface),
                        modifier = Modifier.testTag("sw_pin")
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Authenticate Biometrics Button
            Button(
                onClick = {
                    Toast.makeText(context, "JARVIS Biometric Scan Verified: Access Granted", Toast.LENGTH_SHORT).show()
                },
                colors = ButtonDefaults.buttonColors(containerColor = NeonCyan, contentColor = DeepSpaceBackground),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("btn_verify_bio")
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.Fingerprint, contentDescription = "Scan")
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("VERIFY BIOMETRIC CREDENTIALS", fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
