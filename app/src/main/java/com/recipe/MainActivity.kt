package com.recipe

import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import com.recipe.auth.presentation.AuthNavHost
import com.recipe.navigation.MainShell
import com.recipe.session.SessionCheckViewModel
import com.recipe.splash.SplashScreen
import com.recipe.ui.theme.CreamBackground
import com.recipe.ui.theme.RecipeTheme
import com.recipe.ui.theme.Terracotta
import dagger.hilt.android.AndroidEntryPoint

private enum class AppDestination {
    Splash,
    CheckingSession,
    Auth,
    Main,
}

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val cream = Color.parseColor("#FAF7F2")
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.light(
                scrim = cream,
                darkScrim = cream,
            ),
            navigationBarStyle = SystemBarStyle.light(
                scrim = cream,
                darkScrim = cream,
            ),
        )

        setContent {
            RecipeTheme(dynamicColor = false) {
                RecipeApp()
            }
        }
    }
}

@Composable
private fun RecipeApp() {
    var destination by remember { mutableStateOf(AppDestination.Splash) }

    when (destination) {
        AppDestination.Splash -> {
            SplashScreen(
                onFinished = { destination = AppDestination.CheckingSession },
            )
        }

        AppDestination.CheckingSession -> {
            SessionCheckRoute(
                onSessionValid = { destination = AppDestination.Main },
                onSessionInvalid = { destination = AppDestination.Auth },
            )
        }

        AppDestination.Auth -> {
            AuthNavHost(
                onAuthSuccess = { destination = AppDestination.Main },
            )
        }

        AppDestination.Main -> {
            MainShell(
                onLogout = { destination = AppDestination.Auth },
            )
        }
    }
}

@Composable
private fun SessionCheckRoute(
    onSessionValid: () -> Unit,
    onSessionInvalid: () -> Unit,
    viewModel: SessionCheckViewModel = hiltViewModel(),
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(CreamBackground),
        contentAlignment = Alignment.Center,
    ) {
        CircularProgressIndicator(color = Terracotta)
    }
    LaunchedEffect(Unit) {
        if (viewModel.validateSession()) {
            onSessionValid()
        } else {
            onSessionInvalid()
        }
    }
}
