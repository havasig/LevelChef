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
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.compose.composable
import com.levelchef.android.ui.showcase.DesignSystemShowcaseScreen
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

/** Hidden route for [DesignSystemShowcaseScreen] — not in [bottomNavItems], reachable only by
 * tapping the Home nav item 5x quickly (see [rapidTapsToOpenShowcase] below). */
private const val DESIGN_SYSTEM_SHOWCASE_ROUTE = "designSystemShowcase"

/** Taps on the Home nav item within [RAPID_TAP_WINDOW_MS] of each other count toward the streak;
 * a slower tap (or any tap on another item) resets it. Reaching [TAPS_TO_OPEN_SHOWCASE] opens the
 * hidden showcase instead of navigating to Home. */
private const val RAPID_TAP_WINDOW_MS = 600L
private const val TAPS_TO_OPEN_SHOWCASE = 5

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LevelChefApp() {
    val navController = rememberNavController()
    var homeTapCount by remember { mutableIntStateOf(0) }
    var lastHomeTapTime by remember { mutableLongStateOf(0L) }

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
                        onClick = onClick@{
                            var openShowcase = false
                            if (dest == LevelChefDestination.Home) {
                                val now = System.currentTimeMillis()
                                homeTapCount = if (now - lastHomeTapTime < RAPID_TAP_WINDOW_MS) homeTapCount + 1 else 1
                                lastHomeTapTime = now
                                if (homeTapCount >= TAPS_TO_OPEN_SHOWCASE) {
                                    homeTapCount = 0
                                    openShowcase = true
                                }
                            } else {
                                homeTapCount = 0
                            }
                            if (openShowcase) {
                                navController.navigate(DESIGN_SYSTEM_SHOWCASE_ROUTE)
                                return@onClick
                            }
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
            composable(DESIGN_SYSTEM_SHOWCASE_ROUTE) {
                DesignSystemShowcaseScreen(onBackClick = { navController.popBackStack() })
            }
        }
    }
}

private fun iconFor(dest: LevelChefDestination) = when (dest) {
    LevelChefDestination.Home -> Icons.Filled.Home
    LevelChefDestination.Recipes -> Icons.Filled.List
    LevelChefDestination.Trophies -> Icons.Filled.Star
    LevelChefDestination.Log -> Icons.Filled.List
}
