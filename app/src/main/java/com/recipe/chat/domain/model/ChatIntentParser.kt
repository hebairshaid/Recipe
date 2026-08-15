package com.recipe.chat.domain.model

enum class ChatIntent {
    GREETING,
    HELP,
    SUGGEST_GENERAL,
    SUGGEST_KEYWORD,
    BY_INGREDIENTS,
}

data class ParsedChatRequest(
    val intent: ChatIntent,
    val keyword: String? = null,
)

object ChatIntentParser {
    private val greetingWords = setOf("hi", "hello", "hey", "good morning", "good evening")
    private val helpWords = setOf("help", "how", "what can you do")
    private val suggestTriggers = listOf(
        "suggest",
        "recommend",
        "give me",
        "show me",
        "find me",
        "i want",
        "need a",
        "any recipe",
        "recipe for",
        "recipe with",
        "what can i make",
        "what can i cook",
        "ideas for",
    )

    fun parse(message: String, detectedIngredients: List<String>): ParsedChatRequest {
        val lower = message.trim().lowercase()

        if (lower.isBlank()) {
            return ParsedChatRequest(ChatIntent.HELP)
        }

        if (greetingWords.any { lower == it || lower.startsWith("$it ") }) {
            return ParsedChatRequest(ChatIntent.GREETING)
        }

        if (helpWords.any { lower.contains(it) }) {
            return ParsedChatRequest(ChatIntent.HELP)
        }

        if (detectedIngredients.isNotEmpty()) {
            return ParsedChatRequest(ChatIntent.BY_INGREDIENTS)
        }

        val keyword = extractKeyword(lower)
        if (keyword != null) {
            return ParsedChatRequest(ChatIntent.SUGGEST_KEYWORD, keyword = keyword)
        }

        if (suggestTriggers.any { lower.contains(it) } || lower.contains("recipe")) {
            return ParsedChatRequest(ChatIntent.SUGGEST_GENERAL)
        }

        return ParsedChatRequest(ChatIntent.HELP)
    }

    private fun extractKeyword(lower: String): String? {
        val patterns = listOf(
            Regex("(?:suggest|recommend|give me|show me|find|need)(?: a| an| some)?\\s+(.+?)(?:\\s+recipe)?$"),
            Regex("recipe(?:s)?\\s+(?:for|with)\\s+(.+)$"),
            Regex("^(?:make|cook)\\s+(.+)$"),
        )
        for (pattern in patterns) {
            val match = pattern.find(lower)?.groupValues?.getOrNull(1)?.trim()
            if (!match.isNullOrBlank() && !isGenericPhrase(match)) {
                return match.removeSuffix(" recipes").removeSuffix(" recipe").trim()
            }
        }
        return null
    }

    private fun isGenericPhrase(text: String): Boolean {
        return text in setOf(
            "a recipe",
            "some recipe",
            "something",
            "anything",
            "food",
            "dinner",
            "lunch",
            "breakfast",
        )
    }
}
