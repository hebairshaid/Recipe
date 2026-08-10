package com.recipe.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.recipe.auth.di.AuthModule
import com.recipe.auth.domain.session.SessionRepository
import com.recipe.data.network.NetworkMonitor
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
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class HomeUiState(
    val query: String = "",
    val favoriteIds: Set<String> = emptySet(),
    val isOffline: Boolean = false,
    val loggedOut: Boolean = false,
)

@OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
class HomeViewModel(
    private val repository: RecipeRepository,
    private val favoritesRepository: FavoritesRepository,
    private val sessionRepository: SessionRepository,
    networkMonitor: NetworkMonitor,
) : ViewModel() {

    private val _query = MutableStateFlow("")
    private val _loggedOut = MutableStateFlow(false)

    val uiState: StateFlow<HomeUiState> = combine(
        _query,
        favoritesRepository.favorites,
        networkMonitor.isOnline.map { online -> !online },
        _loggedOut,
    ) { query, favorites, isOffline, loggedOut ->
        HomeUiState(
            query = query,
            favoriteIds = favorites.map { it.id }.toSet(),
            isOffline = isOffline,
            loggedOut = loggedOut,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = HomeUiState(isOffline = !networkMonitor.currentlyOnline()),
    )

    val recipes: Flow<PagingData<Recipe>> = combine(
        _query.debounce(400),
        networkMonitor.isOnline,
    ) { query, _ ->
        query
    }
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

    fun logout() {
        sessionRepository.clearToken()
        _loggedOut.value = true
    }

    companion object {
        fun factory(): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                val deps = AuthModule.get()
                return HomeViewModel(
                    repository = deps.recipeRepository,
                    favoritesRepository = deps.favoritesRepository,
                    sessionRepository = deps.sessionRepository,
                    networkMonitor = deps.networkMonitor,
                ) as T
            }
        }
    }
}
