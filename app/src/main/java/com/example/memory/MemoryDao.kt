package com.example.memory

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface MemoryDao {
    @Query("SELECT * FROM long_term_memories ORDER BY importanceScore DESC, timestamp DESC")
    fun getAllMemoriesFlow(): Flow<List<MemoryEntity>>

    @Query("SELECT * FROM long_term_memories ORDER BY importanceScore DESC, timestamp DESC")
    suspend fun getAllMemories(): List<MemoryEntity>

    @Query("SELECT * FROM long_term_memories WHERE importanceScore >= :minScore ORDER BY importanceScore DESC, lastAccessedTimestamp DESC LIMIT :limit")
    suspend fun getTopMemories(minScore: Float = 0.4f, limit: Int = 20): List<MemoryEntity>

    @Query("SELECT * FROM long_term_memories WHERE content LIKE '%' || :query || '%' OR relevanceKeywords LIKE '%' || :query || '%' OR category LIKE '%' || :query || '%'")
    suspend fun searchMemories(query: String): List<MemoryEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMemory(memory: MemoryEntity): Long

    @Update
    suspend fun updateMemory(memory: MemoryEntity)

    @Delete
    suspend fun deleteMemory(memory: MemoryEntity)

    @Query("DELETE FROM long_term_memories WHERE id = :id")
    suspend fun deleteMemoryById(id: Long)

    @Query("DELETE FROM long_term_memories")
    suspend fun clearAll()

    @Query("UPDATE long_term_memories SET accessCount = accessCount + 1, lastAccessedTimestamp = :timestamp WHERE id = :id")
    suspend fun incrementAccessCount(id: Long, timestamp: Long = System.currentTimeMillis())
}
