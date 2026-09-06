package com.levelchef.feature.ingredients

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import org.koin.compose.viewmodel.koinViewModel

/** Stateful entry point for the ingredients list. */
@Composable
fun IngredientsListRoute(
    onBackClick: () -> Unit,
    onIngredientClick: (String) -> Unit,
    onAddClick: () -> Unit,
    viewModel: IngredientsListViewModel = koinViewModel(),
) {
    val state by viewModel.uiState.collectAsState()
    IngredientsListScreen(
        state = state,
        onBackClick = onBackClick,
        onIngredientClick = onIngredientClick,
        onAddClick = onAddClick,
    )
}
