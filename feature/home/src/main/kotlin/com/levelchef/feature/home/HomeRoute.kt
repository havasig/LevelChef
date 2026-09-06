package com.levelchef.feature.home

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import org.koin.compose.viewmodel.koinViewModel

/** Stateful entry point: collects [HomeViewModel]'s state and hands it to the stateless [HomeScreen]. */
@Composable
fun HomeRoute(
    onRecipeClick: (RecipeRecommendation) -> Unit = {},
    onSettingsClick: () -> Unit = {},
    onIngredientsClick: () -> Unit = {},
    viewModel: HomeViewModel = koinViewModel(),
) {
    val state by viewModel.uiState.collectAsState()

    // The Home stats (XP, cooking sessions, last cooked, new ingredients) are read once per load, so
    // refresh whenever Home comes back to the foreground — e.g. after logging a cook from a recipe.
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) { viewModel.refresh() }

    HomeScreen(
        state = state,
        onRecipeClick = onRecipeClick,
        onSettingsClick = onSettingsClick,
        onIngredientsClick = onIngredientsClick,
    )
}
