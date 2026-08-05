package com.example.memory

import kotlin.math.ln

class MemorySearchEngine {

    fun findRelevantMemories(
        allMemories: List<MemoryEntity>,
        query: String,
        maxResults: Int = 5,
        minScoreThreshold: Float = 0.15f
    ): List<MemoryEntity> {
        if (allMemories.isEmpty() || query.isBlank()) return emptyList()

        val queryTokens = tokenize(query)
        if (queryTokens.isEmpty()) return emptyList()

        val scored = allMemories.map { memory ->
            val relevanceScore = calculateScore(memory, queryTokens, query)
            memory to relevanceScore
        }

        return scored
            .filter { it.second >= minScoreThreshold }
            .sortedByDescending { it.second }
            .take(maxResults)
            .map { it.first }
    }

    fun calculateScore(memory: MemoryEntity, queryTokens: Set<String>, rawQuery: String): Float {
        val memoryTokens = tokenize("${memory.content} ${memory.relevanceKeywords} ${memory.category}")
        if (memoryTokens.isEmpty()) return 0f

        // Token overlap ratio
        val intersection = queryTokens.intersect(memoryTokens)
        val tokenMatchScore = if (queryTokens.isNotEmpty()) {
            intersection.size.toFloat() / queryTokens.size.toFloat()
        } else 0f

        // Exact phrase boost
        val lowerContent = memory.content.lowercase()
        val lowerQuery = rawQuery.lowercase()
        val exactMatchBoost = if (lowerContent.contains(lowerQuery) || lowerQuery.contains(lowerContent)) 0.35f else 0f

        // Category relevance boost
        val categoryBoost = if (queryTokens.any { memory.category.lowercase().contains(it) }) 0.2f else 0f

        // Recency decay factor
        val now = System.currentTimeMillis()
        val ageDays = (now - memory.lastAccessedTimestamp) / (1000f * 60 * 60 * 24)
        val recencyFactor = 1.0f / (1.0f + 0.1f * ageDays)

        // Access frequency boost
        val frequencyFactor = (1.0f + 0.05f * ln((memory.accessCount + 1).toDouble()).toFloat()).coerceAtMost(1.3f)

        val totalScore = ((tokenMatchScore * 0.5f) + exactMatchBoost + categoryBoost + (memory.importanceScore * 0.3f)) * recencyFactor * frequencyFactor
        return totalScore.coerceIn(0.0f, 1.0f)
    }

    private fun tokenize(text: String): Set<String> {
        val stopWords = setOf(
            "a", "an", "the", "and", "or", "but", "is", "are", "was", "were", "be", "been",
            "in", "on", "at", "to", "for", "with", "about", "against", "between", "into",
            "through", "during", "before", "after", "above", "below", "from", "up", "down",
            "of", "off", "over", "under", "again", "further", "then", "once", "here", "there",
            "when", "where", "why", "how", "all", "any", "both", "each", "few", "more", "most",
            "other", "some", "such", "no", "nor", "not", "only", "own", "same", "so", "than",
            "too", "very", "can", "will", "just", "don", "should", "now", "i", "you", "me",
            "my", "we", "he", "she", "it", "they", "them", "what", "which", "who", "whom"
        )
        return text.lowercase()
            .replace(Regex("[^a-z0-9\\s]"), " ")
            .split("\\s+".toRegex())
            .filter { it.length > 1 && !stopWords.contains(it) }
            .toSet()
    }
}
