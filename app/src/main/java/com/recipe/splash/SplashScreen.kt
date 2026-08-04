package com.recipe.splash

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition
import com.recipe.R
import com.recipe.ui.theme.CreamBackground
import com.recipe.ui.theme.CreamBackgroundDeep
import com.recipe.ui.theme.RecipeTheme
import com.recipe.ui.theme.Terracotta
import java.util.concurrent.atomic.AtomicBoolean
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    onFinished: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val composition by rememberLottieComposition(
        LottieCompositionSpec.RawRes(R.raw.food_animation)
    )
    val progress by animateLottieCompositionAsState(
        composition = composition,
        iterations = 1,
        speed = 1f,
    )

    var showTitle by remember { mutableStateOf(false) }
    val didFinish = remember { AtomicBoolean(false) }
    val titleAlpha by animateFloatAsState(
        targetValue = if (showTitle) 1f else 0f,
        animationSpec = tween(durationMillis = 500),
        label = "splashTitleAlpha",
    )

    fun finishOnce() {
        if (didFinish.compareAndSet(false, true)) {
            onFinished()
        }
    }

    LaunchedEffect(Unit) {
        delay(300)
        showTitle = true
    }

    LaunchedEffect(progress, composition) {
        if (composition != null && progress >= 1f) {
            delay(400)
            finishOnce()
        }
    }

    // Fallback if composition fails to load
    LaunchedEffect(Unit) {
        delay(3500)
        finishOnce()
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(CreamBackground, CreamBackgroundDeep)
                )
            ),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            LottieAnimation(
                composition = composition,
                progress = { progress },
                modifier = Modifier.size(220.dp),
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Recipe",
                color = Terracotta.copy(alpha = titleAlpha),
                fontSize = 40.sp,
                fontWeight = FontWeight.SemiBold,
                fontFamily = FontFamily.Serif,
                letterSpacing = 0.5.sp,
                modifier = Modifier.graphicsLayer { alpha = titleAlpha },
            )
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun SplashScreenPreview() {
    RecipeTheme {
        SplashScreen(onFinished = {})
    }
}
