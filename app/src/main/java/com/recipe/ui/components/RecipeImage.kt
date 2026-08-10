package com.recipe.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import coil.compose.SubcomposeAsyncImage
import com.recipe.R
import com.recipe.ui.theme.CreamBackgroundDeep
import com.recipe.ui.theme.Terracotta

@Composable
fun RecipeImage(
    imageUrl: String,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Crop,
) {
    SubcomposeAsyncImage(
        model = imageUrl.takeIf { it.isNotBlank() },
        contentDescription = contentDescription,
        contentScale = contentScale,
        modifier = modifier.background(CreamBackgroundDeep),
        loading = {
            ImagePlaceholder(showLoading = true)
        },
        error = {
            ImagePlaceholder(showLoading = false)
        },
    )
}

@Composable
private fun ImagePlaceholder(
    showLoading: Boolean,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Image(
            painter = painterResource(R.drawable.ic_image_placeholder),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize(),
        )
        if (showLoading) {
            CircularProgressIndicator(
                color = Terracotta,
                modifier = Modifier.size(28.dp),
                strokeWidth = 2.5.dp,
            )
        }
    }
}
