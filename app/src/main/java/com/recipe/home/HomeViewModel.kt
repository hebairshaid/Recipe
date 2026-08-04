package com.recipe.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.recipe.data.remote.NetworkModule
import com.recipe.data.repository.RecipeRepository
import com.recipe.domain.model.Recipe
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class HomeUiState(
    val query: String = "",
    val recipes: List<Recipe> = emptyList(),
    val favoriteIds: Set<String> = emptySet(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
)

class HomeViewModel(
    private val repository: RecipeRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState(isLoading = true))
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private var searchJob: Job? = null

    init {
        loadHome()
    }

    fun loadHome() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            runCatching { repository.getHomeRecipes() }
                .onSuccess { recipes ->
                    _uiState.update {
                        it.copy(recipes = recipes, isLoading = false, errorMessage = null)
                    }
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = error.message ?: "Failed to load recipes",
                        )
                    }
                }
        }
    }

    fun onQueryChange(query: String) {
        _uiState.update { it.copy(query = query) }
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            delay(400)
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            runCatching { repository.searchRecipes(query) }
                .onSuccess { recipes ->
                    _uiState.update {
                        it.copy(recipes = recipes, isLoading = false, errorMessage = null)
                    }
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = error.message ?: "Search failed",
                        )
                    }
                }
        }
    }

    fun toggleFavorite(recipeId: String) {
        _uiState.update { state ->
            val next = state.favoriteIds.toMutableSet()
            if (!next.add(recipeId)) next.remove(recipeId)
            state.copy(favoriteIds = next)
        }
    }

    companion object {
        fun factory(): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                val repository = RecipeRepository(NetworkModule.api)
                return HomeViewModel(repository) as T
            }
        }
    }
}
