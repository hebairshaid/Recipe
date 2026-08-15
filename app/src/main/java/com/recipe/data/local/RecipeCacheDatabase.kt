package com.recipe.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [RecipeCacheEntity::class],
    version = 1,
    exportSchema = false,
)
abstract class RecipeCacheDatabase : RoomDatabase() {
    abstract fun recipeCacheDao(): RecipeCacheDao

    companion object {
        @Volatile
        private var instance: RecipeCacheDatabase? = null

        fun getInstance(context: Context): RecipeCacheDatabase {
            return instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    RecipeCacheDatabase::class.java,
                    "recipe_cache.db",
                )
                    .fallbackToDestructiveMigration(dropAllTables = true)
                    .build()
                    .also { instance = it }
            }
        }
    }
}
