package com.levelchef.feature.home

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import org.koin.compose.viewmodel.koinViewModel

/** Stateful entry point: collects [HomeViewModel]'s state and hands it to the stateless [HomeScreen]. */
@Composable
fun HomeRoute(
    onRecipeClick: (RecipeRecommendation) -> Unit = {},
    onSettingsClick: () -> Unit = {},
    viewModel: HomeViewModel = koinViewModel(),
) {
    val state by viewModel.uiState.collectAsState()
    HomeScreen(
        state = state,
        onRecipeClick = onRecipeClick,
        onSettingsClick = onSettingsClick,
    )
}
