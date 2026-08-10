package com.recipe.favorites

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.recipe.data.repository.FavoritesRepository
import com.recipe.domain.model.Recipe
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class FavoritesUiState(
    val recipes: List<Recipe> = emptyList(),
)

@HiltViewModel
class FavoritesViewModel @Inject constructor(
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
}
