package com.example.automation

import android.content.Context
import com.example.database.JarvisRepository
import com.example.mobile.MobileSkillsManager
import com.example.models.Routine
import com.example.models.TriggerType
import com.example.utils.SystemMonitor
import kotlinx.coroutines.flow.first
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class AutomationEngine(
    private val context: Context,
    private val repository: JarvisRepository,
    private val skillsManager: MobileSkillsManager,
    private val systemMonitor: SystemMonitor
) {
    suspend fun checkAndTriggerRoutines(): List<String> {
        val triggeredLogs = mutableListOf<String>()
        val activeRoutines = repository.routines.first().filter { it.isEnabled }
        val currentMetrics = systemMonitor.getMetrics()
        val currentTime = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())

        activeRoutines.forEach { routine ->
            var shouldTrigger = false
            when (routine.triggerType) {
                TriggerType.BATTERY -> {
                    val targetLevel = routine.triggerCondition.filter { it.isDigit() }.toIntOrNull() ?: 20
                    if (currentMetrics.batteryLevel <= targetLevel && !currentMetrics.isCharging) {
                        shouldTrigger = true
                    }
                }
                TriggerType.TIME -> {
                    if (routine.triggerCondition.contains(currentTime)) {
                        shouldTrigger = true
                    }
                }
                TriggerType.LOCATION -> {
                    // Location trigger simulated logic
                    if (routine.triggerCondition.contains("Home", ignoreCase = true)) {
                        shouldTrigger = true
                    }
                }
                TriggerType.APP_LAUNCH -> {
                    // App launch trigger
                }
                TriggerType.NOTIFICATION -> {
                    // Notification trigger
                }
            }

            if (shouldTrigger && (System.currentTimeMillis() - routine.lastExecuted > 60_000)) {
                val result = skillsManager.executeSkill("app_launcher", routine.actionCommand)
                repository.logAnalytics("ROUTINE_TRIGGERED", "AUTOMATION", "${routine.title}: $result")
                triggeredLogs.add("Executed '${routine.title}': $result")
            }
        }
        return triggeredLogs
    }
}
