package com.recipe.details

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.recipe.data.network.NetworkMonitor
import com.recipe.data.repository.FavoritesRepository
import com.recipe.data.repository.RecipeRepository
import com.recipe.domain.model.Recipe
import com.recipe.domain.model.RecipeDetail
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class RecipeDetailsUiState(
    val detail: RecipeDetail? = null,
    val isFavorite: Boolean = false,
    val isLoading: Boolean = true,
    val isOffline: Boolean = false,
    val errorMessage: String? = null,
)

@HiltViewModel(assistedFactory = RecipeDetailsViewModel.Factory::class)
class RecipeDetailsViewModel @AssistedInject constructor(
    @Assisted private val recipeId: String,
    private val repository: RecipeRepository,
    private val favoritesRepository: FavoritesRepository,
    networkMonitor: NetworkMonitor,
) : ViewModel() {

    private val _detailState = MutableStateFlow(RecipeDetailsUiState())

    val uiState: StateFlow<RecipeDetailsUiState> = combine(
        _detailState,
        favoritesRepository.favorites,
        networkMonitor.isOnline.map { online -> !online },
    ) { detailState, favorites, isOffline ->
        detailState.copy(
            isFavorite = favorites.any { it.id == recipeId },
            isOffline = isOffline,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = RecipeDetailsUiState(isOffline = !networkMonitor.currentlyOnline()),
    )

    init {
        loadDetail()
    }

    fun loadDetail() {
        viewModelScope.launch {
            _detailState.update { it.copy(isLoading = true, errorMessage = null) }
            runCatching { repository.getRecipeDetail(recipeId) }
                .onSuccess { detail ->
                    _detailState.update {
                        it.copy(detail = detail, isLoading = false, errorMessage = null)
                    }
                }
                .onFailure { error ->
                    _detailState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = error.message ?: "Failed to load recipe",
                        )
                    }
                }
        }
    }

    fun toggleFavorite() {
        val detail = _detailState.value.detail ?: return
        viewModelScope.launch {
            favoritesRepository.toggle(
                Recipe(
                    id = detail.id,
                    name = detail.name,
                    imageUrl = detail.imageUrl,
                    category = detail.category,
                    area = detail.area,
                    ingredientCount = detail.ingredients.size,
                ),
            )
        }
    }

    @AssistedFactory
    interface Factory {
        fun create(recipeId: String): RecipeDetailsViewModel
    }
}
