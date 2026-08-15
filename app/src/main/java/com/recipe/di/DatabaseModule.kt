package com.recipe.di

import android.content.Context
import androidx.room.Room
import com.recipe.auth.data.local.AuthDatabase
import com.recipe.auth.data.local.FavoriteDao
import com.recipe.auth.data.local.ShoppingListDao
import com.recipe.auth.data.local.UserDao
import com.recipe.auth.data.repository.AuthRepositoryImpl
import com.recipe.auth.data.security.BCryptPasswordHasher
import com.recipe.auth.data.security.SecureSessionManager
import com.recipe.auth.domain.repository.AuthRepository
import com.recipe.auth.domain.security.PasswordHasher
import com.recipe.auth.domain.session.SessionRepository
import com.recipe.chat.data.repository.ChatAssistantRepositoryImpl
import com.recipe.chat.domain.repository.ChatAssistantRepository
import com.recipe.data.local.RecipeCacheDao
import com.recipe.data.local.RecipeCacheDatabase
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class AuthBindModule {
    @Binds
    @Singleton
    abstract fun bindAuthRepository(impl: AuthRepositoryImpl): AuthRepository

    @Binds
    @Singleton
    abstract fun bindSessionRepository(impl: SecureSessionManager): SessionRepository

    @Binds
    @Singleton
    abstract fun bindPasswordHasher(impl: BCryptPasswordHasher): PasswordHasher

    @Binds
    @Singleton
    abstract fun bindChatAssistantRepository(
        impl: ChatAssistantRepositoryImpl,
    ): ChatAssistantRepository
}

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    @Provides
    @Singleton
    fun provideAuthDatabase(@ApplicationContext context: Context): AuthDatabase {
        return Room.databaseBuilder(
            context,
            AuthDatabase::class.java,
            "recipe_auth.db",
        )
            .fallbackToDestructiveMigration(dropAllTables = true)
            .fallbackToDestructiveMigrationOnDowngrade(dropAllTables = true)
            .build()
    }

    @Provides
    fun provideUserDao(db: AuthDatabase): UserDao = db.userDao()

    @Provides
    fun provideFavoriteDao(db: AuthDatabase): FavoriteDao = db.favoriteDao()

    @Provides
    fun provideShoppingListDao(db: AuthDatabase): ShoppingListDao = db.shoppingListDao()

    @Provides
    @Singleton
    fun provideRecipeCacheDatabase(@ApplicationContext context: Context): RecipeCacheDatabase {
        return Room.databaseBuilder(
            context,
            RecipeCacheDatabase::class.java,
            "recipe_cache.db",
        )
            .fallbackToDestructiveMigration(dropAllTables = true)
            .build()
    }

    @Provides
    fun provideRecipeCacheDao(db: RecipeCacheDatabase): RecipeCacheDao = db.recipeCacheDao()
}
