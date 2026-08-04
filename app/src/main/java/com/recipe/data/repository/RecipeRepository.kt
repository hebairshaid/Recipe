package com.recipe.data.repository

import com.recipe.data.remote.MealDto
import com.recipe.data.remote.TheMealDbApi
import com.recipe.domain.model.Recipe

class RecipeRepository(
    private val api: TheMealDbApi,
) {
    suspend fun getHomeRecipes(): List<Recipe> {
        val letters = listOf("a", "b", "c", "s")
        return letters
            .flatMap { letter -> api.mealsByFirstLetter(letter).meals.orEmpty() }
            .mapNotNull { it.toRecipe() }
            .distinctBy { it.id }
            .shuffled()
    }

    suspend fun searchRecipes(query: String): List<Recipe> {
        if (query.isBlank()) return getHomeRecipes()
        return api.searchMeals(query.trim()).meals.orEmpty().mapNotNull { it.toRecipe() }
    }

    private fun MealDto.toRecipe(): Recipe? {
        val id = idMeal?.takeIf { it.isNotBlank() } ?: return null
        val name = strMeal?.takeIf { it.isNotBlank() } ?: return null
        return Recipe(
            id = id,
            name = name,
            imageUrl = strMealThumb.orEmpty(),
            category = strCategory.orEmpty(),
            area = strArea.orEmpty(),
            ingredientCount = ingredientCount(),
        )
    }
}
