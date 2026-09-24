package com.levelchef.feature.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.levelchef.core.designsystem.BadgeStyle
import com.levelchef.core.designsystem.ButtonType
import com.levelchef.core.designsystem.LevelChefBadge
import com.levelchef.core.designsystem.LevelChefButton
import com.levelchef.core.designsystem.LevelChefCard
import com.levelchef.core.designsystem.LevelChefLastCookedCard
import com.levelchef.core.designsystem.LevelChefRecipeCard
import com.levelchef.core.designsystem.LevelChefWeeklyChallengeCard
import com.levelchef.core.ui.theme.LevelChefTextStyles
import com.levelchef.core.ui.theme.LevelChefTheme

/** Section composables for [HomeScreen], kept internal since they're implementation details of this feature. */

@Composable
internal fun LevelProgressSection(state: HomeUiState) {
    val colors = LevelChefTheme.colors
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        LevelChefBadge(
            stringResource(R.string.home_level_badge, state.level.label(), state.level.ordinal + 1),
            style = BadgeStyle.LIGHT,
        )
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
                stringResource(R.string.home_xp_to_next_level, state.currentXp, state.xpForNextLevel),
                color = colors.textSecondary,
                style = LevelChefTextStyles.bodySmall,
            )
        }
    }
}

@Composable
internal fun StatCardsRow(state: HomeUiState, onIngredientsClick: () -> Unit = {}) {
    // IntrinsicSize.Min + fillMaxHeight keeps both cards the same height when one label wraps (e.g. Hungarian).
    Row(Modifier.height(IntrinsicSize.Min), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        StatCard(
            "🍳 ${state.cookingSessions}",
            stringResource(R.string.home_cooking_sessions_label),
            Modifier.weight(1f).fillMaxHeight(),
        )
        StatCard(
            "🌿 ${state.ingredientsTried}",
            stringResource(R.string.home_ingredients_tried_label),
            Modifier.weight(1f).fillMaxHeight(),
            onClick = onIngredientsClick,
        )
    }
}

/** Not a Figma-inventoried component — a Home-specific use of the generic [LevelChefCard] primitive. */
@Composable
internal fun StatCard(value: String, label: String, modifier: Modifier = Modifier, onClick: (() -> Unit)? = null) {
    val colors = LevelChefTheme.colors
    LevelChefCard(modifier = if (onClick != null) modifier.clickable(onClick = onClick) else modifier) {
        Text(value, color = colors.textPrimary, style = LevelChefTextStyles.h2)
        Text(label, color = colors.textSecondary, style = LevelChefTextStyles.bodyRegular)
    }
}

@Composable
internal fun WeeklyChallengeSection(state: HomeUiState, onDoneClick: () -> Unit) {
    LevelChefWeeklyChallengeCard(
        title = weeklyChallengeTitle(state.challengeId, fallback = state.challengeTitle),
        xp = state.challengeXp,
        inProgress = true,
        completed = state.challengeCompleted,
        action = if (state.challengeCompleted) {
            null
        } else {
            {
                LevelChefButton(
                    label = stringResource(R.string.home_challenge_done),
                    type = ButtonType.SECONDARY,
                    enabled = state.challengeEligible,
                    onClick = onDoneClick,
                )
            }
        },
    )
}

@Composable
internal fun RecipeRecommendationCard(rec: RecipeRecommendation, onClick: () -> Unit) {
    LevelChefRecipeCard(
        emoji = rec.emoji,
        title = rec.name,
        xp = rec.xp,
        minutes = rec.minutes,
        difficulty = rec.difficulty.label(),
        onClick = onClick,
        tag = rec.tag,
    )
}

@Composable
internal fun LastCookedCard(lastCooked: LastCooked) {
    LevelChefLastCookedCard(
        title = lastCooked.recipeName,
        time = daysAgoLabel(lastCooked.daysAgo),
        stars = lastCooked.stars,
    )
}
