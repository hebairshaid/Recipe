package com.recipe.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.recipe.auth.di.AuthModule
import com.recipe.data.remote.NetworkModule
import com.recipe.data.repository.FavoritesRepository
import com.recipe.data.repository.RecipeRepository
import com.recipe.domain.model.Recipe
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class HomeUiState(
    val query: String = "",
    val favoriteIds: Set<String> = emptySet(),
)

@OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
class HomeViewModel(
    private val repository: RecipeRepository,
    private val favoritesRepository: FavoritesRepository,
) : ViewModel() {

    private val _query = MutableStateFlow("")

    val uiState: StateFlow<HomeUiState> = combine(
        _query,
        favoritesRepository.favorites,
    ) { query, favorites ->
        HomeUiState(
            query = query,
            favoriteIds = favorites.map { it.id }.toSet(),
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = HomeUiState(),
    )

    val recipes: Flow<PagingData<Recipe>> = _query
        .debounce(400)
        .flatMapLatest { query ->
            repository.getPagedRecipes(query)
        }
        .cachedIn(viewModelScope)

    fun onQueryChange(value: String) {
        _query.update { value }
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
