package com.recipe.auth.data.local

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "shopping_list",
    indices = [Index(value = ["userEmail"])],
)
data class ShoppingListEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val userEmail: String,
    val name: String,
    val isDone: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
)
