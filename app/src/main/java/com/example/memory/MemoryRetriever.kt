package com.example.memory

class MemoryRetriever(
    private val repository: MemoryRepository,
    private val searchEngine: MemorySearchEngine = MemorySearchEngine()
) {

    suspend fun retrieveRelevantMemories(
        prompt: String,
        maxResults: Int = 5
    ): List<MemoryEntity> {
        val allMemories = repository.getAllMemories()
        if (allMemories.isEmpty()) return emptyList()

        val relevant = searchEngine.findRelevantMemories(allMemories, prompt, maxResults)

        val topCritical = allMemories.filter { it.importanceScore >= 0.8f && !relevant.contains(it) }.take(2)

        val combined = (relevant + topCritical).distinctBy { it.id }

        combined.forEach { memory ->
            repository.updateAccess(memory.id)
        }

        return combined
    }

    suspend fun formatMemoryPromptContext(prompt: String): String {
        val memories = retrieveRelevantMemories(prompt)
        if (memories.isEmpty()) return ""

        val sb = StringBuilder()
        sb.append("\n\n[JARVIS LONG-TERM MEMORY RECALL INJECTED]:\n")
        memories.forEachIndexed { index, m ->
            sb.append("${index + 1}. [${m.category}] ${m.content} (Importance: ${(m.importanceScore * 100).toInt()}%)\n")
        }
        sb.append("Use the above remembered facts naturally in your response when relevant to Boss/Sir.\n")
        return sb.toString()
    }
}
