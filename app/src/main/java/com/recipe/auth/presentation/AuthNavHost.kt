package com.recipe.auth.presentation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.recipe.auth.presentation.login.LoginScreen
import com.recipe.auth.presentation.signup.SignUpScreen

private object AuthRoutes {
    const val LOGIN = "login"
    const val SIGN_UP = "sign_up"
}

@Composable
fun AuthNavHost(
    onAuthSuccess: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = AuthRoutes.LOGIN,
        modifier = modifier,
    ) {
        composable(AuthRoutes.LOGIN) {
            LoginScreen(
                onLoginSuccess = onAuthSuccess,
                onNavigateToSignUp = {
                    navController.navigate(AuthRoutes.SIGN_UP)
                },
            )
        }
        composable(AuthRoutes.SIGN_UP) {
            SignUpScreen(
                onSignUpSuccess = onAuthSuccess,
                onNavigateToLogin = {
                    navController.popBackStack()
                },
            )
        }
    }
}
