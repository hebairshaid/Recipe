package com.recipe.details

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.recipe.auth.di.AuthModule
import com.recipe.data.remote.NetworkModule
import com.recipe.data.repository.FavoritesRepository
import com.recipe.data.repository.RecipeRepository
import com.recipe.domain.model.Recipe
import com.recipe.domain.model.RecipeDetail
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class RecipeDetailsUiState(
    val detail: RecipeDetail? = null,
    val isFavorite: Boolean = false,
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
)

class RecipeDetailsViewModel(
    private val recipeId: String,
    private val repository: RecipeRepository,
    private val favoritesRepository: FavoritesRepository,
) : ViewModel() {

    private val _detailState = MutableStateFlow(RecipeDetailsUiState())

    val uiState: StateFlow<RecipeDetailsUiState> = combine(
        _detailState,
        favoritesRepository.favorites,
    ) { detailState, favorites ->
        detailState.copy(isFavorite = favorites.any { it.id == recipeId })
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = RecipeDetailsUiState(),
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

    companion object {
        fun factory(recipeId: String): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return RecipeDetailsViewModel(
                        recipeId = recipeId,
                        repository = RecipeRepository(NetworkModule.api),
                        favoritesRepository = AuthModule.get().favoritesRepository,
                    ) as T
                }
            }
    }
}
