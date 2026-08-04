package com.recipe.domain.model

data class Recipe(
    val id: String,
    val name: String,
    val imageUrl: String,
    val category: String,
    val area: String,
    val ingredientCount: Int,
)
