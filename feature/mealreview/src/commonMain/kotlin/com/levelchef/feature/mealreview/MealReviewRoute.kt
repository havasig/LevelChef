package com.levelchef.feature.mealreview

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

/** Stateful entry point for the Meal Review ("Log experience") screen. */
@Composable
fun MealReviewRoute(
    recipeId: String,
    onBackClick: () -> Unit,
    onSaved: () -> Unit,
    servings: Int? = null,
    viewModel: MealReviewViewModel = koinViewModel { parametersOf(recipeId, servings) },
) {
    val state by viewModel.uiState.collectAsState()

    LaunchedEffect(state.saved) {
        if (state.saved) onSaved()
    }

    MealReviewScreen(
        state = state,
        actions = MealReviewActions(
            onBackClick = onBackClick,
            onRatingChange = viewModel::changeRating,
            onNoteChange = viewModel::changeNote,
            onDurationChange = viewModel::changeDuration,
            onCaloriesChange = viewModel::changeCalories,
            onProteinChange = viewModel::changeProtein,
            onCarbsChange = viewModel::changeCarbs,
            onFatChange = viewModel::changeFat,
            onSaveClick = viewModel::save,
        ),
    )
}
