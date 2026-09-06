package com.levelchef.android.ui.nav

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavController
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.levelchef.android.ui.showcase.DesignSystemShowcaseScreen
import com.levelchef.core.designsystem.LevelChefBottomNavigationBar
import com.levelchef.core.designsystem.LevelChefNavItem
import com.levelchef.core.ui.theme.LevelChefTheme
import com.levelchef.feature.home.HomeRoute
import com.levelchef.feature.ingredients.IngredientDetailRoute
import com.levelchef.feature.ingredients.IngredientFormRoute
import com.levelchef.feature.ingredients.IngredientsListRoute
import com.levelchef.feature.onboarding.OnboardingGate
import com.levelchef.feature.recipedetail.RecipeDetailRoute
import com.levelchef.feature.recipedetail.RecipesScreen
import com.levelchef.feature.settings.SettingsRoute
import com.levelchef.feature.trophyroom.TrophyRoomScreen

/** The top-level destinations shown in the bottom navigation bar. The bar is hidden on every other
 * route (recipe detail, the hidden showcase). Each screen renders its own top app bar. */
private val bottomNavItems = listOf(
    LevelChefDestination.Home,
    LevelChefDestination.Recipes,
    LevelChefDestination.Trophies,
)

private const val SETTINGS_ROUTE = "settings"

private const val RECIPE_ID_ARG = "recipeId"
private const val RECIPE_DETAIL_ROUTE = "recipeDetail/{$RECIPE_ID_ARG}"
private fun recipeDetailPath(id: String) = "recipeDetail/$id"

private const val INGREDIENTS_ROUTE = "ingredients"
private const val INGREDIENT_ID_ARG = "ingredientId"
private const val INGREDIENT_DETAIL_ROUTE = "ingredients/{$INGREDIENT_ID_ARG}"
private const val INGREDIENT_FORM_ROUTE = "ingredientForm?$INGREDIENT_ID_ARG={$INGREDIENT_ID_ARG}"

private fun ingredientDetailPath(id: String) = "ingredients/$id"
private fun ingredientFormPath(id: String? = null) =
    if (id == null) "ingredientForm" else "ingredientForm?$INGREDIENT_ID_ARG=$id"

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
    val showBottomBar = currentRoute in bottomNavItems.map { it.route }

    Scaffold(
        containerColor = LevelChefTheme.colors.background,
        // Each screen owns its top app bar and applies its own status-bar padding, so the Scaffold
        // must not inject a top inset into innerPadding.
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        bottomBar = {
            if (showBottomBar) {
                LevelChefBottomNavigationBar(
                    modifier = Modifier.navigationBarsPadding(),
                    items = bottomNavItems.map { dest ->
                        LevelChefNavItem(
                            icon = dest.icon,
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
                HomeRoute(
                    onRecipeClick = { rec -> navController.navigate(recipeDetailPath(rec.id)) },
                    onSettingsClick = { navController.navigate(SETTINGS_ROUTE) },
                    onIngredientsClick = { navController.navigate(INGREDIENTS_ROUTE) },
                )
            }
            composable(LevelChefDestination.Recipes.route) { RecipesScreen() }
            composable(LevelChefDestination.Trophies.route) { TrophyRoomScreen() }
            composable(
                RECIPE_DETAIL_ROUTE,
                arguments = listOf(navArgument(RECIPE_ID_ARG) { type = NavType.StringType }),
            ) { entry ->
                RecipeDetailRoute(
                    recipeId = entry.arguments?.getString(RECIPE_ID_ARG).orEmpty(),
                    onBackClick = { navController.popBackStack() },
                    onSettingsClick = { navController.navigate(SETTINGS_ROUTE) },
                )
            }
            composable(SETTINGS_ROUTE) {
                SettingsRoute(onBackClick = { navController.popBackStack() })
            }
            composable(INGREDIENTS_ROUTE) {
                IngredientsListRoute(
                    onBackClick = { navController.popBackStack() },
                    onIngredientClick = { id -> navController.navigate(ingredientDetailPath(id)) },
                    onAddClick = { navController.navigate(ingredientFormPath()) },
                )
            }
            composable(
                INGREDIENT_DETAIL_ROUTE,
                arguments = listOf(navArgument(INGREDIENT_ID_ARG) { type = NavType.StringType }),
            ) { entry ->
                IngredientDetailRoute(
                    ingredientId = entry.arguments?.getString(INGREDIENT_ID_ARG).orEmpty(),
                    onBackClick = { navController.popBackStack() },
                    onEditClick = { id -> navController.navigate(ingredientFormPath(id)) },
                    onDeleted = { navController.popBackStack() },
                )
            }
            composable(
                INGREDIENT_FORM_ROUTE,
                arguments = listOf(
                    navArgument(INGREDIENT_ID_ARG) {
                        type = NavType.StringType
                        nullable = true
                        defaultValue = null
                    },
                ),
            ) { entry ->
                IngredientFormRoute(
                    ingredientId = entry.arguments?.getString(INGREDIENT_ID_ARG),
                    onBackClick = { navController.popBackStack() },
                    onSaved = { navController.popBackStack() },
                )
            }
            composable(DESIGN_SYSTEM_SHOWCASE_ROUTE) {
                DesignSystemShowcaseScreen(onBackClick = { navController.popBackStack() })
            }
        }
    }
}
