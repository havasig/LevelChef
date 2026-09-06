package com.levelchef.feature.recipedetail

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.levelchef.core.model.Difficulty

@Composable
internal fun Difficulty.label(): String = stringResource(
    when (this) {
        Difficulty.EASY -> R.string.recipe_detail_difficulty_easy
        Difficulty.MEDIUM -> R.string.recipe_detail_difficulty_medium
        Difficulty.HARD -> R.string.recipe_detail_difficulty_hard
    },
)
