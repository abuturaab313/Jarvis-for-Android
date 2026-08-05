package com.example.database

import com.example.models.*
import kotlinx.coroutines.flow.Flow

class JarvisRepository(
    private val db: AppDatabase
) {
    val chatMessages: Flow<List<ChatMessage>> = db.chatDao().getAllMessages()
    val routines: Flow<List<Routine>> = db.routineDao().getAllRoutines()
    val tasks: Flow<List<TaskItem>> = db.taskDao().getAllTasks()
    val analyticsLogs: Flow<List<SystemAnalyticsLog>> = db.analyticsDao().getRecentLogs()

    suspend fun sendChatMessage(message: ChatMessage): Long {
        db.analyticsDao().insertLog(
            SystemAnalyticsLog(action = "CHAT_SENT", category = "AI", details = message.content.take(30))
        )
        return db.chatDao().insertMessage(message)
    }

    suspend fun clearChatHistory() {
        db.chatDao().clearHistory()
    }

    suspend fun addRoutine(routine: Routine): Long {
        db.analyticsDao().insertLog(
            SystemAnalyticsLog(action = "ROUTINE_CREATED", category = "AUTOMATION", details = routine.title)
        )
        return db.routineDao().insertRoutine(routine)
    }

    suspend fun toggleRoutine(routine: Routine) {
        db.routineDao().updateRoutine(routine.copy(isEnabled = !routine.isEnabled))
    }

    suspend fun deleteRoutine(routine: Routine) {
        db.routineDao().deleteRoutine(routine)
    }

    suspend fun addTask(task: TaskItem): Long {
        db.analyticsDao().insertLog(
            SystemAnalyticsLog(action = "TASK_CREATED", category = "TASK", details = task.title)
        )
        return db.taskDao().insertTask(task)
    }

    suspend fun toggleTaskCompleted(task: TaskItem) {
        db.taskDao().updateTask(task.copy(isCompleted = !task.isCompleted))
    }

    suspend fun deleteTask(taskId: Long) {
        db.taskDao().deleteTask(taskId)
    }

    suspend fun logAnalytics(action: String, category: String, details: String = "") {
        db.analyticsDao().insertLog(
            SystemAnalyticsLog(action = action, category = category, details = details)
        )
    }
}
