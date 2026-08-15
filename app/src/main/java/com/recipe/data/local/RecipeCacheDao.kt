package com.recipe.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface RecipeCacheDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(recipes: List<RecipeCacheEntity>)

    @Query("SELECT * FROM recipe_cache WHERE letterIndex = :letterIndex ORDER BY name ASC")
    suspend fun getByLetter(letterIndex: Int): List<RecipeCacheEntity>

    @Query("SELECT * FROM recipe_cache ORDER BY name ASC")
    suspend fun getAll(): List<RecipeCacheEntity>

    @Query(
        """
        SELECT * FROM recipe_cache
        WHERE name LIKE '%' || :query || '%'
           OR category LIKE '%' || :query || '%'
        ORDER BY name ASC
        """,
    )
    suspend fun search(query: String): List<RecipeCacheEntity>

    @Query("SELECT * FROM recipe_cache WHERE id = :id LIMIT 1")
    suspend fun getById(id: String): RecipeCacheEntity?
}
