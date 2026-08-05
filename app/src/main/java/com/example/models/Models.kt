package com.example.models

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class Sender {
    USER, JARVIS, SYSTEM
}

@Entity(tableName = "chat_messages")
data class ChatMessage(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val sender: Sender,
    val content: String,
    val timestamp: Long = System.currentTimeMillis(),
    val imageUri: String? = null,
    val codeSnippet: String? = null,
    val isStreaming: Boolean = false,
    val actionSuggested: String? = null
)

enum class TriggerType {
    BATTERY, TIME, LOCATION, APP_LAUNCH, NOTIFICATION
}

@Entity(tableName = "routines")
data class Routine(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val triggerType: TriggerType,
    val triggerCondition: String, // e.g. "Battery < 20%", "08:00 AM"
    val actionCommand: String, // e.g. "Enable Low Power Mode & Dim Screen"
    val isEnabled: Boolean = true,
    val lastExecuted: Long = 0
)

enum class TaskPriority {
    LOW, MEDIUM, HIGH, CRITICAL
}

@Entity(tableName = "tasks")
data class TaskItem(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val description: String = "",
    val dueDate: Long = System.currentTimeMillis(),
    val priority: TaskPriority = TaskPriority.MEDIUM,
    val isCompleted: Boolean = false
)

@Entity(tableName = "analytics_logs")
data class SystemAnalyticsLog(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val action: String,
    val category: String,
    val timestamp: Long = System.currentTimeMillis(),
    val details: String = ""
)

data class SystemMetrics(
    val batteryLevel: Int = 85,
    val isCharging: Boolean = false,
    val ramUsagePct: Int = 42,
    val storageUsagePct: Int = 58,
    val cpuUsagePct: Int = 24,
    val networkType: String = "Wi-Fi (5G)",
    val isWifiConnected: Boolean = true,
    val isBluetoothConnected: Boolean = true,
    val uptimeHours: Int = 14
)

data class MobileSkill(
    val id: String,
    val title: String,
    val category: String,
    val description: String,
    val iconName: String,
    val actionKey: String
)

data class VisionAnalysisResult(
    val timestamp: Long = System.currentTimeMillis(),
    val recognizedText: String = "",
    val labels: List<String> = emptyList(),
    val barcodeValue: String = "",
    val summary: String = ""
)
