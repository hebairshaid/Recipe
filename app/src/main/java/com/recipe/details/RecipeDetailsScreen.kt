package com.recipe.details

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Public
import androidx.compose.material.icons.outlined.WifiOff
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.Restaurant
import androidx.compose.material.icons.rounded.Spa
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.recipe.domain.model.Ingredient
import com.recipe.domain.model.RecipeDetail
import com.recipe.ui.components.RecipeImage
import com.recipe.ui.theme.CreamBackground
import com.recipe.ui.theme.CreamBackgroundDeep
import com.recipe.ui.theme.ForestGreen
import com.recipe.ui.theme.SageGreen
import com.recipe.ui.theme.Terracotta
import kotlinx.coroutines.delay

@Composable
fun RecipeDetailsScreen(
    recipeId: String,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: RecipeDetailsViewModel = viewModel(
        key = recipeId,
        factory = RecipeDetailsViewModel.factory(recipeId),
    ),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(CreamBackground),
    ) {
        when {
            state.isLoading && state.detail == null -> {
                CircularProgressIndicator(
                    color = Terracotta,
                    modifier = Modifier.align(Alignment.Center),
                )
            }

            state.errorMessage != null && state.detail == null -> {
                Column(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(
                        text = if (state.isOffline) {
                            "You are offline"
                        } else {
                            state.errorMessage ?: "Something went wrong"
                        },
                        color = ForestGreen,
                        fontWeight = FontWeight.SemiBold,
                    )
                    TextButton(onClick = viewModel::loadDetail) {
                        Text("Retry", color = Terracotta)
                    }
                }
            }

            state.detail != null -> {
                RecipeDetailsContent(
                    detail = state.detail!!,
                    isFavorite = state.isFavorite,
                    isOffline = state.isOffline,
                    onBack = onBack,
                    onFavoriteClick = viewModel::toggleFavorite,
                )
            }
        }

        if (state.detail == null) {
            BackButton(
                onBack = onBack,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .windowInsetsPadding(WindowInsets.statusBars)
                    .padding(12.dp),
            )
        }
    }
}

@Composable
private fun RecipeDetailsContent(
    detail: RecipeDetail,
    isFavorite: Boolean,
    isOffline: Boolean,
    onBack: () -> Unit,
    onFavoriteClick: () -> Unit,
) {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(detail.id) {
        delay(40)
        visible = true
    }

    val steps = remember(detail.instructions) { parseSteps(detail.instructions) }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
        ) {
            // Hero
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(340.dp),
            ) {
                RecipeImage(
                    imageUrl = detail.imageUrl,
                    contentDescription = detail.name,
                    modifier = Modifier.fillMaxSize(),
                )
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colorStops = arrayOf(
                                    0.0f to Color.Black.copy(alpha = 0.18f),
                                    0.45f to Color.Transparent,
                                    0.78f to CreamBackground.copy(alpha = 0.55f),
                                    1.0f to CreamBackground,
                                ),
                            ),
                        ),
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .windowInsetsPadding(WindowInsets.statusBars)
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    BackButton(onBack = onBack)
                    FavoriteButton(
                        isFavorite = isFavorite,
                        onClick = onFavoriteClick,
                    )
                }
            }

            AnimatedVisibility(
                visible = visible,
                enter = fadeIn() + slideInVertically { it / 8 },
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .offset(y = (-28).dp)
                        .clip(RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
                        .background(CreamBackground)
                        .padding(horizontal = 22.dp)
                        .padding(top = 8.dp, bottom = 36.dp),
                ) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.CenterHorizontally)
                            .padding(bottom = 16.dp)
                            .size(width = 42.dp, height = 4.dp)
                            .clip(RoundedCornerShape(50))
                            .background(ForestGreen.copy(alpha = 0.15f)),
                    )

                    if (isOffline) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 14.dp)
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

                    Text(
                        text = detail.name,
                        color = ForestGreen,
                        fontSize = 30.sp,
                        fontWeight = FontWeight.SemiBold,
                        fontFamily = FontFamily.Serif,
                        lineHeight = 36.sp,
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        if (detail.category.isNotBlank()) {
                            MetaBadge(
                                icon = Icons.Rounded.Restaurant,
                                text = detail.category,
                                modifier = Modifier.weight(1f, fill = false),
                            )
                        }
                        if (detail.area.isNotBlank()) {
                            MetaBadge(
                                icon = Icons.Outlined.Public,
                                text = detail.area,
                                modifier = Modifier.weight(1f, fill = false),
                            )
                        }
                        MetaBadge(
                            icon = Icons.Rounded.Spa,
                            text = "${detail.ingredients.size} items",
                            modifier = Modifier.weight(1f, fill = false),
                        )
                    }

                    Spacer(modifier = Modifier.height(28.dp))

                    SectionHeader(
                        title = "Ingredients",
                        subtitle = "Everything you need for this dish",
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(22.dp))
                            .background(CreamBackgroundDeep)
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                    ) {
                        detail.ingredients.forEachIndexed { index, ingredient ->
                            IngredientRow(ingredient = ingredient)
                            if (index != detail.ingredients.lastIndex) {
                                HorizontalDivider(
                                    color = ForestGreen.copy(alpha = 0.08f),
                                    thickness = 1.dp,
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(30.dp))

                    SectionHeader(
                        title = "Method",
                        subtitle = if (steps.isNotEmpty()) {
                            "${steps.size} steps to cook"
                        } else {
                            "Follow the instructions below"
                        },
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    if (steps.isEmpty()) {
                        Text(
                            text = "No instructions available.",
                            color = ForestGreen.copy(alpha = 0.65f),
                            fontSize = 15.sp,
                        )
                    } else {
                        steps.forEachIndexed { index, step ->
                            MethodStepCard(
                                number = index + 1,
                                text = step,
                                isLast = index == steps.lastIndex,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun BackButton(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    IconButton(
        onClick = onBack,
        modifier = modifier
            .shadow(6.dp, CircleShape, clip = false)
            .background(Color.White.copy(alpha = 0.94f), CircleShape)
            .size(42.dp),
    ) {
        Icon(
            imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
            contentDescription = "Back",
            tint = ForestGreen,
        )
    }
}

@Composable
private fun FavoriteButton(
    isFavorite: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    IconButton(
        onClick = onClick,
        modifier = modifier
            .shadow(6.dp, CircleShape, clip = false)
            .background(Color.White.copy(alpha = 0.94f), CircleShape)
            .size(42.dp),
    ) {
        Icon(
            imageVector = if (isFavorite) Icons.Rounded.Favorite else Icons.Outlined.FavoriteBorder,
            contentDescription = if (isFavorite) "Remove favorite" else "Add favorite",
            tint = Terracotta,
        )
    }
}

@Composable
private fun SectionHeader(
    title: String,
    subtitle: String,
) {
    Column {
        Text(
            text = title,
            color = Terracotta,
            fontSize = 22.sp,
            fontWeight = FontWeight.SemiBold,
            fontFamily = FontFamily.Serif,
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = subtitle,
            color = ForestGreen.copy(alpha = 0.55f),
            fontSize = 13.sp,
        )
    }
}

@Composable
private fun MetaBadge(
    icon: ImageVector,
    text: String,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(Color.White.copy(alpha = 0.75f))
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = SageGreen,
            modifier = Modifier.size(16.dp),
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = text,
            color = ForestGreen.copy(alpha = 0.85f),
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            maxLines = 1,
        )
    }
}

@Composable
private fun IngredientRow(ingredient: Ingredient) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .clip(CircleShape)
                .background(SageGreen),
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = ingredient.name.replaceFirstChar { it.uppercase() },
            color = ForestGreen,
            fontSize = 15.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.weight(1f),
        )
        if (ingredient.measure.isNotBlank()) {
            Text(
                text = ingredient.measure,
                color = Terracotta,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color.White.copy(alpha = 0.7f))
                    .padding(horizontal = 10.dp, vertical = 5.dp),
            )
        }
    }
}

@Composable
private fun MethodStepCard(
    number: Int,
    text: String,
    isLast: Boolean,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = if (isLast) 0.dp else 12.dp),
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(CircleShape)
                    .background(Terracotta),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = number.toString(),
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                )
            }
            if (!isLast) {
                Box(
                    modifier = Modifier
                        .padding(vertical = 4.dp)
                        .width(2.dp)
                        .height(28.dp)
                        .background(Terracotta.copy(alpha = 0.25f)),
                )
            }
        }

        Spacer(modifier = Modifier.width(14.dp))

        Column(
            modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(18.dp))
                .background(Color.White.copy(alpha = 0.72f))
                .padding(horizontal = 16.dp, vertical = 14.dp),
        ) {
            Text(
                text = "Step $number",
                color = Terracotta,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = text,
                color = ForestGreen.copy(alpha = 0.9f),
                fontSize = 15.sp,
                lineHeight = 22.sp,
            )
        }
    }
}

private fun parseSteps(instructions: String): List<String> {
    if (instructions.isBlank()) return emptyList()

    val byLine = instructions
        .split(Regex("\\r?\\n+"))
        .map { it.trim() }
        .filter { it.isNotBlank() }
        .map { cleanStepText(it) }
        .filter { it.isNotBlank() }

    if (byLine.size > 1) return byLine

    // Fallback: split long single-block instructions into sentences
    val sentences = instructions
        .split(Regex("(?<=[.!?])\\s+"))
        .map { it.trim() }
        .filter { it.length > 8 }

    return if (sentences.size > 1) sentences else listOf(instructions.trim())
}

private fun cleanStepText(step: String): String {
    return step
        .replace(Regex("^(STEP\\s*\\d+[:.\\-)\\s]*)", RegexOption.IGNORE_CASE), "")
        .replace(Regex("^\\d+[:.\\-)\\s]+"), "")
        .replace(Regex("^[-•]\\s*"), "")
        .trim()
}
