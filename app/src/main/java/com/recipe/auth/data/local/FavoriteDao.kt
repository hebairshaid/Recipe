package com.recipe.auth.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface FavoriteDao {
    @Query("SELECT * FROM favorites WHERE userEmail = :userEmail ORDER BY name ASC")
    fun observeByUser(userEmail: String): Flow<List<FavoriteEntity>>

    @Query("SELECT * FROM favorites WHERE userEmail = :userEmail AND recipeId = :recipeId LIMIT 1")
    suspend fun getFavorite(userEmail: String, recipeId: String): FavoriteEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(favorite: FavoriteEntity)

    @Query("DELETE FROM favorites WHERE userEmail = :userEmail AND recipeId = :recipeId")
    suspend fun delete(userEmail: String, recipeId: String)
}
