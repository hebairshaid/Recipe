package com.recipe.chat.domain.repository

interface IngredientAnalyzer {
    fun analyze(text: String): List<String>
}
