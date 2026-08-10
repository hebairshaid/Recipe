package com.recipe.data.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.recipe.data.local.RecipeCacheDao
import com.recipe.data.local.letterIndexFromName
import com.recipe.data.local.toCacheEntity
import com.recipe.data.local.toRecipeDetailOrNull
import com.recipe.data.network.NetworkMonitor
import com.recipe.data.paging.RecipePagingSource
import com.recipe.data.remote.TheMealDbApi
import com.recipe.data.remote.toRecipeDetail
import com.recipe.domain.model.Recipe
import com.recipe.domain.model.RecipeDetail
import kotlinx.coroutines.flow.Flow

class RecipeRepository(
    private val api: TheMealDbApi,
    private val cacheDao: RecipeCacheDao,
    private val networkMonitor: NetworkMonitor,
) {
    fun getPagedRecipes(query: String): Flow<PagingData<Recipe>> {
        return Pager(
            config = PagingConfig(
                pageSize = 20,
                initialLoadSize = 20,
                prefetchDistance = 4,
                enablePlaceholders = false,
            ),
            pagingSourceFactory = {
                RecipePagingSource(
                    api = api,
                    cacheDao = cacheDao,
                    networkMonitor = networkMonitor,
                    query = query,
                )
            },
        ).flow
    }

    suspend fun getRecipeDetail(id: String): RecipeDetail {
        if (networkMonitor.currentlyOnline()) {
            try {
                val meal = api.mealById(id).meals?.firstOrNull()
                    ?: error("Recipe not found")
                val detail = meal.toRecipeDetail() ?: error("Recipe not found")
                val existing = cacheDao.getById(id)
                cacheDao.upsertAll(
                    listOf(
                        detail.toCacheEntity(
                            letterIndex = existing?.letterIndex
                                ?: letterIndexFromName(detail.name),
                        ),
                    ),
                )
                return detail
            } catch (_: Exception) {
                return cachedDetail(id)
                    ?: error("Unable to load recipe. You are offline.")
            }
        }

        return cachedDetail(id)
            ?: error("Unable to load recipe. You are offline.")
    }

    private suspend fun cachedDetail(id: String): RecipeDetail? {
        return cacheDao.getById(id)?.toRecipeDetailOrNull()
    }
}
