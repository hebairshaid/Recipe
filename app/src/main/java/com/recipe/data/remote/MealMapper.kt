package com.recipe.data.remote

import com.recipe.domain.model.Ingredient
import com.recipe.domain.model.Recipe
import com.recipe.domain.model.RecipeDetail

fun MealDto.toRecipe(): Recipe? {
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

fun MealDto.toRecipeDetail(): RecipeDetail? {
    val id = idMeal?.takeIf { it.isNotBlank() } ?: return null
    val name = strMeal?.takeIf { it.isNotBlank() } ?: return null
    return RecipeDetail(
        id = id,
        name = name,
        imageUrl = strMealThumb.orEmpty(),
        category = strCategory.orEmpty(),
        area = strArea.orEmpty(),
        ingredients = ingredients().map { (ingredient, measure) ->
            Ingredient(name = ingredient, measure = measure)
        },
        instructions = strInstructions?.trim().orEmpty(),
    )
}
