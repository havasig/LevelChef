package com.levelchef.feature.home

import com.levelchef.core.designsystem.TagColor
import com.levelchef.core.model.CookingSession
import com.levelchef.core.model.Recipe
import kotlinx.datetime.Instant
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

/** Maps domain models (from [com.levelchef.domain.repository]) onto this screen's presentation types. */

internal fun Recipe.toRecommendation(): RecipeRecommendation = RecipeRecommendation(
    emoji = emoji,
    name = name,
    xp = xpReward,
    minutes = timeMinutes,
    difficulty = difficulty.name.lowercase().replaceFirstChar { it.uppercase() },
    tagEmoji = "🤖",
    tagLabel = "AI pick",
    tagColor = TagColor.GREEN,
)

internal fun CookingSession.toLastCooked(): LastCooked = LastCooked(
    recipeName = recipeName,
    whenText = cookedAt.toRelativeDayString(),
    stars = rating ?: 0,
)

@OptIn(ExperimentalTime::class)
private fun Instant.toRelativeDayString(): String {
    val daysAgo = (Clock.System.now() - this).inWholeDays
    return when {
        daysAgo <= 0 -> "today"
        daysAgo == 1L -> "1 day ago"
        else -> "$daysAgo days ago"
    }
}
