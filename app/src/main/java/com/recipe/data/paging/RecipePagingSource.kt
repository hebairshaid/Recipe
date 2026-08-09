package com.recipe.data.paging

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.recipe.data.remote.TheMealDbApi
import com.recipe.data.remote.toRecipe
import com.recipe.domain.model.Recipe

class RecipePagingSource(
    private val api: TheMealDbApi,
    private val query: String,
) : PagingSource<Int, Recipe>() {

    override fun getRefreshKey(state: PagingState<Int, Recipe>): Int? {
        return state.anchorPosition?.let { anchor ->
            state.closestPageToPosition(anchor)?.prevKey?.plus(1)
                ?: state.closestPageToPosition(anchor)?.nextKey?.minus(1)
        }
    }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Recipe> {
        return try {
            val cleanQuery = query.trim()
            if (cleanQuery.isNotEmpty()) {
                val recipes = api.searchMeals(cleanQuery).meals
                    .orEmpty()
                    .mapNotNull { it.toRecipe() }
                    .distinctBy { it.id }
                return LoadResult.Page(
                    data = recipes,
                    prevKey = null,
                    nextKey = null,
                )
            }

            var letterIndex = params.key ?: 0
            var recipes = emptyList<Recipe>()

            while (letterIndex <= LAST_LETTER_INDEX && recipes.isEmpty()) {
                val letter = ('a' + letterIndex).toString()
                recipes = api.mealsByFirstLetter(letter).meals
                    .orEmpty()
                    .mapNotNull { it.toRecipe() }
                    .distinctBy { it.id }
                if (recipes.isEmpty()) {
                    letterIndex++
                }
            }

            LoadResult.Page(
                data = recipes,
                prevKey = null,
                nextKey = if (letterIndex < LAST_LETTER_INDEX) letterIndex + 1 else null,
            )
        } catch (error: Exception) {
            LoadResult.Error(error)
        }
    }

    private companion object {
        const val LAST_LETTER_INDEX = 25
    }
}
