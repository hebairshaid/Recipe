package com.recipe.data.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.recipe.data.paging.RecipePagingSource
import com.recipe.data.remote.TheMealDbApi
import com.recipe.data.remote.toRecipeDetail
import com.recipe.domain.model.Recipe
import com.recipe.domain.model.RecipeDetail
import kotlinx.coroutines.flow.Flow

class RecipeRepository(
    private val api: TheMealDbApi,
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
                    query = query,
                )
            },
        ).flow
    }

    suspend fun getRecipeDetail(id: String): RecipeDetail {
        val meal = api.mealById(id).meals?.firstOrNull()
            ?: error("Recipe not found")
        return meal.toRecipeDetail()
            ?: error("Recipe not found")
    }
}
