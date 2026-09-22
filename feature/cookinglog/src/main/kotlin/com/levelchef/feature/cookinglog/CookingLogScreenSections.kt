package com.levelchef.feature.cookinglog

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.levelchef.core.designsystem.IconButtonStyle
import com.levelchef.core.designsystem.LevelChefIconButton
import com.levelchef.core.designsystem.LevelChefLastCookedCard
import com.levelchef.core.designsystem.LevelChefRecipeCard
import com.levelchef.core.model.Difficulty
import com.levelchef.core.ui.theme.LevelChefTextStyles
import com.levelchef.core.ui.theme.LevelChefTheme

/** Section composables for [CookingLogScreen], kept internal to this feature. */

internal fun List<SavedRecipeItem>.filtered(tab: CookingLogTab, query: String): List<SavedRecipeItem> {
    val byTab = when (tab) {
        CookingLogTab.ALL -> this
        CookingLogTab.COOKED -> filterIsInstance<SavedRecipeItem.Cooked>()
        CookingLogTab.NEW -> filterIsInstance<SavedRecipeItem.Uncooked>()
    }
    if (query.isBlank()) return byTab
    return byTab.filter { it.name.contains(query, ignoreCase = true) }
}

@Composable
internal fun SavedRecipeRow(item: SavedRecipeItem, onClick: () -> Unit, onDelete: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(modifier = Modifier.weight(1f)) {
            when (item) {
                is SavedRecipeItem.Uncooked -> LevelChefRecipeCard(
                    emoji = item.emoji,
                    title = item.name,
                    xp = item.xpReward,
                    minutes = item.timeMinutes,
                    difficulty = item.difficulty.label(),
                    onClick = onClick,
                )

                is SavedRecipeItem.Cooked -> LevelChefLastCookedCard(
                    title = item.name,
                    time = item.cookedAgo.label(),
                    stars = item.rating,
                    label = stringResource(R.string.cooking_log_last_cooked_label),
                    onClick = onClick,
                )
            }
        }
        LevelChefIconButton(
            icon = Icons.Filled.Delete,
            contentDescription = stringResource(R.string.cooking_log_delete, item.name),
            style = IconButtonStyle.PLAIN,
            onClick = onDelete,
        )
    }
}

@Composable
internal fun EmptyState(tab: CookingLogTab, modifier: Modifier = Modifier) {
    val textRes = when (tab) {
        CookingLogTab.ALL -> R.string.cooking_log_empty_all
        CookingLogTab.COOKED -> R.string.cooking_log_empty_cooked
        CookingLogTab.NEW -> R.string.cooking_log_empty_new
    }
    Box(
        modifier = modifier.fillMaxSize().padding(24.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            stringResource(textRes),
            color = LevelChefTheme.colors.textSecondary,
            style = LevelChefTextStyles.bodyRegular,
        )
    }
}

@Composable
private fun Difficulty.label(): String = stringResource(
    when (this) {
        Difficulty.EASY -> R.string.cooking_log_difficulty_easy
        Difficulty.MEDIUM -> R.string.cooking_log_difficulty_medium
        Difficulty.HARD -> R.string.cooking_log_difficulty_hard
    },
)

@Composable
private fun CookedAgo.label(): String = when (this) {
    CookedAgo.Today -> stringResource(R.string.cooking_log_cooked_today)
    CookedAgo.Yesterday -> stringResource(R.string.cooking_log_cooked_yesterday)
    is CookedAgo.DaysAgo -> stringResource(R.string.cooking_log_cooked_days_ago, days)
}
