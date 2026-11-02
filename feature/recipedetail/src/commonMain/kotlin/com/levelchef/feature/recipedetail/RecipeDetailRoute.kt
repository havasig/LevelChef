package com.levelchef.feature.recipedetail

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalUriHandler
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

/** Stateful entry point for the recipe detail screen. */
@Composable
fun RecipeDetailRoute(
    recipeId: String,
    onBackClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onMadeIt: (servings: Int) -> Unit,
    viewModel: RecipeDetailViewModel = koinViewModel { parametersOf(recipeId) },
) {
    val state by viewModel.uiState.collectAsState()
    val uriHandler = LocalUriHandler.current
    val playTimerAlarm = rememberTimerAlarm()

    // A running step timer keeps the screen awake (hands are busy cooking), and its end is announced
    // with a sound and vibration, not just the snackbar — the phone is usually not being looked at.
    KeepScreenOn(enabled = state.runningTimerStepIndex != null)
    LaunchedEffect(state.transientMessage) {
        if (state.transientMessage == TransientMessage.TIMER_DONE) playTimerAlarm()
    }

    RecipeDetailScreen(
        state = state,
        actions = RecipeDetailActions(
            onBackClick = onBackClick,
            onSettingsClick = onSettingsClick,
            onServingsChange = viewModel::changeServings,
            onIngredientToggle = viewModel::toggleIngredient,
            onToggleSaved = viewModel::toggleSaved,
            onMadeIt = onMadeIt,
            onStartTimer = viewModel::startTimer,
            onCancelTimer = viewModel::cancelTimer,
            onOpenVideo = uriHandler::openWebLinkSafely,
            onDismissMessage = viewModel::dismissMessage,
        ),
    )
}
