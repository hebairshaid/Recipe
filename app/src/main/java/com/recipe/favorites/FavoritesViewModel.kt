package com.recipe.favorites

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.recipe.auth.di.AuthModule
import com.recipe.data.repository.FavoritesRepository
import com.recipe.domain.model.Recipe
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class FavoritesUiState(
    val recipes: List<Recipe> = emptyList(),
)

class FavoritesViewModel(
    private val favoritesRepository: FavoritesRepository,
) : ViewModel() {

    val uiState: StateFlow<FavoritesUiState> = favoritesRepository.favorites
        .map { FavoritesUiState(recipes = it) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = FavoritesUiState(),
        )

    fun removeFavorite(recipeId: String) {
        viewModelScope.launch {
            favoritesRepository.remove(recipeId)
        }
    }

    companion object {
        fun factory(): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return FavoritesViewModel(AuthModule.get().favoritesRepository) as T
            }
        }
    }
}
