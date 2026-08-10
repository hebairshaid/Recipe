package com.recipe.data.remote

import com.google.gson.Gson
import com.recipe.data.local.RecipeCacheEntity
import com.recipe.data.local.letterIndexFromName
import com.recipe.domain.model.Ingredient
import com.recipe.domain.model.Recipe
import com.recipe.domain.model.RecipeDetail

private val gson = Gson()

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

fun MealDto.toCacheEntity(letterIndex: Int? = null): RecipeCacheEntity? {
    val detail = toRecipeDetail() ?: return null
    val ingredients = detail.ingredients
    return RecipeCacheEntity(
        id = detail.id,
        name = detail.name,
        imageUrl = detail.imageUrl,
        category = detail.category,
        area = detail.area,
        ingredientCount = ingredients.size,
        letterIndex = letterIndex ?: letterIndexFromName(detail.name),
        instructions = detail.instructions,
        ingredientsJson = gson.toJson(ingredients),
    )
}
