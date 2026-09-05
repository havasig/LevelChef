package com.levelchef.feature.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.levelchef.core.designsystem.BadgeStyle
import com.levelchef.core.designsystem.ButtonType
import com.levelchef.core.designsystem.LevelChefBadge
import com.levelchef.core.designsystem.LevelChefButton
import com.levelchef.core.designsystem.LevelChefCard
import com.levelchef.core.designsystem.LevelChefLastCookedCard
import com.levelchef.core.designsystem.LevelChefRecipeCard
import com.levelchef.core.designsystem.LevelChefWeeklyChallengeCard
import com.levelchef.core.designsystem.RecipeCardTag
import com.levelchef.core.ui.theme.LevelChefTextStyles
import com.levelchef.core.ui.theme.LevelChefTheme

/** Section composables for [HomeScreen], kept internal since they're implementation details of this feature. */

@Composable
internal fun LevelProgressSection(state: HomeUiState) {
    val colors = LevelChefTheme.colors
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        LevelChefBadge(state.levelLabel, style = BadgeStyle.LIGHT)
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            val progress = (state.currentXp.toFloat() / state.xpForNextLevel).coerceIn(0f, 1f)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .size(height = 8.dp, width = 0.dp)
                    .background(colors.textPrimary.copy(alpha = 0.1f), RoundedCornerShape(100.dp)),
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(progress)
                        .size(height = 8.dp, width = 0.dp)
                        .background(colors.accentPrimary, RoundedCornerShape(100.dp)),
                )
            }
            Text(
                "${state.currentXp} / ${state.xpForNextLevel} XP to next level",
                color = colors.textSecondary,
                style = LevelChefTextStyles.bodySmall,
            )
        }
    }
}

@Composable
internal fun StatCardsRow(state: HomeUiState) {
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        StatCard("🍳 ${state.cookingSessions}", "Cooking sessions", Modifier.weight(1f))
        StatCard("🌿 ${state.ingredientsTried}", "Ingredients tried", Modifier.weight(1f))
    }
}

/** Not a Figma-inventoried component — a Home-specific use of the generic [LevelChefCard] primitive. */
@Composable
internal fun StatCard(value: String, label: String, modifier: Modifier = Modifier) {
    val colors = LevelChefTheme.colors
    LevelChefCard(modifier = modifier) {
        Text(value, color = colors.textPrimary, style = LevelChefTextStyles.h2)
        Text(label, color = colors.textSecondary, style = LevelChefTextStyles.bodyRegular)
    }
}

@Composable
internal fun WeeklyChallengeSection(state: HomeUiState, onDoneClick: () -> Unit) {
    LevelChefWeeklyChallengeCard(
        title = state.challengeTitle,
        xp = state.challengeXp,
        inProgress = state.challengeInProgress,
        action = { LevelChefButton(label = "Done", type = ButtonType.SECONDARY, onClick = onDoneClick) },
    )
}

@Composable
internal fun RecipeRecommendationCard(rec: RecipeRecommendation, onClick: () -> Unit) {
    LevelChefRecipeCard(
        emoji = rec.emoji,
        title = rec.name,
        xp = rec.xp,
        minutes = rec.minutes,
        difficulty = rec.difficulty,
        onClick = onClick,
        tag = RecipeCardTag(label = rec.tagLabel, emoji = rec.tagEmoji, color = rec.tagColor),
    )
}

@Composable
internal fun LastCookedCard(lastCooked: LastCooked) {
    LevelChefLastCookedCard(title = lastCooked.recipeName, time = lastCooked.whenText, stars = lastCooked.stars)
}
