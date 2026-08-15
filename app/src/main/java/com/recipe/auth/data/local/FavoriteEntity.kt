package com.recipe.auth.data.local

import androidx.room.Entity

@Entity(
    tableName = "favorites",
    primaryKeys = ["userEmail", "recipeId"],
)
data class FavoriteEntity(
    val userEmail: String,
    val recipeId: String,
    val name: String,
    val imageUrl: String,
    val category: String,
    val area: String,
    val ingredientCount: Int,
)
