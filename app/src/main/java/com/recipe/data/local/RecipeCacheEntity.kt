package com.recipe.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "recipe_cache")
data class RecipeCacheEntity(
    @PrimaryKey val id: String,
    val name: String,
    val imageUrl: String,
    val category: String,
    val area: String,
    val ingredientCount: Int,
    val letterIndex: Int,
    val instructions: String = "",
    val ingredientsJson: String = "[]",
    val cachedAt: Long = System.currentTimeMillis(),
)
