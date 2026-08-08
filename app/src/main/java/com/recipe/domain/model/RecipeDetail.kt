package com.recipe.domain.model

data class Ingredient(
    val name: String,
    val measure: String,
)

data class RecipeDetail(
    val id: String,
    val name: String,
    val imageUrl: String,
    val category: String,
    val area: String,
    val ingredients: List<Ingredient>,
    val instructions: String,
)
