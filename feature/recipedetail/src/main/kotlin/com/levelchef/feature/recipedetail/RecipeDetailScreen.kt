package com.levelchef.feature.recipedetail

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.levelchef.core.designsystem.LevelChefPreview
import com.levelchef.core.designsystem.LevelChefSnackbar
import com.levelchef.core.designsystem.LevelChefTopAppBarInner
import com.levelchef.core.model.Recipe
import com.levelchef.core.ui.theme.LevelChefTextStyles
import com.levelchef.core.ui.theme.LevelChefTheme
import kotlinx.coroutines.delay

private const val SNACKBAR_VISIBLE_MS = 2500L

/**
 * Figma node 371:728 — the data-driven Recipe Detail screen. Stateless: [RecipeDetailRoute] owns
 * the [RecipeDetailViewModel] and supplies [state] + [actions].
 */
@Composable
fun RecipeDetailScreen(
    state: RecipeDetailUiState = RecipeDetailUiState(),
    actions: RecipeDetailActions = RecipeDetailActions(),
) {
    val colors = LevelChefTheme.colors
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
            .navigationBarsPadding(),
    ) {
        LevelChefTopAppBarInner(
            title = state.recipe?.name ?: stringResource(R.string.recipe_detail_title),
            onBackClick = actions.onBackClick,
            trailingIcon = Icons.Filled.Settings,
            trailingContentDescription = stringResource(R.string.recipe_detail_settings),
            onTrailingClick = actions.onSettingsClick,
            modifier = Modifier.statusBarsPadding(),
        )
        when (val recipe = state.recipe) {
            null -> MissingRecipe(showMessage = !state.loading)
            else -> LoadedRecipe(recipe, state, actions)
        }
    }
}

@Composable
private fun MissingRecipe(showMessage: Boolean) {
    Box(modifier = Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
        if (showMessage) {
            Text(
                stringResource(R.string.recipe_detail_not_found),
                color = LevelChefTheme.colors.textSecondary,
                style = LevelChefTextStyles.bodyRegular,
            )
        }
    }
}

@Composable
private fun LoadedRecipe(recipe: Recipe, state: RecipeDetailUiState, actions: RecipeDetailActions) {
    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 24.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp),
        ) {
            HeroTile(recipe.emoji, recipe.xpReward)
            TitleAndTags(recipe)
            MacrosGrid(recipe)
            ServingsCard(state.servings, actions.onServingsChange)
            IngredientsSection(recipe, state.servings, state.checkedIngredients, actions.onIngredientToggle)
            StepsSection(recipe.steps, actions.onStartTimer)
            recipe.videoUrl?.let { url -> RelatedVideoRow(onClick = { actions.onOpenVideo(url) }) }
            ActionButtons(recipe.xpReward, state.isSaved, actions.onMadeIt, actions.onToggleSaved)
        }

        state.transientMessage?.let { message ->
            LaunchedEffect(message) {
                delay(SNACKBAR_VISIBLE_MS)
                actions.onDismissMessage()
            }
            LevelChefSnackbar(
                message = message.text(recipe.xpReward),
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(16.dp),
            )
        }
    }
}

@LevelChefPreview
@Composable
private fun RecipeDetailScreenPreview() {
    LevelChefTheme { RecipeDetailScreen(state = sampleRecipeDetailState) }
}
