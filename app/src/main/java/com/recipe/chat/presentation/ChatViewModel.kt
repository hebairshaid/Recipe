package com.recipe.chat.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.recipe.chat.domain.model.ChatMessage
import com.recipe.chat.domain.model.ChatRole
import com.recipe.chat.domain.usecase.SendChatMessageUseCase
import com.recipe.data.network.NetworkMonitor
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

data class ChatUiState(
    val messages: List<ChatMessage> = emptyList(),
    val input: String = "",
    val isProcessing: Boolean = false,
    val isOffline: Boolean = false,
)

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val sendChatMessage: SendChatMessageUseCase,
    networkMonitor: NetworkMonitor,
) : ViewModel() {

    private val _state = MutableStateFlow(ChatUiState())

    val uiState: StateFlow<ChatUiState> = kotlinx.coroutines.flow.combine(
        _state,
        networkMonitor.isOnline.map { online -> !online },
    ) { state, isOffline ->
        state.copy(isOffline = isOffline)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = ChatUiState(isOffline = !networkMonitor.currentlyOnline()),
    )

    init {
        _state.update {
            it.copy(
                messages = listOf(
                    ChatMessage(
                        id = UUID.randomUUID().toString(),
                        role = ChatRole.ASSISTANT,
                        text = "Hi! I'm your recipe assistant.\n\n• List ingredients: chicken, rice, tomato\n• Or ask: suggest a recipe / give me pasta ideas",
                    ),
                ),
            )
        }
    }

    fun onInputChange(value: String) {
        _state.update { it.copy(input = value) }
    }

    fun sendMessage() {
        val text = _state.value.input.trim()
        if (text.isBlank() || _state.value.isProcessing) return

        val userMessage = ChatMessage(
            id = UUID.randomUUID().toString(),
            role = ChatRole.USER,
            text = text,
        )
        val loadingId = UUID.randomUUID().toString()
        val loadingMessage = ChatMessage(
            id = loadingId,
            role = ChatRole.ASSISTANT,
            text = "Finding recipes for you…",
            isLoading = true,
        )

        _state.update {
            it.copy(
                input = "",
                isProcessing = true,
                messages = it.messages + userMessage + loadingMessage,
            )
        }

        viewModelScope.launch {
            runCatching { sendChatMessage(text) }
                .onSuccess { reply ->
                    _state.update { state ->
                        val withoutLoading = state.messages.filterNot { it.id == loadingId }
                        state.copy(
                            isProcessing = false,
                            messages = withoutLoading + ChatMessage(
                                id = UUID.randomUUID().toString(),
                                role = ChatRole.ASSISTANT,
                                text = reply.message,
                                recipes = reply.recipes,
                                detectedIngredients = reply.ingredients,
                            ),
                        )
                    }
                }
                .onFailure { error ->
                    _state.update { state ->
                        val withoutLoading = state.messages.filterNot { it.id == loadingId }
                        state.copy(
                            isProcessing = false,
                            messages = withoutLoading + ChatMessage(
                                id = UUID.randomUUID().toString(),
                                role = ChatRole.ASSISTANT,
                                text = error.message ?: "Something went wrong. Please try again.",
                            ),
                        )
                    }
                }
        }
    }
}
