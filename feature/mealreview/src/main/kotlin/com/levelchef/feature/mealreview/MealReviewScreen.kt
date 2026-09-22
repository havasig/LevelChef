package com.levelchef.feature.mealreview

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.levelchef.core.designsystem.LevelChefPreview
import com.levelchef.core.designsystem.LevelChefTopAppBarInner
import com.levelchef.core.ui.theme.LevelChefTheme

/**
 * Figma node 385:586 — "Log experience": rate a just-cooked recipe, add a note, adjust cook time
 * and macros, then Save. Stateless: [MealReviewRoute] owns the [MealReviewViewModel] and supplies
 * [state] + [actions]. Reached from `feature:recipedetail`'s "I made it" button.
 */
@Composable
fun MealReviewScreen(
    state: MealReviewUiState = MealReviewUiState(),
    actions: MealReviewActions = MealReviewActions(),
) {
    val colors = LevelChefTheme.colors
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
            .navigationBarsPadding(),
    ) {
        LevelChefTopAppBarInner(
            title = stringResource(R.string.meal_review_title),
            onBackClick = actions.onBackClick,
            modifier = Modifier.statusBarsPadding(),
        )
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            RecipeHeaderCard(state.recipeName, state.xpReward)
            RatingSection(state.rating, actions.onRatingChange)
            NoteSection(state.note, actions.onNoteChange)
            DurationRow(state.durationMinutes, actions.onDurationChange)
            MacroValuesSection(
                caloriesKcal = state.caloriesKcal,
                proteinGrams = state.proteinGrams,
                carbsGrams = state.carbsGrams,
                fatGrams = state.fatGrams,
                onCaloriesChange = actions.onCaloriesChange,
                onProteinChange = actions.onProteinChange,
                onCarbsChange = actions.onCarbsChange,
                onFatChange = actions.onFatChange,
            )
            IngredientsChecklist(state.ingredientLines)
            SaveButton(enabled = state.rating > 0, onClick = actions.onSaveClick)
        }
    }
}

@LevelChefPreview
@Composable
private fun MealReviewScreenPreview() {
    LevelChefTheme { MealReviewScreen(state = sampleMealReviewState) }
}
