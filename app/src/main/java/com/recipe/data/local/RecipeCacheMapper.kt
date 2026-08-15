package com.recipe.data.local

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.recipe.domain.model.Ingredient
import com.recipe.domain.model.Recipe
import com.recipe.domain.model.RecipeDetail

private val gson = Gson()
private val ingredientListType = object : TypeToken<List<Ingredient>>() {}.type

fun Recipe.toCacheEntity(letterIndex: Int): RecipeCacheEntity {
    return RecipeCacheEntity(
        id = id,
        name = name,
        imageUrl = imageUrl,
        category = category,
        area = area,
        ingredientCount = ingredientCount,
        letterIndex = letterIndex,
    )
}

fun RecipeCacheEntity.toRecipe(): Recipe {
    return Recipe(
        id = id,
        name = name,
        imageUrl = imageUrl,
        category = category,
        area = area,
        ingredientCount = ingredientCount,
    )
}

fun RecipeDetail.toCacheEntity(letterIndex: Int = letterIndexFromName(name)): RecipeCacheEntity {
    return RecipeCacheEntity(
        id = id,
        name = name,
        imageUrl = imageUrl,
        category = category,
        area = area,
        ingredientCount = ingredients.size,
        letterIndex = letterIndex,
        instructions = instructions,
        ingredientsJson = gson.toJson(ingredients),
    )
}

fun RecipeCacheEntity.toRecipeDetailOrNull(): RecipeDetail? {
    if (instructions.isBlank() && ingredientsJson == "[]") return null
    val ingredients: List<Ingredient> = runCatching {
        gson.fromJson<List<Ingredient>>(ingredientsJson, ingredientListType)
    }.getOrDefault(emptyList())
    return RecipeDetail(
        id = id,
        name = name,
        imageUrl = imageUrl,
        category = category,
        area = area,
        ingredients = ingredients,
        instructions = instructions,
    )
}

fun letterIndexFromName(name: String): Int {
    val first = name.trim().firstOrNull()?.lowercaseChar() ?: return -1
    return if (first in 'a'..'z') first - 'a' else -1
}
