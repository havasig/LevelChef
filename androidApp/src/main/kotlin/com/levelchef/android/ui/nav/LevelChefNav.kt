package com.levelchef.android.ui.nav

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.compose.composable
import com.levelchef.core.ui.theme.AccentPrimary
import com.levelchef.core.ui.theme.BackgroundPrimary
import com.levelchef.core.ui.theme.BackgroundSurface
import com.levelchef.core.ui.theme.TextPrimary
import com.levelchef.core.ui.theme.TextSecondary
import com.levelchef.feature.cookinglog.CookingLogScreen
import com.levelchef.feature.home.HomeRoute
import com.levelchef.feature.recipedetail.RecipeDetailScreen
import com.levelchef.feature.trophyroom.TrophyRoomScreen

sealed class LevelChefDestination(val route: String, val label: String) {
    data object Home : LevelChefDestination("home", "Home")
    data object Recipes : LevelChefDestination("recipes", "Recipes")
    data object Trophies : LevelChefDestination("trophies", "Trophies")
    data object Log : LevelChefDestination("log", "Log")
}

private val bottomNavItems = listOf(
    LevelChefDestination.Home,
    LevelChefDestination.Recipes,
    LevelChefDestination.Trophies,
    LevelChefDestination.Log,
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LevelChefApp() {
    val navController = rememberNavController()

    Scaffold(
        containerColor = BackgroundPrimary,
        topBar = {
            TopAppBar(
                title = { Text("LevelChef", color = TextPrimary, fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = BackgroundPrimary),
            )
        },
        bottomBar = {
            val navBackStackEntry by navController.currentBackStackEntryAsState()
            val currentDestination = navBackStackEntry?.destination

            NavigationBar(containerColor = BackgroundSurface) {
                bottomNavItems.forEach { dest ->
                    val selected = currentDestination?.hierarchy?.any { it.route == dest.route } == true
                    NavigationBarItem(
                        selected = selected,
                        onClick = {
                            navController.navigate(dest.route) {
                                popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = { Icon(iconFor(dest), contentDescription = dest.label) },
                        label = { Text(dest.label) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = AccentPrimary,
                            selectedTextColor = AccentPrimary,
                            unselectedIconColor = TextSecondary,
                            unselectedTextColor = TextSecondary,
                            indicatorColor = BackgroundSurface,
                        ),
                    )
                }
            }
        },
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = LevelChefDestination.Home.route,
            modifier = Modifier.padding(innerPadding),
        ) {
            composable(LevelChefDestination.Home.route) {
                HomeRoute(onRecipeClick = { navController.navigate("recipeDetail") })
            }
            composable(LevelChefDestination.Recipes.route) { RecipeDetailScreen() }
            composable(LevelChefDestination.Trophies.route) { TrophyRoomScreen() }
            composable(LevelChefDestination.Log.route) { CookingLogScreen() }
            composable("recipeDetail") { RecipeDetailScreen() }
        }
    }
}

private fun iconFor(dest: LevelChefDestination) = when (dest) {
    LevelChefDestination.Home -> Icons.Filled.Home
    LevelChefDestination.Recipes -> Icons.Filled.List
    LevelChefDestination.Trophies -> Icons.Filled.Star
    LevelChefDestination.Log -> Icons.Filled.List
}
