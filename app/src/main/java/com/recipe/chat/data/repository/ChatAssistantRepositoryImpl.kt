package com.recipe.chat.data.repository

import com.recipe.chat.domain.repository.ChatAssistantRepository
import com.recipe.data.local.RecipeCacheDao
import com.recipe.data.local.toRecipe
import com.recipe.data.network.NetworkMonitor
import com.recipe.data.remote.TheMealDbApi
import com.recipe.data.remote.toRecipe
import com.recipe.domain.model.Recipe
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ChatAssistantRepositoryImpl @Inject constructor(
    private val api: TheMealDbApi,
    private val cacheDao: RecipeCacheDao,
    private val networkMonitor: NetworkMonitor,
) : ChatAssistantRepository {

    override suspend fun findRecipesByIngredients(ingredients: List<String>): List<Recipe> {
        if (ingredients.isEmpty()) return emptyList()

        return if (networkMonitor.currentlyOnline()) {
            searchOnlineByIngredients(ingredients)
        } else {
            searchOfflineByIngredients(ingredients)
        }
    }

    override suspend fun suggestRecipes(keyword: String?): List<Recipe> {
        return if (networkMonitor.currentlyOnline()) {
            suggestOnline(keyword)
        } else {
            suggestOffline(keyword)
        }
    }

    private suspend fun suggestOnline(keyword: String?): List<Recipe> {
        val queries = if (keyword.isNullOrBlank()) {
            listOf("chicken", "pasta", "salad", "soup", "beef")
        } else {
            listOf(keyword)
        }

        val results = mutableListOf<Recipe>()
        for (query in queries) {
            runCatching {
                api.searchMeals(query).meals.orEmpty()
                    .mapNotNull { it.toRecipe() }
                    .forEach { recipe ->
                        if (results.none { it.id == recipe.id }) {
                            results.add(recipe)
                        }
                    }
            }
            if (keyword != null && results.size >= 8) break
        }

        return results.take(8)
    }

    private suspend fun suggestOffline(keyword: String?): List<Recipe> {
        val cached = cacheDao.getAll().map { it.toRecipe() }
        if (keyword.isNullOrBlank()) return cached.take(8)
        return cached.filter { recipe ->
            val haystack = "${recipe.name} ${recipe.category}".lowercase()
            keyword in haystack
        }.take(8).ifEmpty { cached.take(8) }
    }

    private suspend fun searchOnlineByIngredients(ingredients: List<String>): List<Recipe> {
        val primary = ingredients.first()
        val results = mutableListOf<Recipe>()

        runCatching {
            api.searchMeals(primary).meals.orEmpty()
                .mapNotNull { it.toRecipe() }
                .let { results.addAll(it) }
        }

        if (results.size < 6 && ingredients.size > 1) {
            ingredients.drop(1).forEach { ingredient ->
                runCatching {
                    api.searchMeals(ingredient).meals.orEmpty()
                        .mapNotNull { it.toRecipe() }
                        .forEach { recipe ->
                            if (results.none { it.id == recipe.id }) {
                                results.add(recipe)
                            }
                        }
                }
            }
        }

        return rankByIngredients(results, ingredients).take(8)
    }

    private suspend fun searchOfflineByIngredients(ingredients: List<String>): List<Recipe> {
        val cached = cacheDao.getAll().map { it.toRecipe() }
        return rankByIngredients(cached, ingredients).take(8)
    }

    private fun rankByIngredients(recipes: List<Recipe>, ingredients: List<String>): List<Recipe> {
        return recipes
            .map { recipe ->
                val haystack = "${recipe.name} ${recipe.category}".lowercase()
                val score = ingredients.count { ingredient -> ingredient in haystack }
                recipe to score
            }
            .sortedByDescending { it.second }
            .map { it.first }
    }
}
