package com.recipe.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.PersonOutline
import androidx.compose.material.icons.outlined.ShoppingCart
import androidx.compose.material.icons.rounded.ChatBubble
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.ShoppingCart
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.recipe.details.RecipeDetailsScreen
import com.recipe.favorites.FavoritesScreen
import com.recipe.home.HomeScreen
import com.recipe.ui.theme.CreamBackground
import com.recipe.ui.theme.ForestGreen
import com.recipe.ui.theme.Terracotta

private data class BottomTab(
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
)

private object Routes {
    const val MAIN = "main"
    const val DETAILS = "recipe_details/{recipeId}"

    fun details(recipeId: String) = "recipe_details/$recipeId"
}

@Composable
fun MainShell() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Routes.MAIN,
        modifier = Modifier.fillMaxSize(),
    ) {
        composable(Routes.MAIN) {
            MainTabs(
                onRecipeClick = { recipeId ->
                    navController.navigate(Routes.details(recipeId))
                },
            )
        }
        composable(
            route = Routes.DETAILS,
            arguments = listOf(
                navArgument("recipeId") { type = NavType.StringType },
            ),
        ) { entry ->
            val recipeId = entry.arguments?.getString("recipeId").orEmpty()
            RecipeDetailsScreen(
                recipeId = recipeId,
                onBack = { navController.popBackStack() },
                modifier = Modifier.fillMaxSize(),
            )
        }
    }
}

@Composable
private fun MainTabs(
    onRecipeClick: (String) -> Unit,
) {
    val tabs = listOf(
        BottomTab("Home", Icons.Rounded.Home, Icons.Outlined.Home),
        BottomTab("Chat", Icons.Rounded.ChatBubble, Icons.Outlined.ChatBubbleOutline),
        BottomTab("List", Icons.Rounded.ShoppingCart, Icons.Outlined.ShoppingCart),
        BottomTab("Favorite", Icons.Rounded.Favorite, Icons.Outlined.FavoriteBorder),
        BottomTab("Profile", Icons.Rounded.Person, Icons.Outlined.PersonOutline),
    )
    var selectedIndex by rememberSaveable { mutableIntStateOf(0) }

    Scaffold(
        containerColor = CreamBackground,
        bottomBar = {
            NavigationBar(
                containerColor = Color.White.copy(alpha = 0.92f),
                contentColor = ForestGreen,
            ) {
                tabs.forEachIndexed { index, tab ->
                    val selected = selectedIndex == index
                    NavigationBarItem(
                        selected = selected,
                        onClick = { selectedIndex = index },
                        icon = {
                            Icon(
                                imageVector = if (selected) tab.selectedIcon else tab.unselectedIcon,
                                contentDescription = tab.label,
                            )
                        },
                        label = {
                            Text(
                                text = tab.label,
                                fontSize = 11.sp,
                                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Terracotta,
                            selectedTextColor = Terracotta,
                            unselectedIconColor = ForestGreen.copy(alpha = 0.55f),
                            unselectedTextColor = ForestGreen.copy(alpha = 0.55f),
                            indicatorColor = Terracotta.copy(alpha = 0.12f),
                        ),
                    )
                }
            }
        },
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
            when (selectedIndex) {
                0 -> HomeScreen(
                    onRecipeClick = onRecipeClick,
                    modifier = Modifier.fillMaxSize(),
                )
                1 -> PlaceholderTab(title = "Chat")
                2 -> PlaceholderTab(title = "Shopping List")
                3 -> FavoritesScreen(
                    onRecipeClick = onRecipeClick,
                    modifier = Modifier.fillMaxSize(),
                )
                4 -> PlaceholderTab(title = "Profile")
            }
        }
    }
}

@Composable
private fun PlaceholderTab(title: String) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(CreamBackground),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = title,
            color = ForestGreen,
            fontSize = 26.sp,
            fontWeight = FontWeight.Medium,
            fontFamily = FontFamily.Serif,
        )
    }
}
