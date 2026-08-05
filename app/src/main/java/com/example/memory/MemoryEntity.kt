package com.example.memory

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "long_term_memories")
data class MemoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val content: String,
    val category: String = "GENERAL",
    val importanceScore: Float = 0.5f,
    val relevanceKeywords: String = "",
    val accessCount: Int = 0,
    val lastAccessedTimestamp: Long = System.currentTimeMillis(),
    val timestamp: Long = System.currentTimeMillis()
)
