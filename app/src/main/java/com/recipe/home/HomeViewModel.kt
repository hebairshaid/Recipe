package com.recipe.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.recipe.auth.di.AuthModule
import com.recipe.data.remote.NetworkModule
import com.recipe.data.repository.FavoritesRepository
import com.recipe.data.repository.RecipeRepository
import com.recipe.domain.model.Recipe
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
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
    private val favoritesRepository: FavoritesRepository,
) : ViewModel() {

    private val _homeState = MutableStateFlow(HomeUiState(isLoading = true))

    val uiState: StateFlow<HomeUiState> = combine(
        _homeState,
        favoritesRepository.favorites,
    ) { home, favorites ->
        home.copy(favoriteIds = favorites.map { it.id }.toSet())
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = HomeUiState(isLoading = true),
    )

    private var searchJob: Job? = null

    init {
        loadHome()
    }

    fun loadHome() {
        viewModelScope.launch {
            _homeState.update { it.copy(isLoading = true, errorMessage = null) }
            runCatching { repository.getHomeRecipes() }
                .onSuccess { recipes ->
                    _homeState.update {
                        it.copy(recipes = recipes, isLoading = false, errorMessage = null)
                    }
                }
                .onFailure { error ->
                    _homeState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = error.message ?: "Failed to load recipes",
                        )
                    }
                }
        }
    }

    fun onQueryChange(query: String) {
        _homeState.update { it.copy(query = query) }
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            delay(400)
            _homeState.update { it.copy(isLoading = true, errorMessage = null) }
            runCatching { repository.searchRecipes(query) }
                .onSuccess { recipes ->
                    _homeState.update {
                        it.copy(recipes = recipes, isLoading = false, errorMessage = null)
                    }
                }
                .onFailure { error ->
                    _homeState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = error.message ?: "Search failed",
                        )
                    }
                }
        }
    }

    fun toggleFavorite(recipe: Recipe) {
        viewModelScope.launch {
            favoritesRepository.toggle(recipe)
        }
    }

    companion object {
        fun factory(): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return HomeViewModel(
                    repository = RecipeRepository(NetworkModule.api),
                    favoritesRepository = AuthModule.get().favoritesRepository,
                ) as T
            }
        }
    }
}
