package com.levelchef.shared.nav

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.savedstate.read
import com.levelchef.core.designsystem.LevelChefBottomNavigationBar
import com.levelchef.core.designsystem.LevelChefNavItem
import com.levelchef.core.ui.theme.LevelChefTheme
import com.levelchef.feature.cookinglog.CookingLogRoute
import com.levelchef.feature.home.HomeRoute
import com.levelchef.feature.home.RecipeRecommendation
import com.levelchef.feature.ingredients.IngredientDetailRoute
import com.levelchef.feature.ingredients.IngredientFormRoute
import com.levelchef.feature.ingredients.IngredientsListRoute
import com.levelchef.feature.mealreview.MealReviewRoute
import com.levelchef.feature.recipedetail.RecipeDetailRoute
import com.levelchef.feature.settings.SettingsRoute
import com.levelchef.feature.trophyroom.TrophyRoomRoute
import org.jetbrains.compose.resources.stringResource

/** The top-level destinations shown in the bottom navigation bar — the bar is hidden on every
 * other route (recipe detail, meal review, settings, ingredients). Each screen renders its own top
 * app bar, same as Android. */
private val bottomNavItems = listOf(
    SharedDestination.Home,
    SharedDestination.Recipes,
    SharedDestination.Trophies,
)

private const val SETTINGS_ROUTE = "settings"

private const val RECIPE_ID_ARG = "recipeId"
private const val RECIPE_DETAIL_ROUTE = "recipeDetail/{$RECIPE_ID_ARG}"
private fun recipeDetailPath(id: String) = "recipeDetail/$id"

private const val MEAL_REVIEW_ROUTE = "mealReview/{$RECIPE_ID_ARG}"
private fun mealReviewPath(id: String) = "mealReview/$id"

private const val INGREDIENTS_ROUTE = "ingredients"
private const val INGREDIENT_ID_ARG = "ingredientId"
private const val INGREDIENT_DETAIL_ROUTE = "ingredients/{$INGREDIENT_ID_ARG}"
private const val INGREDIENT_FORM_ROUTE = "ingredientForm?$INGREDIENT_ID_ARG={$INGREDIENT_ID_ARG}"

private fun ingredientDetailPath(id: String) = "ingredients/$id"
private fun ingredientFormPath(id: String? = null) =
    if (id == null) "ingredientForm" else "ingredientForm?$INGREDIENT_ID_ARG=$id"

/**
 * The iOS counterpart of androidApp's `LevelChefAppContent` — same route graph (Home, Recipes,
 * Trophies, recipe detail, meal review, settings, the ingredients list/detail/form), minus two
 * things deliberately not ported: the `OnboardingGate` wrapper (`feature:onboarding` isn't KMP
 * yet, so iOS keeps showing Home directly, same as before this change) and the debug-only
 * design-system-showcase rapid-tap easter egg (`BuildConfig.DEBUG` has no iOS equivalent here and
 * it isn't a real user-facing flow).
 */
@Composable
fun SharedApp() {
    val navController = rememberNavController()
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
                            onClick = {
                                navController.navigate(dest.route) {
                                    popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                        )
                    },
                )
            }
        },
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = SharedDestination.Home.route,
            modifier = Modifier.padding(innerPadding),
        ) {
            composable(SharedDestination.Home.route) {
                val onRecipeSelected: (RecipeRecommendation) -> Unit =
                    { rec -> navController.navigate(recipeDetailPath(rec.id)) }
                HomeRoute(
                    onCookToday = onRecipeSelected,
                    onRecipeClick = onRecipeSelected,
                    onSettingsClick = { navController.navigate(SETTINGS_ROUTE) },
                    onIngredientsClick = { navController.navigate(INGREDIENTS_ROUTE) },
                )
            }
            composable(SharedDestination.Recipes.route) {
                CookingLogRoute(onRecipeClick = { id -> navController.navigate(recipeDetailPath(id)) })
            }
            composable(SharedDestination.Trophies.route) { TrophyRoomRoute() }
            composable(
                RECIPE_DETAIL_ROUTE,
                arguments = listOf(navArgument(RECIPE_ID_ARG) { type = NavType.StringType }),
            ) { entry ->
                val recipeId = entry.arguments?.read { getStringOrNull(RECIPE_ID_ARG) }.orEmpty()
                RecipeDetailRoute(
                    recipeId = recipeId,
                    onBackClick = { navController.popBackStack() },
                    onSettingsClick = { navController.navigate(SETTINGS_ROUTE) },
                    onMadeIt = { navController.navigate(mealReviewPath(recipeId)) },
                )
            }
            composable(
                MEAL_REVIEW_ROUTE,
                arguments = listOf(navArgument(RECIPE_ID_ARG) { type = NavType.StringType }),
            ) { entry ->
                MealReviewRoute(
                    recipeId = entry.arguments?.read { getStringOrNull(RECIPE_ID_ARG) }.orEmpty(),
                    onBackClick = { navController.popBackStack() },
                    onSaved = { navController.popBackStack(SharedDestination.Home.route, false) },
                )
            }
            composable(SETTINGS_ROUTE) {
                SettingsRoute(
                    onBackClick = { navController.popBackStack() },
                    showDeveloperOptions = false,
                )
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
                    ingredientId = entry.arguments?.read { getStringOrNull(INGREDIENT_ID_ARG) }.orEmpty(),
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
                    ingredientId = entry.arguments?.read { getStringOrNull(INGREDIENT_ID_ARG) },
                    onBackClick = { navController.popBackStack() },
                    onSaved = { navController.popBackStack() },
                )
            }
        }
    }
}
