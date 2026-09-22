package com.levelchef.feature.cookinglog

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import org.koin.compose.viewmodel.koinViewModel

/** Stateful entry point for the Recipes bottom-nav tab. */
@Composable
fun CookingLogRoute(
    onRecipeClick: (recipeId: String) -> Unit,
    viewModel: CookingLogViewModel = koinViewModel(),
) {
    val state by viewModel.uiState.collectAsState()

    CookingLogScreen(
        state = state,
        actions = CookingLogActions(
            onQueryChange = viewModel::changeQuery,
            onTabSelected = viewModel::selectTab,
            onRecipeClick = onRecipeClick,
            onDeleteClick = viewModel::deleteRecipe,
        ),
    )
}
