package com.recipe.chat.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Send
import androidx.compose.material.icons.outlined.WifiOff
import androidx.compose.material.icons.rounded.SmartToy
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.recipe.chat.domain.model.ChatMessage
import com.recipe.chat.domain.model.ChatRole
import com.recipe.domain.model.Recipe
import com.recipe.ui.components.RecipeImage
import com.recipe.ui.theme.CreamBackground
import com.recipe.ui.theme.ForestGreen
import com.recipe.ui.theme.SageGreen
import com.recipe.ui.theme.Terracotta

@Composable
fun ChatScreen(
    onRecipeClick: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ChatViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val listState = rememberLazyListState()

    LaunchedEffect(state.messages.size) {
        if (state.messages.isNotEmpty()) {
            listState.animateScrollToItem(state.messages.lastIndex)
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(CreamBackground)
            .windowInsetsPadding(WindowInsets.statusBars)
            .imePadding(),
    ) {
        ChatHeader(isOffline = state.isOffline)

        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            items(state.messages, key = { it.id }) { message ->
                ChatBubble(
                    message = message,
                    onRecipeClick = onRecipeClick,
                )
            }
        }

        ChatInputBar(
            value = state.input,
            enabled = !state.isProcessing,
            onValueChange = viewModel::onInputChange,
            onSend = viewModel::sendMessage,
        )
    }
}

@Composable
private fun ChatHeader(isOffline: Boolean) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 12.dp),
    ) {
        Text(
            text = "Recipe Assistant",
            color = Terracotta,
            fontSize = 28.sp,
            fontWeight = FontWeight.SemiBold,
            fontFamily = FontFamily.Serif,
        )
        Text(
            text = "Powered by on-device AI + TheMealDB",
            color = ForestGreen.copy(alpha = 0.65f),
            fontSize = 13.sp,
        )
        if (isOffline) {
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(Terracotta.copy(alpha = 0.12f))
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    imageVector = Icons.Outlined.WifiOff,
                    contentDescription = null,
                    tint = Terracotta,
                    modifier = Modifier.size(16.dp),
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "You are offline",
                    color = Terracotta,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                )
            }
        }
    }
}

@Composable
private fun ChatBubble(
    message: ChatMessage,
    onRecipeClick: (String) -> Unit,
) {
    val isUser = message.role == ChatRole.USER
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start,
    ) {
        if (!isUser) {
            Icon(
                imageVector = Icons.Rounded.SmartToy,
                contentDescription = null,
                tint = SageGreen,
                modifier = Modifier
                    .size(28.dp)
                    .padding(top = 4.dp, end = 8.dp),
            )
        }

        Column(
            modifier = Modifier.widthIn(max = 320.dp),
            horizontalAlignment = if (isUser) Alignment.End else Alignment.Start,
        ) {
            Box(
                modifier = Modifier
                    .clip(
                        RoundedCornerShape(
                            topStart = 18.dp,
                            topEnd = 18.dp,
                            bottomStart = if (isUser) 18.dp else 4.dp,
                            bottomEnd = if (isUser) 4.dp else 18.dp,
                        ),
                    )
                    .background(if (isUser) Terracotta else Color.White.copy(alpha = 0.9f))
                    .padding(horizontal = 14.dp, vertical = 10.dp),
            ) {
                if (message.isLoading) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        CircularProgressIndicator(
                            color = if (isUser) Color.White else Terracotta,
                            modifier = Modifier.size(18.dp),
                            strokeWidth = 2.dp,
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = message.text,
                            color = if (isUser) Color.White else ForestGreen,
                            fontSize = 14.sp,
                        )
                    }
                } else {
                    Text(
                        text = message.text,
                        color = if (isUser) Color.White else ForestGreen,
                        fontSize = 14.sp,
                        lineHeight = 20.sp,
                    )
                }
            }

            if (message.detectedIngredients.isNotEmpty()) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Detected: ${message.detectedIngredients.joinToString(", ")}",
                    color = ForestGreen.copy(alpha = 0.7f),
                    fontSize = 12.sp,
                )
            }

            if (message.recipes.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                message.recipes.forEach { recipe ->
                    RecipeSuggestionCard(
                        recipe = recipe,
                        onClick = { onRecipeClick(recipe.id) },
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}

@Composable
private fun RecipeSuggestionCard(
    recipe: Recipe,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(Color.White.copy(alpha = 0.85f))
            .clickable(onClick = onClick)
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        RecipeImage(
            imageUrl = recipe.imageUrl,
            contentDescription = recipe.name,
            modifier = Modifier
                .size(64.dp)
                .clip(RoundedCornerShape(10.dp)),
        )
        Spacer(modifier = Modifier.width(10.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = recipe.name,
                color = ForestGreen,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = recipe.category.ifBlank { "Recipe" },
                color = ForestGreen.copy(alpha = 0.6f),
                fontSize = 12.sp,
            )
        }
    }
}

@Composable
private fun ChatInputBar(
    value: String,
    enabled: Boolean,
    onValueChange: (String) -> Unit,
    onSend: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White.copy(alpha = 0.92f))
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.weight(1f),
            enabled = enabled,
            singleLine = true,
            placeholder = {
                Text(
                    text = "Ingredients or: suggest a recipe…",
                    color = ForestGreen.copy(alpha = 0.45f),
                    fontSize = 14.sp,
                )
            },
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
            keyboardActions = KeyboardActions(onSend = { onSend() }),
            shape = RoundedCornerShape(18.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = CreamBackground,
                unfocusedContainerColor = CreamBackground,
                focusedBorderColor = SageGreen.copy(alpha = 0.5f),
                unfocusedBorderColor = ForestGreen.copy(alpha = 0.12f),
                focusedTextColor = ForestGreen,
                unfocusedTextColor = ForestGreen,
                cursorColor = Terracotta,
            ),
        )
        IconButton(
            onClick = onSend,
            enabled = enabled && value.isNotBlank(),
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Rounded.Send,
                contentDescription = "Send",
                tint = if (enabled && value.isNotBlank()) Terracotta else ForestGreen.copy(alpha = 0.35f),
            )
        }
    }
}
