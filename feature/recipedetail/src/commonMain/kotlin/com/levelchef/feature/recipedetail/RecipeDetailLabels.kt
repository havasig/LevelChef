package com.levelchef.feature.recipedetail

import androidx.compose.runtime.Composable
import com.levelchef.core.model.Difficulty
import com.levelchef.feature.recipedetail.generated.resources.Res
import com.levelchef.feature.recipedetail.generated.resources.recipe_detail_difficulty_easy
import com.levelchef.feature.recipedetail.generated.resources.recipe_detail_difficulty_hard
import com.levelchef.feature.recipedetail.generated.resources.recipe_detail_difficulty_medium
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun Difficulty.label(): String = stringResource(
    when (this) {
        Difficulty.EASY -> Res.string.recipe_detail_difficulty_easy
        Difficulty.MEDIUM -> Res.string.recipe_detail_difficulty_medium
        Difficulty.HARD -> Res.string.recipe_detail_difficulty_hard
    },
)
