package com.levelchef.feature.cookinglog

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.levelchef.core.designsystem.LevelChefPreview
import com.levelchef.core.designsystem.PlaceholderScreen
import com.levelchef.core.ui.theme.LevelChefTheme

/** Figma node 489:1362. */
@Composable
fun CookingLogScreen() = PlaceholderScreen(stringResource(R.string.cooking_log_title))

@LevelChefPreview
@Composable
private fun CookingLogScreenPreview() {
    LevelChefTheme { CookingLogScreen() }
}
