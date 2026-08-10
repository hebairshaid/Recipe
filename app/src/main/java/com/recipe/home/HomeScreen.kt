package com.recipe.home

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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Logout
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.WifiOff
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.Spa
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemKey
import com.recipe.domain.model.Recipe
import com.recipe.ui.components.AnimatedLogo
import com.recipe.ui.components.RecipeImage
import com.recipe.ui.theme.CreamBackground
import com.recipe.ui.theme.ForestGreen
import com.recipe.ui.theme.SageGreen
import com.recipe.ui.theme.Terracotta

@Composable
fun HomeScreen(
    onRecipeClick: (String) -> Unit,
    onLogout: () -> Unit = {},
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = viewModel(factory = HomeViewModel.factory()),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val recipes = viewModel.recipes.collectAsLazyPagingItems()

    LaunchedEffect(state.loggedOut) {
        if (state.loggedOut) onLogout()
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(CreamBackground)
            .windowInsetsPadding(WindowInsets.statusBars)
            .padding(horizontal = 20.dp),
    ) {
        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = viewModel::logout,
                    modifier = Modifier.size(40.dp),
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Outlined.Logout,
                        contentDescription = "Logout",
                        tint = Terracotta,
                        modifier = Modifier.scale(scaleX = -1f, scaleY = 1f),
                    )
                }
                Text(
                    text = "Recipe",
                    color = Terracotta,
                    fontSize = 34.sp,
                    fontWeight = FontWeight.SemiBold,
                    fontFamily = FontFamily.Serif,
                )
            }
            AnimatedLogo(size = 56.dp)
        }

        Spacer(modifier = Modifier.height(14.dp))

        OutlinedTextField(
            value = state.query,
            onValueChange = viewModel::onQueryChange,
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            placeholder = {
                Text(
                    text = "Search recipes or ingredients...",
                    color = ForestGreen.copy(alpha = 0.45f),
                    fontSize = 14.sp,
                )
            },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Outlined.Search,
                    contentDescription = null,
                    tint = ForestGreen.copy(alpha = 0.55f),
                )
            },
            shape = RoundedCornerShape(18.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = Color.White.copy(alpha = 0.85f),
                unfocusedContainerColor = Color.White.copy(alpha = 0.7f),
                focusedBorderColor = SageGreen.copy(alpha = 0.5f),
                unfocusedBorderColor = ForestGreen.copy(alpha = 0.12f),
                focusedTextColor = ForestGreen,
                unfocusedTextColor = ForestGreen,
                cursorColor = Terracotta,
            ),
        )

        if (state.isOffline) {
            Spacer(modifier = Modifier.height(12.dp))
            OfflineBanner()
        }

        Spacer(modifier = Modifier.height(18.dp))

        RecipeGrid(
            recipes = recipes,
            favoriteIds = state.favoriteIds,
            isOffline = state.isOffline,
            onFavoriteClick = viewModel::toggleFavorite,
            onRecipeClick = onRecipeClick,
        )
    }
}

@Composable
private fun OfflineBanner(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(Terracotta.copy(alpha = 0.12f))
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = Icons.Outlined.WifiOff,
            contentDescription = null,
            tint = Terracotta,
            modifier = Modifier.size(18.dp),
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = "You are offline",
            color = Terracotta,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
        )
    }
}

@Composable
private fun RecipeGrid(
    recipes: LazyPagingItems<Recipe>,
    favoriteIds: Set<String>,
    isOffline: Boolean,
    onFavoriteClick: (Recipe) -> Unit,
    onRecipeClick: (String) -> Unit,
) {
    val refresh = recipes.loadState.refresh
    val append = recipes.loadState.append

    when {
        refresh is LoadState.Loading && recipes.itemCount == 0 -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator(color = Terracotta)
            }
        }

        refresh is LoadState.Error && recipes.itemCount == 0 -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = if (isOffline) {
                            "You are offline"
                        } else {
                            refresh.error.message ?: "Something went wrong"
                        },
                        color = ForestGreen,
                        fontWeight = FontWeight.SemiBold,
                    )
                    TextButton(onClick = { recipes.retry() }) {
                        Text("Retry", color = Terracotta)
                    }
                }
            }
        }

        refresh is LoadState.NotLoading && recipes.itemCount == 0 -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = if (isOffline) "You are offline" else "No recipes found",
                    color = ForestGreen.copy(alpha = 0.7f),
                    fontFamily = FontFamily.Serif,
                    fontSize = 18.sp,
                )
            }
        }

        else -> {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(bottom = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                verticalArrangement = Arrangement.spacedBy(18.dp),
                modifier = Modifier.fillMaxSize(),
            ) {
                items(
                    count = recipes.itemCount,
                    key = recipes.itemKey { it.id },
                ) { index ->
                    val recipe = recipes[index] ?: return@items
                    RecipeCard(
                        recipe = recipe,
                        isFavorite = recipe.id in favoriteIds,
                        onFavoriteClick = { onFavoriteClick(recipe) },
                        onClick = { onRecipeClick(recipe.id) },
                    )
                }

                when (append) {
                    is LoadState.Loading -> {
                        item(span = { GridItemSpan(maxLineSpan) }) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 16.dp),
                                contentAlignment = Alignment.Center,
                            ) {
                                CircularProgressIndicator(
                                    color = Terracotta,
                                    modifier = Modifier.size(28.dp),
                                    strokeWidth = 3.dp,
                                )
                            }
                        }
                    }

                    is LoadState.Error -> {
                        item(span = { GridItemSpan(maxLineSpan) }) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 12.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                            ) {
                                Text(
                                    text = "Couldn't load more",
                                    color = ForestGreen.copy(alpha = 0.75f),
                                    fontSize = 13.sp,
                                )
                                TextButton(onClick = { recipes.retry() }) {
                                    Text("Retry", color = Terracotta)
                                }
                            }
                        }
                    }

                    else -> Unit
                }
            }
        }
    }
}

@Composable
private fun RecipeCard(
    recipe: Recipe,
    isFavorite: Boolean,
    onFavoriteClick: () -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
    ) {
        RecipeImage(
            imageUrl = recipe.imageUrl,
            contentDescription = recipe.name,
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .clip(RoundedCornerShape(18.dp)),
        )

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Top,
        ) {
            Text(
                text = recipe.name,
                color = ForestGreen,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                fontFamily = FontFamily.Serif,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f),
                lineHeight = 18.sp,
            )
            IconButton(
                onClick = onFavoriteClick,
                modifier = Modifier.size(28.dp),
            ) {
                Icon(
                    imageVector = if (isFavorite) Icons.Rounded.Favorite else Icons.Outlined.FavoriteBorder,
                    contentDescription = "Favorite",
                    tint = if (isFavorite) Terracotta else Terracotta.copy(alpha = 0.85f),
                    modifier = Modifier.size(18.dp),
                )
            }
        }

        Spacer(modifier = Modifier.height(2.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Rounded.Spa,
                contentDescription = null,
                tint = SageGreen,
                modifier = Modifier.size(14.dp),
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = if (recipe.ingredientCount > 0) {
                    "${recipe.ingredientCount} ingredients"
                } else {
                    recipe.category.ifBlank { "Recipe" }
                },
                color = ForestGreen.copy(alpha = 0.65f),
                fontSize = 12.sp,
            )
        }
    }
}
