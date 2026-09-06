package com.levelchef.feature.recipedetail

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalUriHandler
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

/** Stateful entry point for the recipe detail screen. */
@Composable
fun RecipeDetailRoute(
    recipeId: String,
    onBackClick: () -> Unit,
    onSettingsClick: () -> Unit,
    viewModel: RecipeDetailViewModel = koinViewModel { parametersOf(recipeId) },
) {
    val state by viewModel.uiState.collectAsState()
    val uriHandler = LocalUriHandler.current

    RecipeDetailScreen(
        state = state,
        actions = RecipeDetailActions(
            onBackClick = onBackClick,
            onSettingsClick = onSettingsClick,
            onServingsChange = viewModel::changeServings,
            onIngredientToggle = viewModel::toggleIngredient,
            onToggleSaved = viewModel::toggleSaved,
            onMadeIt = viewModel::markCooked,
            onStartTimer = viewModel::showTimerStub,
            onOpenVideo = uriHandler::openUri,
            onDismissMessage = viewModel::dismissMessage,
        ),
    )
}
