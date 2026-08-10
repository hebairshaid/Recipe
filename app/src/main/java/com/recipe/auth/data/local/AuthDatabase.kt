package com.recipe.auth.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [UserEntity::class, FavoriteEntity::class, ShoppingListEntity::class],
    version = 3,
    exportSchema = false,
)
abstract class AuthDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun favoriteDao(): FavoriteDao
    abstract fun shoppingListDao(): ShoppingListDao

    companion object {
        @Volatile
        private var instance: AuthDatabase? = null

        fun getInstance(context: Context): AuthDatabase {
            return instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    AuthDatabase::class.java,
                    "recipe_auth.db",
                )
                    .fallbackToDestructiveMigration(dropAllTables = true)
                    .fallbackToDestructiveMigrationOnDowngrade(dropAllTables = true)
                    .build()
                    .also { instance = it }
            }
        }
    }
}
