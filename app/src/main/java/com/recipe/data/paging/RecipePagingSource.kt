package com.recipe.data.paging

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.recipe.data.local.RecipeCacheDao
import com.recipe.data.local.toRecipe as toCachedRecipe
import com.recipe.data.network.NetworkMonitor
import com.recipe.data.remote.MealDto
import com.recipe.data.remote.TheMealDbApi
import com.recipe.data.remote.toCacheEntity
import com.recipe.data.remote.toRecipe as toRemoteRecipe
import com.recipe.domain.model.Recipe

class RecipePagingSource(
    private val api: TheMealDbApi,
    private val cacheDao: RecipeCacheDao,
    private val networkMonitor: NetworkMonitor,
    private val query: String,
) : PagingSource<Int, Recipe>() {

    override fun getRefreshKey(state: PagingState<Int, Recipe>): Int? {
        return state.anchorPosition?.let { anchor ->
            state.closestPageToPosition(anchor)?.prevKey?.plus(1)
                ?: state.closestPageToPosition(anchor)?.nextKey?.minus(1)
        }
    }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Recipe> {
        val cleanQuery = query.trim()
        if (!networkMonitor.currentlyOnline()) {
            return loadFromCache(params, cleanQuery)
        }

        return try {
            if (cleanQuery.isNotEmpty()) {
                val meals = api.searchMeals(cleanQuery).meals.orEmpty()
                cacheMeals(meals)
                val recipes = meals.mapNotNull { it.toRemoteRecipe() }.distinctBy { it.id }
                LoadResult.Page(
                    data = recipes,
                    prevKey = null,
                    nextKey = null,
                )
            } else {
                var letterIndex = params.key ?: 0
                var meals = emptyList<MealDto>()

                while (letterIndex <= LAST_LETTER_INDEX && meals.isEmpty()) {
                    val letter = ('a' + letterIndex).toString()
                    meals = api.mealsByFirstLetter(letter).meals.orEmpty()
                    if (meals.isEmpty()) {
                        letterIndex++
                    }
                }

                if (meals.isNotEmpty()) {
                    cacheMeals(meals, letterIndex)
                }

                val recipes = meals.mapNotNull { it.toRemoteRecipe() }.distinctBy { it.id }
                LoadResult.Page(
                    data = recipes,
                    prevKey = null,
                    nextKey = if (letterIndex < LAST_LETTER_INDEX) letterIndex + 1 else null,
                )
            }
        } catch (_: Exception) {
            val cached = loadFromCache(params, cleanQuery)
            if (cached is LoadResult.Page && cached.data.isNotEmpty()) {
                cached
            } else {
                LoadResult.Error(IllegalStateException("Unable to load recipes. You are offline."))
            }
        }
    }

    private suspend fun loadFromCache(
        params: LoadParams<Int>,
        cleanQuery: String,
    ): LoadResult<Int, Recipe> {
        if (cleanQuery.isNotEmpty()) {
            val recipes = cacheDao.search(cleanQuery).map { it.toCachedRecipe() }
            return LoadResult.Page(
                data = recipes,
                prevKey = null,
                nextKey = null,
            )
        }

        var letterIndex = params.key ?: 0
        var recipes = emptyList<Recipe>()

        while (letterIndex <= LAST_LETTER_INDEX && recipes.isEmpty()) {
            recipes = cacheDao.getByLetter(letterIndex).map { it.toCachedRecipe() }
            if (recipes.isEmpty()) {
                letterIndex++
            }
        }

        if (recipes.isEmpty() && (params.key ?: 0) == 0) {
            recipes = cacheDao.getAll().map { it.toCachedRecipe() }
            return LoadResult.Page(
                data = recipes,
                prevKey = null,
                nextKey = null,
            )
        }

        return LoadResult.Page(
            data = recipes,
            prevKey = null,
            nextKey = if (letterIndex < LAST_LETTER_INDEX && recipes.isNotEmpty()) {
                letterIndex + 1
            } else {
                null
            },
        )
    }

    private suspend fun cacheMeals(meals: List<MealDto>, letterIndex: Int? = null) {
        val entities = meals.mapNotNull { meal ->
            meal.toCacheEntity(letterIndex)
        }
        if (entities.isNotEmpty()) {
            cacheDao.upsertAll(entities)
        }
    }

    private companion object {
        const val LAST_LETTER_INDEX = 25
    }
}
