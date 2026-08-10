package com.recipe.di

import com.recipe.data.repository.FavoritesRepository
import com.recipe.data.repository.RecipeRepository
import com.recipe.data.repository.ShoppingListRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {
    @Provides
    @Singleton
    fun provideFavoritesRepository(
        favoriteDao: com.recipe.auth.data.local.FavoriteDao,
        sessionRepository: com.recipe.auth.domain.session.SessionRepository,
    ): FavoritesRepository {
        return FavoritesRepository(favoriteDao, sessionRepository)
    }

    @Provides
    @Singleton
    fun provideShoppingListRepository(
        shoppingListDao: com.recipe.auth.data.local.ShoppingListDao,
        sessionRepository: com.recipe.auth.domain.session.SessionRepository,
    ): ShoppingListRepository {
        return ShoppingListRepository(shoppingListDao, sessionRepository)
    }

    @Provides
    @Singleton
    fun provideRecipeRepository(
        api: com.recipe.data.remote.TheMealDbApi,
        cacheDao: com.recipe.data.local.RecipeCacheDao,
        networkMonitor: com.recipe.data.network.NetworkMonitor,
    ): RecipeRepository {
        return RecipeRepository(api, cacheDao, networkMonitor)
    }
}
