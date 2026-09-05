package com.levelchef.feature.recipedetail

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.levelchef.core.designsystem.LevelChefPreview
import com.levelchef.core.designsystem.PlaceholderScreen
import com.levelchef.core.ui.theme.LevelChefTheme

/** Figma node 371:728. */
@Composable
fun RecipeDetailScreen() = PlaceholderScreen(stringResource(R.string.recipe_detail_title))

@LevelChefPreview
@Composable
private fun RecipeDetailScreenPreview() {
    LevelChefTheme { RecipeDetailScreen() }
}
