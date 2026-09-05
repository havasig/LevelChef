package com.levelchef.feature.recipedetail

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.levelchef.core.designsystem.LevelChefPreview
import com.levelchef.core.designsystem.LevelChefTopAppBarHome
import com.levelchef.core.designsystem.LevelChefTopAppBarInner
import com.levelchef.core.designsystem.PlaceholderScreen
import com.levelchef.core.ui.theme.LevelChefTheme

/**
 * Figma node 371:728. Serves two entry points: as the **Recipes** bottom-nav tab ([onBackClick]
 * null — a plain title bar) and as a drill-down opened from Home ([onBackClick] set — a back arrow).
 */
@Composable
fun RecipeDetailScreen(onBackClick: (() -> Unit)? = null) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(LevelChefTheme.colors.background)
            .navigationBarsPadding(),
    ) {
        val title = stringResource(
            if (onBackClick != null) R.string.recipe_detail_title else R.string.saved_recipes_title,
        )
        if (onBackClick != null) {
            LevelChefTopAppBarInner(
                title = title,
                onBackClick = onBackClick,
                modifier = Modifier.statusBarsPadding(),
            )
        } else {
            LevelChefTopAppBarHome(title = title, modifier = Modifier.statusBarsPadding())
        }
        PlaceholderScreen(title)
    }
}

@LevelChefPreview
@Composable
private fun RecipeDetailScreenPreview() {
    LevelChefTheme { RecipeDetailScreen() }
}

@LevelChefPreview
@Composable
private fun RecipeDetailScreenDrillDownPreview() {
    LevelChefTheme { RecipeDetailScreen(onBackClick = {}) }
}
