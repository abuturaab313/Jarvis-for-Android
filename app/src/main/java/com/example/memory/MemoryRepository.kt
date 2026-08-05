package com.example.memory

import kotlinx.coroutines.flow.Flow

class MemoryRepository(private val memoryDao: MemoryDao) {

    val allMemories: Flow<List<MemoryEntity>> = memoryDao.getAllMemoriesFlow()

    suspend fun saveMemory(
        content: String,
        category: String = "GENERAL",
        importanceScore: Float = 0.5f,
        keywords: List<String> = emptyList()
    ): Long {
        val existing = memoryDao.getAllMemories()
        val duplicate = existing.find { it.content.equals(content.trim(), ignoreCase = true) }
        if (duplicate != null) {
            val updated = duplicate.copy(
                importanceScore = maxOf(duplicate.importanceScore, importanceScore),
                lastAccessedTimestamp = System.currentTimeMillis(),
                accessCount = duplicate.accessCount + 1
            )
            memoryDao.updateMemory(updated)
            return duplicate.id
        }

        val memory = MemoryEntity(
            content = content.trim(),
            category = category,
            importanceScore = importanceScore.coerceIn(0.0f, 1.0f),
            relevanceKeywords = keywords.joinToString(",").lowercase()
        )
        return memoryDao.insertMemory(memory)
    }

    suspend fun getAllMemories(): List<MemoryEntity> = memoryDao.getAllMemories()

    suspend fun getTopMemories(limit: Int = 20): List<MemoryEntity> = memoryDao.getTopMemories(0.3f, limit)

    suspend fun searchMemories(query: String): List<MemoryEntity> = memoryDao.searchMemories(query)

    suspend fun updateAccess(memoryId: Long) {
        memoryDao.incrementAccessCount(memoryId)
    }

    suspend fun deleteMemory(memory: MemoryEntity) {
        memoryDao.deleteMemory(memory)
    }

    suspend fun deleteMemoryById(id: Long) {
        memoryDao.deleteMemoryById(id)
    }

    suspend fun clearAllMemories() {
        memoryDao.clearAll()
    }
}
