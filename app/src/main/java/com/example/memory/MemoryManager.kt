package com.example.memory

import kotlinx.coroutines.flow.Flow

class MemoryManager(
    val repository: MemoryRepository,
    private val retriever: MemoryRetriever = MemoryRetriever(repository),
    private val scorer: MemoryScorer = MemoryScorer()
) {

    val allMemories: Flow<List<MemoryEntity>> = repository.allMemories

    suspend fun prepareContext(prompt: String): String {
        return retriever.formatMemoryPromptContext(prompt)
    }

    suspend fun processPostResponse(userPrompt: String, aiResponse: String) {
        val extracted = scorer.scoreAndExtractMemory(userPrompt, aiResponse)
        if (extracted != null) {
            repository.saveMemory(
                content = extracted.content,
                category = extracted.category,
                importanceScore = extracted.importanceScore,
                keywords = extracted.keywords
            )
        }
    }

    suspend fun addMemory(
        content: String,
        category: String = "GENERAL",
        importanceScore: Float = 0.5f,
        keywords: List<String> = emptyList()
    ): Long {
        return repository.saveMemory(content, category, importanceScore, keywords)
    }

    suspend fun deleteMemory(memory: MemoryEntity) {
        repository.deleteMemory(memory)
    }

    suspend fun deleteMemoryById(id: Long) {
        repository.deleteMemoryById(id)
    }

    suspend fun clearAllMemories() {
        repository.clearAllMemories()
    }
}
