package com.levelchef.android.ui.nav

import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavController
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.levelchef.android.R
import com.levelchef.android.ui.showcase.DesignSystemShowcaseScreen
import com.levelchef.core.designsystem.LevelChefBottomNavigationBar
import com.levelchef.core.designsystem.LevelChefNavItem
import com.levelchef.core.designsystem.LevelChefTopAppBarHome
import com.levelchef.core.designsystem.LevelChefTopAppBarInner
import com.levelchef.core.ui.theme.LevelChefTheme
import com.levelchef.feature.cookinglog.CookingLogScreen
import com.levelchef.feature.home.HomeRoute
import com.levelchef.feature.onboarding.OnboardingGate
import com.levelchef.feature.recipedetail.RecipeDetailScreen
import com.levelchef.feature.trophyroom.TrophyRoomScreen

private val bottomNavItems = listOf(
    LevelChefDestination.Home,
    LevelChefDestination.Recipes,
    LevelChefDestination.Trophies,
    LevelChefDestination.Log,
)

private const val RECIPE_DETAIL_ROUTE = "recipeDetail"

/** Hidden route for [DesignSystemShowcaseScreen] — not in [bottomNavItems], reachable only by
 * tapping the Home nav item 5x quickly (see [HomeTapCounter] below). */
private const val DESIGN_SYSTEM_SHOWCASE_ROUTE = "designSystemShowcase"

/** Taps on the Home nav item within [RAPID_TAP_WINDOW_MS] of each other count toward the streak;
 * a slower tap (or any tap on another item) resets it. Reaching [TAPS_TO_OPEN_SHOWCASE] opens the
 * hidden showcase instead of navigating to Home. */
private const val RAPID_TAP_WINDOW_MS = 600L
private const val TAPS_TO_OPEN_SHOWCASE = 5

/** Mutable streak counter for rapid taps on the Home nav item. Not Compose snapshot state — it
 * only feeds click handling and never drives recomposition. */
private class HomeTapCounter {
    private var tapCount = 0
    private var lastTapTime = 0L

    /** Registers a tap on the Home nav item and returns true when the streak reaches
     * [TAPS_TO_OPEN_SHOWCASE], meaning the hidden showcase should open instead of navigating Home. */
    fun registerHomeTap(now: Long = System.currentTimeMillis()): Boolean {
        tapCount = if (now - lastTapTime < RAPID_TAP_WINDOW_MS) tapCount + 1 else 1
        lastTapTime = now
        return (tapCount >= TAPS_TO_OPEN_SHOWCASE).also { if (it) tapCount = 0 }
    }

    fun reset() {
        tapCount = 0
    }
}

/** Handles a bottom-nav item tap: opens the hidden design-system showcase on a 5x rapid Home-tap
 * streak, otherwise navigates to [dest] as a single-top, state-restoring destination. */
private fun onNavItemClick(
    dest: LevelChefDestination,
    navController: NavController,
    homeTapCounter: HomeTapCounter,
) {
    if (dest == LevelChefDestination.Home) {
        if (homeTapCounter.registerHomeTap()) {
            navController.navigate(DESIGN_SYSTEM_SHOWCASE_ROUTE)
            return
        }
    } else {
        homeTapCounter.reset()
    }
    navController.navigate(dest.route) {
        popUpTo(navController.graph.findStartDestination().id) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
}

@Composable
fun LevelChefApp() {
    OnboardingGate {
        LevelChefAppContent()
    }
}

@Composable
private fun LevelChefAppContent() {
    val navController = rememberNavController()
    val homeTapCounter = remember { HomeTapCounter() }
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    val currentRoute = currentDestination?.route

    Scaffold(
        containerColor = LevelChefTheme.colors.background,
        topBar = {
            when (currentRoute) {
                // The showcase renders its own inner top bar; anything before the first
                // destination resolves has no bar yet.
                DESIGN_SYSTEM_SHOWCASE_ROUTE, null -> Unit
                RECIPE_DETAIL_ROUTE -> LevelChefTopAppBarInner(
                    title = stringResource(R.string.nav_recipe_detail),
                    onBackClick = { navController.popBackStack() },
                    modifier = Modifier.statusBarsPadding(),
                )
                else -> LevelChefTopAppBarHome(
                    title = topBarTitleFor(currentRoute),
                    modifier = Modifier.statusBarsPadding(),
                )
            }
        },
        bottomBar = {
            if (currentRoute != DESIGN_SYSTEM_SHOWCASE_ROUTE) {
                LevelChefBottomNavigationBar(
                    modifier = Modifier.navigationBarsPadding(),
                    items = bottomNavItems.map { dest ->
                        LevelChefNavItem(
                            icon = iconFor(dest),
                            label = stringResource(dest.labelRes),
                            selected = currentDestination?.hierarchy?.any { it.route == dest.route } == true,
                            onClick = { onNavItemClick(dest, navController, homeTapCounter) },
                        )
                    },
                )
            }
        },
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = LevelChefDestination.Home.route,
            modifier = Modifier.padding(innerPadding),
        ) {
            composable(LevelChefDestination.Home.route) {
                HomeRoute(onRecipeClick = { navController.navigate(RECIPE_DETAIL_ROUTE) })
            }
            composable(LevelChefDestination.Recipes.route) { RecipeDetailScreen() }
            composable(LevelChefDestination.Trophies.route) { TrophyRoomScreen() }
            composable(LevelChefDestination.Log.route) { CookingLogScreen() }
            composable(RECIPE_DETAIL_ROUTE) { RecipeDetailScreen() }
            composable(DESIGN_SYSTEM_SHOWCASE_ROUTE) {
                DesignSystemShowcaseScreen(onBackClick = { navController.popBackStack() })
            }
        }
    }
}

@Composable
private fun topBarTitleFor(route: String): String = when (route) {
    LevelChefDestination.Recipes.route -> stringResource(LevelChefDestination.Recipes.labelRes)
    LevelChefDestination.Trophies.route -> stringResource(LevelChefDestination.Trophies.labelRes)
    LevelChefDestination.Log.route -> stringResource(LevelChefDestination.Log.labelRes)
    else -> "LevelChef"
}

private fun iconFor(dest: LevelChefDestination) = when (dest) {
    LevelChefDestination.Home -> Icons.Filled.Home
    LevelChefDestination.Recipes -> Icons.Filled.List
    LevelChefDestination.Trophies -> Icons.Filled.Star
    LevelChefDestination.Log -> Icons.Filled.List
}
