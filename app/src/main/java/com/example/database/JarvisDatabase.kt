package com.example.database

import android.content.Context
import androidx.room.*
import com.example.models.*
import kotlinx.coroutines.flow.Flow

@Dao
interface ChatDao {
    @Query("SELECT * FROM chat_messages ORDER BY timestamp ASC")
    fun getAllMessages(): Flow<List<ChatMessage>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: ChatMessage): Long

    @Query("DELETE FROM chat_messages")
    suspend fun clearHistory()
}

@Dao
interface RoutineDao {
    @Query("SELECT * FROM routines ORDER BY id DESC")
    fun getAllRoutines(): Flow<List<Routine>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRoutine(routine: Routine): Long

    @Update
    suspend fun updateRoutine(routine: Routine)

    @Delete
    suspend fun deleteRoutine(routine: Routine)
}

@Dao
interface TaskDao {
    @Query("SELECT * FROM tasks ORDER BY isCompleted ASC, priority DESC, dueDate ASC")
    fun getAllTasks(): Flow<List<TaskItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: TaskItem): Long

    @Update
    suspend fun updateTask(task: TaskItem)

    @Query("DELETE FROM tasks WHERE id = :taskId")
    suspend fun deleteTask(taskId: Long)
}

@Dao
interface AnalyticsDao {
    @Query("SELECT * FROM analytics_logs ORDER BY timestamp DESC LIMIT 50")
    fun getRecentLogs(): Flow<List<SystemAnalyticsLog>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: SystemAnalyticsLog)

    @Query("SELECT COUNT(*) FROM analytics_logs")
    suspend fun getTotalLogCount(): Int
}

class Converters {
    @TypeConverter
    fun fromSender(value: Sender): String = value.name

    @TypeConverter
    fun toSender(value: String): Sender = Sender.valueOf(value)

    @TypeConverter
    fun fromTriggerType(value: TriggerType): String = value.name

    @TypeConverter
    fun toTriggerType(value: String): TriggerType = TriggerType.valueOf(value)

    @TypeConverter
    fun fromTaskPriority(value: TaskPriority): String = value.name

    @TypeConverter
    fun toTaskPriority(value: String): TaskPriority = TaskPriority.valueOf(value)
}

@Database(
    entities = [ChatMessage::class, Routine::class, TaskItem::class, SystemAnalyticsLog::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun chatDao(): ChatDao
    abstract fun routineDao(): RoutineDao
    abstract fun taskDao(): TaskDao
    abstract fun analyticsDao(): AnalyticsDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "jarvis_os_db"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}
