package com.example.memory

data class ExtractedMemory(
    val content: String,
    val category: String,
    val importanceScore: Float,
    val keywords: List<String>
)

class MemoryScorer {

    fun scoreAndExtractMemory(userPrompt: String, aiResponse: String): ExtractedMemory? {
        val cleanPrompt = userPrompt.trim()
        val lowerPrompt = cleanPrompt.lowercase()

        // Explicit user memory commands
        if (lowerPrompt.startsWith("remember that ") || lowerPrompt.startsWith("remember: ") || lowerPrompt.startsWith("remember ") || lowerPrompt.startsWith("note that ")) {
            val fact = cleanPrompt
                .replace(Regex("^(remember that|remember:|remember|note that)\\s*", RegexOption.IGNORE_CASE), "")
                .trim()
            if (fact.isNotBlank()) {
                val keywords = extractKeywords(fact)
                return ExtractedMemory(
                    content = fact,
                    category = "EXPLICIT_INSTRUCTION",
                    importanceScore = 0.95f,
                    keywords = keywords
                )
            }
        }

        // Detect user preferences / identity assertions
        val prefPatterns = listOf(
            Regex("my name is ([A-Za-z0-9\\s]+)", RegexOption.IGNORE_CASE),
            Regex("i prefer ([A-Za-z0-9\\s,]+)", RegexOption.IGNORE_CASE),
            Regex("i like ([A-Za-z0-9\\s,]+)", RegexOption.IGNORE_CASE),
            Regex("i love ([A-Za-z0-9\\s,]+)", RegexOption.IGNORE_CASE),
            Regex("i hate ([A-Za-z0-9\\s,]+)", RegexOption.IGNORE_CASE),
            Regex("i work as ([A-Za-z0-9\\s]+)", RegexOption.IGNORE_CASE),
            Regex("i live in ([A-Za-z0-9\\s]+)", RegexOption.IGNORE_CASE),
            Regex("my favorite ([A-Za-z0-9\\s]+) is ([A-Za-z0-9\\s]+)", RegexOption.IGNORE_CASE),
            Regex("call me ([A-Za-z0-9\\s]+)", RegexOption.IGNORE_CASE),
            Regex("always ([A-Za-z0-9\\s]+)", RegexOption.IGNORE_CASE),
            Regex("never ([A-Za-z0-9\\s]+)", RegexOption.IGNORE_CASE)
        )

        for (pattern in prefPatterns) {
            val match = pattern.find(cleanPrompt)
            if (match != null) {
                val keywords = extractKeywords(cleanPrompt)
                val category = when {
                    lowerPrompt.contains("name") || lowerPrompt.contains("call me") -> "USER_IDENTITY"
                    lowerPrompt.contains("prefer") || lowerPrompt.contains("like") || lowerPrompt.contains("favorite") -> "USER_PREFERENCE"
                    lowerPrompt.contains("work") || lowerPrompt.contains("live") -> "USER_PROFILE"
                    else -> "USER_RULE"
                }
                return ExtractedMemory(
                    content = cleanPrompt,
                    category = category,
                    importanceScore = 0.85f,
                    keywords = keywords
                )
            }
        }

        // Implicit important facts extraction from AI response or task creation
        if (aiResponse.contains("Task Created", ignoreCase = true) || aiResponse.contains("Routine Enabled", ignoreCase = true)) {
            val summary = cleanPrompt.take(60)
            return ExtractedMemory(
                content = "User executed automation/task: $summary",
                category = "SYSTEM_ACTION",
                importanceScore = 0.60f,
                keywords = extractKeywords(summary)
            )
        }

        return null
    }

    private fun extractKeywords(text: String): List<String> {
        val stopWords = setOf("i", "me", "my", "is", "am", "are", "a", "an", "the", "that", "this", "to", "in", "it", "of", "and", "or")
        return text.lowercase()
            .replace(Regex("[^a-z0-9\\s]"), " ")
            .split("\\s+".toRegex())
            .filter { it.length > 2 && !stopWords.contains(it) }
            .distinct()
            .take(6)
    }
}
