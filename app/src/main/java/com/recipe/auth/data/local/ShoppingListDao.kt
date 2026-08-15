package com.recipe.auth.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ShoppingListDao {
    @Query(
        """
        SELECT * FROM shopping_list
        WHERE userEmail = :userEmail
        ORDER BY isDone ASC, createdAt DESC
        """,
    )
    fun observeByUser(userEmail: String): Flow<List<ShoppingListEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: ShoppingListEntity): Long

    @Query(
        """
        UPDATE shopping_list
        SET name = :name
        WHERE id = :id AND userEmail = :userEmail
        """,
    )
    suspend fun updateName(id: Long, userEmail: String, name: String): Int

    @Query(
        """
        UPDATE shopping_list
        SET isDone = :isDone
        WHERE id = :id AND userEmail = :userEmail
        """,
    )
    suspend fun updateDone(id: Long, userEmail: String, isDone: Boolean): Int

    @Query(
        """
        DELETE FROM shopping_list
        WHERE id = :id AND userEmail = :userEmail
        """,
    )
    suspend fun delete(id: Long, userEmail: String): Int
}
