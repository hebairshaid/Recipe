package com.recipe.data.repository

import com.recipe.domain.model.Recipe
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

object FavoritesRepository {
    private val _favorites = MutableStateFlow<List<Recipe>>(emptyList())
    val favorites: StateFlow<List<Recipe>> = _favorites.asStateFlow()

    fun isFavorite(recipeId: String): Boolean =
        _favorites.value.any { it.id == recipeId }

    fun favoriteIds(): Set<String> =
        _favorites.value.map { it.id }.toSet()

    fun toggle(recipe: Recipe) {
        _favorites.update { current ->
            if (current.any { it.id == recipe.id }) {
                current.filterNot { it.id == recipe.id }
            } else {
                listOf(recipe) + current
            }
        }
    }

    fun remove(recipeId: String) {
        _favorites.update { current -> current.filterNot { it.id == recipeId } }
    }
}
