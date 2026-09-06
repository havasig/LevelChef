package com.levelchef.feature.recipedetail

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.levelchef.core.designsystem.LevelChefPreview
import com.levelchef.core.designsystem.LevelChefTopAppBarHome
import com.levelchef.core.designsystem.PlaceholderScreen
import com.levelchef.core.ui.theme.LevelChefTheme

/**
 * The **Recipes** bottom-nav tab — still a stub. Recipe detail is reached from the Home
 * recommendation cards ([RecipeDetailRoute]); this tab will grow into a saved/browse list later.
 */
@Composable
fun RecipesScreen() {
    val title = stringResource(R.string.saved_recipes_title)
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(LevelChefTheme.colors.background),
    ) {
        LevelChefTopAppBarHome(title = title, modifier = Modifier.statusBarsPadding())
        PlaceholderScreen(title)
    }
}

@LevelChefPreview
@Composable
private fun RecipesScreenPreview() {
    LevelChefTheme { RecipesScreen() }
}
