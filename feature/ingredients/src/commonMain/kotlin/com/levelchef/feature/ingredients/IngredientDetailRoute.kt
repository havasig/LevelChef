package com.levelchef.feature.ingredients

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

/** Stateful entry point for the ingredient detail screen. */
@Composable
fun IngredientDetailRoute(
    ingredientId: String,
    onBackClick: () -> Unit,
    onEditClick: (String) -> Unit,
    onDeleted: () -> Unit,
    viewModel: IngredientDetailViewModel = koinViewModel { parametersOf(ingredientId) },
) {
    val state by viewModel.uiState.collectAsState()

    LaunchedEffect(state.deleted) {
        if (state.deleted) onDeleted()
    }

    IngredientDetailScreen(
        state = state,
        onBackClick = onBackClick,
        onEditClick = { onEditClick(ingredientId) },
        onDeleteConfirmed = viewModel::delete,
    )
}
