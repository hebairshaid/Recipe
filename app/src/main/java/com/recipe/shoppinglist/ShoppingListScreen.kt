package com.recipe.shoppinglist

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.ShoppingCart
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.recipe.domain.model.ShoppingListItem
import com.recipe.ui.theme.CreamBackground
import com.recipe.ui.theme.CreamBackgroundDeep
import com.recipe.ui.theme.ForestGreen
import com.recipe.ui.theme.SageGreen
import com.recipe.ui.theme.Terracotta

@Composable
fun ShoppingListScreen(
    modifier: Modifier = Modifier,
    viewModel: ShoppingListViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(CreamBackground)
            .windowInsetsPadding(WindowInsets.statusBars)
            .padding(horizontal = 20.dp),
    ) {
        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Shopping List",
            color = Terracotta,
            fontSize = 34.sp,
            fontWeight = FontWeight.SemiBold,
            fontFamily = FontFamily.Serif,
        )

        Text(
            text = if (state.items.isEmpty()) {
                "Add ingredients you need to buy"
            } else {
                val pending = state.items.count { !it.isDone }
                "$pending left · ${state.items.size} total"
            },
            color = ForestGreen.copy(alpha = 0.55f),
            fontSize = 14.sp,
            modifier = Modifier.padding(top = 4.dp, bottom = 16.dp),
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            OutlinedTextField(
                value = state.draftText,
                onValueChange = viewModel::onDraftChange,
                modifier = Modifier.weight(1f),
                singleLine = true,
                placeholder = {
                    Text(
                        text = "Write an ingredient...",
                        color = ForestGreen.copy(alpha = 0.4f),
                    )
                },
                shape = RoundedCornerShape(16.dp),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = { viewModel.addItem() }),
                colors = listFieldColors(),
            )

            Spacer(modifier = Modifier.width(10.dp))

            FilledIconButton(
                onClick = viewModel::addItem,
                modifier = Modifier.size(52.dp),
                shape = RoundedCornerShape(16.dp),
                colors = IconButtonDefaults.filledIconButtonColors(
                    containerColor = Terracotta,
                    contentColor = CreamBackground,
                ),
            ) {
                Icon(
                    imageVector = Icons.Outlined.Add,
                    contentDescription = "Add ingredient",
                )
            }
        }

        if (state.errorMessage != null) {
            Text(
                text = state.errorMessage ?: "",
                color = Terracotta,
                fontSize = 13.sp,
                modifier = Modifier.padding(top = 8.dp),
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (state.items.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Outlined.ShoppingCart,
                        contentDescription = null,
                        tint = Terracotta.copy(alpha = 0.35f),
                        modifier = Modifier.size(56.dp),
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Your list is empty",
                        color = ForestGreen,
                        fontFamily = FontFamily.Serif,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Medium,
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Add milk, eggs, tomatoes...",
                        color = ForestGreen.copy(alpha = 0.55f),
                        fontSize = 14.sp,
                    )
                }
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(bottom = 20.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxSize(),
            ) {
                items(state.items, key = { it.id }) { item ->
                    ShoppingListRow(
                        item = item,
                        isEditing = state.editingItemId == item.id,
                        editingText = state.editingText,
                        onEditingTextChange = viewModel::onEditingTextChange,
                        onToggleDone = { viewModel.toggleDone(item) },
                        onStartEdit = { viewModel.startEditing(item) },
                        onSaveEdit = viewModel::saveEditing,
                        onCancelEdit = viewModel::cancelEditing,
                        onDelete = { viewModel.deleteItem(item.id) },
                    )
                }
            }
        }
    }
}

@Composable
private fun ShoppingListRow(
    item: ShoppingListItem,
    isEditing: Boolean,
    editingText: String,
    onEditingTextChange: (String) -> Unit,
    onToggleDone: () -> Unit,
    onStartEdit: () -> Unit,
    onSaveEdit: () -> Unit,
    onCancelEdit: () -> Unit,
    onDelete: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(CreamBackgroundDeep)
            .padding(horizontal = 10.dp, vertical = 8.dp),
    ) {
        if (isEditing) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                OutlinedTextField(
                    value = editingText,
                    onValueChange = onEditingTextChange,
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = { onSaveEdit() }),
                    colors = listFieldColors(),
                )
                IconButton(onClick = onSaveEdit) {
                    Icon(
                        imageVector = Icons.Outlined.Check,
                        contentDescription = "Save",
                        tint = SageGreen,
                    )
                }
                IconButton(onClick = onCancelEdit) {
                    Icon(
                        imageVector = Icons.Outlined.Close,
                        contentDescription = "Cancel",
                        tint = ForestGreen.copy(alpha = 0.65f),
                    )
                }
            }
        } else {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Checkbox(
                    checked = item.isDone,
                    onCheckedChange = { onToggleDone() },
                    colors = CheckboxDefaults.colors(
                        checkedColor = SageGreen,
                        uncheckedColor = ForestGreen.copy(alpha = 0.4f),
                        checkmarkColor = CreamBackground,
                    ),
                )

                Text(
                    text = item.name,
                    color = if (item.isDone) {
                        ForestGreen.copy(alpha = 0.45f)
                    } else {
                        ForestGreen
                    },
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    textDecoration = if (item.isDone) {
                        TextDecoration.LineThrough
                    } else {
                        TextDecoration.None
                    },
                    modifier = Modifier.weight(1f),
                )

                IconButton(onClick = onStartEdit) {
                    Icon(
                        imageVector = Icons.Outlined.Edit,
                        contentDescription = "Edit",
                        tint = Terracotta.copy(alpha = 0.85f),
                    )
                }
                IconButton(onClick = onDelete) {
                    Icon(
                        imageVector = Icons.Outlined.Delete,
                        contentDescription = "Delete",
                        tint = ForestGreen.copy(alpha = 0.55f),
                    )
                }
            }
        }
    }
}

@Composable
private fun listFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedContainerColor = Color.White.copy(alpha = 0.85f),
    unfocusedContainerColor = Color.White.copy(alpha = 0.7f),
    focusedBorderColor = SageGreen.copy(alpha = 0.55f),
    unfocusedBorderColor = ForestGreen.copy(alpha = 0.12f),
    focusedTextColor = ForestGreen,
    unfocusedTextColor = ForestGreen,
    cursorColor = Terracotta,
)
