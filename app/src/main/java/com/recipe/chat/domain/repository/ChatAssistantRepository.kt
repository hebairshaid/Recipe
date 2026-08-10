package com.recipe.chat.domain.repository

import com.recipe.domain.model.Recipe

interface ChatAssistantRepository {
    suspend fun findRecipesByIngredients(ingredients: List<String>): List<Recipe>
    suspend fun suggestRecipes(keyword: String? = null): List<Recipe>
}
