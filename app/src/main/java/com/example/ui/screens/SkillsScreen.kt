package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.models.MobileSkill
import com.example.ui.JarvisViewModel
import com.example.ui.theme.*
import com.example.utils.glassmorphic

@Composable
fun SkillsScreen(viewModel: JarvisViewModel) {
    val context = LocalContext.current
    val skills = remember { viewModel.skillsManager.availableSkills }
    var selectedSkillForDialog by remember { mutableStateOf<MobileSkill?>(null) }
    var inputParam by remember { mutableStateOf("") }

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
                        text = "MOBILE SKILLS MATRIX",
                        color = NeonCyan,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = 2.sp
                    )
                    Text(
                        text = "HARDWARE & OS CONTROL SUBSYSTEMS",
                        color = TextSecondary,
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }

                Icon(
                    imageVector = Icons.Default.Extension,
                    contentDescription = "Skills Matrix",
                    tint = ElectricBlue
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Grid of Mobile Skills
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(skills, key = { it.id }) { skill ->
                    SkillCard(
                        skill = skill,
                        onClick = {
                            if (skill.actionKey in listOf("ACTION_CALL", "ACTION_SMS", "ACTION_MAPS", "ACTION_APP_LAUNCH")) {
                                selectedSkillForDialog = skill
                            } else {
                                val result = viewModel.skillsManager.executeSkill(skill.actionKey)
                                Toast.makeText(context, result, Toast.LENGTH_SHORT).show()
                            }
                        }
                    )
                }
            }
        }

        // Skill Parameter Dialog
        selectedSkillForDialog?.let { skill ->
            AlertDialog(
                onDismissRequest = { selectedSkillForDialog = null },
                containerColor = DarkGlassSurface,
                title = {
                    Text(
                        text = "EXECUTE ${skill.title.uppercase()}",
                        color = NeonCyan,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                },
                text = {
                    Column {
                        Text(
                            text = skill.description,
                            color = TextSecondary,
                            fontSize = 12.sp,
                            fontFamily = FontFamily.Monospace
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        OutlinedTextField(
                            value = inputParam,
                            onValueChange = { inputParam = it },
                            label = { Text("Parameter (Phone / Query / App Name)", color = TextMuted) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = NeonCyan,
                                unfocusedBorderColor = GlassBorder,
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("tf_skill_param")
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            val result = viewModel.skillsManager.executeSkill(skill.actionKey, inputParam)
                            Toast.makeText(context, result, Toast.LENGTH_SHORT).show()
                            selectedSkillForDialog = null
                            inputParam = ""
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = NeonCyan, contentColor = DeepSpaceBackground),
                        modifier = Modifier.testTag("btn_skill_confirm")
                    ) {
                        Text("EXECUTE", fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { selectedSkillForDialog = null }) {
                        Text("CANCEL", color = TextMuted, fontFamily = FontFamily.Monospace)
                    }
                }
            )
        }
    }
}

@Composable
fun SkillCard(
    skill: MobileSkill,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .glassmorphic(cornerRadius = 16.dp)
            .clickable { onClick() }
            .padding(14.dp)
            .testTag("skill_${skill.id}")
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = skill.category.uppercase(),
                    color = ElectricBlue,
                    fontSize = 9.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                )

                Icon(
                    imageVector = getSkillIcon(skill.id),
                    contentDescription = skill.title,
                    tint = NeonCyan,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = skill.title,
                color = TextPrimary,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = skill.description,
                color = TextSecondary,
                fontSize = 10.sp,
                maxLines = 2
            )
        }
    }
}

private fun getSkillIcon(id: String): ImageVector {
    return when (id) {
        "flashlight" -> Icons.Default.FlashOn
        "wifi" -> Icons.Default.Wifi
        "bluetooth" -> Icons.Default.Bluetooth
        "hotspot" -> Icons.Default.WifiTethering
        "volume" -> Icons.Default.VolumeUp
        "brightness" -> Icons.Default.Brightness6
        "phone" -> Icons.Default.Phone
        "sms" -> Icons.Default.Sms
        "email" -> Icons.Default.Email
        "alarm" -> Icons.Default.Alarm
        "calendar" -> Icons.Default.CalendarToday
        "calculator" -> Icons.Default.Calculate
        "maps" -> Icons.Default.Map
        "gallery" -> Icons.Default.PhotoLibrary
        "files" -> Icons.Default.Folder
        "clipboard" -> Icons.Default.Assignment
        else -> Icons.Default.Extension
    }
}
