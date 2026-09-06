package com.levelchef.feature.trophyroom

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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.levelchef.core.designsystem.BadgeStyle
import com.levelchef.core.designsystem.LevelChefAvatar
import com.levelchef.core.designsystem.LevelChefBadge
import com.levelchef.core.designsystem.LevelChefCard
import com.levelchef.core.designsystem.LevelChefTag
import com.levelchef.core.designsystem.TagColor
import com.levelchef.core.ui.theme.LevelChefTextStyles
import com.levelchef.core.ui.theme.LevelChefTheme

/** Section composables for [TrophyRoomScreen], kept internal since they're implementation details of this feature. */

@Composable
internal fun ProfileCard(state: TrophyRoomUiState) {
    val colors = LevelChefTheme.colors
    LevelChefCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                LevelChefAvatar(initials = state.levelEmoji)
                Column {
                    Text(state.levelName, color = colors.textPrimary, style = LevelChefTextStyles.bodyLargeBold)
                    Text(
                        stringResource(R.string.trophy_room_level_of, state.levelIndex, state.levelCount),
                        color = colors.textSecondary,
                        style = LevelChefTextStyles.bodySmall,
                    )
                }
            }
            if (state.isMaxLevel) {
                LevelChefBadge(stringResource(R.string.trophy_room_max_level), style = BadgeStyle.DARK)
            }
        }
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(
                    stringResource(R.string.trophy_room_level_progress),
                    color = colors.textSecondary,
                    style = LevelChefTextStyles.bodySmall,
                )
                val percent = levelProgressPercent(state.currentXp, state.xpForNextLevel)
                Text(
                    stringResource(R.string.trophy_room_xp_percent, state.currentXp, state.xpForNextLevel, percent),
                    color = colors.textSecondary,
                    style = LevelChefTextStyles.bodySmall,
                )
            }
            ProgressBar(current = state.currentXp, target = state.xpForNextLevel)
        }
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(
                "🔥 " + stringResource(R.string.trophy_room_streak, state.streakDays),
                color = colors.textSecondary,
                style = LevelChefTextStyles.bodySmall,
            )
            state.avgRatingPercent?.let {
                Text(
                    "🎯 " + stringResource(R.string.trophy_room_avg_rating, it),
                    color = colors.textSecondary,
                    style = LevelChefTextStyles.bodySmall,
                )
            }
        }
    }
}

@Composable
internal fun TrophyStatCardsRow(state: TrophyRoomUiState) {
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        TrophyStatCard(
            label = stringResource(R.string.trophy_room_weekly_challenge_label),
            value = if (state.weeklyChallengeCompleted) {
                stringResource(R.string.trophy_room_completed)
            } else {
                state.weeklyChallengeProgressText
            },
            caption = stringResource(R.string.trophy_room_weekly_challenge_caption),
            modifier = Modifier.weight(1f),
        )
        TrophyStatCard(
            label = stringResource(R.string.trophy_room_kitchen_time_label),
            value = state.kitchenTimeLabel,
            caption = stringResource(R.string.trophy_room_kitchen_time_caption),
            modifier = Modifier.weight(1f),
        )
    }
}

/** Not a Figma-inventoried component — a Trophy Room-specific use of the generic [LevelChefCard]
 * primitive, mirroring `feature:home`'s `StatCard`. */
@Composable
internal fun TrophyStatCard(label: String, value: String, caption: String, modifier: Modifier = Modifier) {
    val colors = LevelChefTheme.colors
    LevelChefCard(modifier = modifier) {
        Text(label, color = colors.textSecondary, style = LevelChefTextStyles.bodySmall)
        Text(value, color = colors.textPrimary, style = LevelChefTextStyles.h2)
        Text(caption, color = colors.textSecondary, style = LevelChefTextStyles.captionRegular)
    }
}

@Composable
internal fun SectionHeader(text: String) {
    Text(text.uppercase(), color = LevelChefTheme.colors.textSecondary, style = LevelChefTextStyles.captionBold)
}

/** A "Streaks" card: an in-progress [BadgeUiModel] with a live progress bar and status pill. */
@Composable
internal fun StreakBadgeCard(badge: BadgeUiModel) {
    val colors = LevelChefTheme.colors
    LevelChefCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                Text(badge.emoji, style = LevelChefTextStyles.bodyLargeBold)
                Text(badge.name, color = colors.textPrimary, style = LevelChefTextStyles.bodyRegularBold)
            }
            when {
                badge.earned -> LevelChefTag(stringResource(R.string.trophy_room_earned), color = TagColor.GREEN)
                badge.progressCurrent > 0 -> LevelChefTag(stringResource(R.string.trophy_room_in_progress), color = TagColor.PURPLE)
                else -> LevelChefBadge(stringResource(R.string.trophy_room_not_started), style = BadgeStyle.LIGHT)
            }
        }
        Text(badge.description, color = colors.textSecondary, style = LevelChefTextStyles.bodySmall)
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            ProgressBar(current = badge.progressCurrent, target = badge.progressTarget, modifier = Modifier.weight(1f))
            Text("${badge.progressCurrent}/${badge.progressTarget}", color = colors.textSecondary, style = LevelChefTextStyles.bodySmall)
        }
    }
}

/** A "Badges" card: a collected [BadgeUiModel] shown flat as earned-or-locked, no progress bar. */
@Composable
internal fun BadgeCard(badge: BadgeUiModel) {
    val colors = LevelChefTheme.colors
    LevelChefCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                Text(badge.emoji, style = LevelChefTextStyles.bodyLargeBold)
                Text(
                    badge.name,
                    color = if (badge.earned) colors.textPrimary else colors.textSecondary,
                    style = LevelChefTextStyles.bodyRegularBold,
                )
            }
            if (badge.earned) {
                LevelChefTag(stringResource(R.string.trophy_room_earned), color = TagColor.GREEN)
            } else {
                LevelChefBadge(stringResource(R.string.trophy_room_locked), style = BadgeStyle.LIGHT)
            }
        }
        Text(badge.description, color = colors.textSecondary, style = LevelChefTextStyles.bodySmall)
    }
}

/** The thin rounded track + filled bar shared by the level-progress header and streak badge cards
 * (same visual as `feature:home`'s `LevelProgressSection`, but reusable here for badges too). */
@Composable
private fun ProgressBar(current: Int, target: Int, modifier: Modifier = Modifier) {
    val colors = LevelChefTheme.colors
    val progress = if (target <= 0) 0f else (current.toFloat() / target).coerceIn(0f, 1f)
    Box(
        modifier = modifier
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
}

private const val PERCENT = 100

private fun levelProgressPercent(current: Int, target: Int): Int =
    if (target <= 0) PERCENT else ((current.toFloat() / target) * PERCENT).toInt().coerceIn(0, PERCENT)
