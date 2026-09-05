package com.levelchef.feature.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.levelchef.core.designsystem.ButtonType
import com.levelchef.core.designsystem.LevelChefButton
import com.levelchef.core.designsystem.LevelChefDivider
import com.levelchef.core.designsystem.LevelChefPreview
import com.levelchef.core.ui.theme.LevelChefTextStyles
import com.levelchef.core.ui.theme.LevelChefTheme

@Composable
fun HomeScreen(
    state: HomeUiState = HomeUiState(),
    onCookToday: () -> Unit = {},
    onRecipeClick: (RecipeRecommendation) -> Unit = {},
    onChallengeDone: () -> Unit = {},
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .background(LevelChefTheme.colors.background),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 24.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp),
    ) {
        item { LevelProgressSection(state) }
        item { StatCardsRow(state) }
        item { LevelChefDivider() }
        item { WeeklyChallengeSection(state, onDoneClick = onChallengeDone) }
        item { LevelChefDivider() }
        item {
            LevelChefButton(
                label = stringResource(R.string.home_cook_today_cta),
                type = ButtonType.PRIMARY,
                onClick = onCookToday,
                modifier = Modifier.fillMaxWidth(),
            )
        }
        item { LevelChefDivider() }
        item {
            Text(
                stringResource(R.string.home_recommended_for_you),
                color = LevelChefTheme.colors.textPrimary,
                style = LevelChefTextStyles.bodyRegularBold,
            )
        }
        items(state.recommendations) { rec ->
            RecipeRecommendationCard(rec, onClick = { onRecipeClick(rec) })
        }
        state.lastCooked?.let { lastCooked ->
            item { LevelChefDivider() }
            item { LastCookedCard(lastCooked) }
        }
    }
}

@LevelChefPreview
@Composable
private fun HomeScreenPreview() {
    LevelChefTheme { HomeScreen() }
}
