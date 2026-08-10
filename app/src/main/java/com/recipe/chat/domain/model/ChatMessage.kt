package com.recipe.chat.domain.model

import com.recipe.domain.model.Recipe

enum class ChatRole {
    USER,
    ASSISTANT,
}

data class ChatMessage(
    val id: String,
    val role: ChatRole,
    val text: String,
    val recipes: List<Recipe> = emptyList(),
    val detectedIngredients: List<String> = emptyList(),
    val isLoading: Boolean = false,
)
