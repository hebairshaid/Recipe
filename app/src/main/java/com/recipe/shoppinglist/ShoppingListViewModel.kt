package com.recipe.shoppinglist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.recipe.auth.di.AuthModule
import com.recipe.data.repository.ShoppingListRepository
import com.recipe.domain.model.ShoppingListItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ShoppingListUiState(
    val items: List<ShoppingListItem> = emptyList(),
    val draftText: String = "",
    val editingItemId: Long? = null,
    val editingText: String = "",
    val errorMessage: String? = null,
)

class ShoppingListViewModel(
    private val repository: ShoppingListRepository,
) : ViewModel() {

    private val _formState = MutableStateFlow(ShoppingListUiState())

    val uiState: StateFlow<ShoppingListUiState> = combine(
        repository.items,
        _formState,
    ) { items, form ->
        form.copy(items = items)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = ShoppingListUiState(),
    )

    fun onDraftChange(value: String) {
        _formState.update { it.copy(draftText = value, errorMessage = null) }
    }

    fun addItem() {
        val text = _formState.value.draftText.trim()
        if (text.isBlank()) {
            _formState.update { it.copy(errorMessage = "Write an ingredient first") }
            return
        }
        viewModelScope.launch {
            repository.add(text)
            _formState.update { it.copy(draftText = "", errorMessage = null) }
        }
    }

    fun startEditing(item: ShoppingListItem) {
        _formState.update {
            it.copy(
                editingItemId = item.id,
                editingText = item.name,
                errorMessage = null,
            )
        }
    }

    fun onEditingTextChange(value: String) {
        _formState.update { it.copy(editingText = value, errorMessage = null) }
    }

    fun saveEditing() {
        val state = _formState.value
        val id = state.editingItemId ?: return
        val text = state.editingText.trim()
        if (text.isBlank()) {
            _formState.update { it.copy(errorMessage = "Ingredient cannot be empty") }
            return
        }
        viewModelScope.launch {
            repository.update(id, text)
            _formState.update {
                it.copy(editingItemId = null, editingText = "", errorMessage = null)
            }
        }
    }

    fun cancelEditing() {
        _formState.update {
            it.copy(editingItemId = null, editingText = "", errorMessage = null)
        }
    }

    fun toggleDone(item: ShoppingListItem) {
        viewModelScope.launch {
            repository.toggleDone(item.id, !item.isDone)
        }
    }

    fun deleteItem(itemId: Long) {
        viewModelScope.launch {
            repository.delete(itemId)
            if (_formState.value.editingItemId == itemId) {
                cancelEditing()
            }
        }
    }

    companion object {
        fun factory(): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return ShoppingListViewModel(AuthModule.get().shoppingListRepository) as T
            }
        }
    }
}
