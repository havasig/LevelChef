package com.levelchef.feature.home

import com.levelchef.core.model.CookingSession
import com.levelchef.core.model.Recipe
import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

/** Maps domain models (from [com.levelchef.domain.repository]) onto this screen's presentation types. */

internal fun Recipe.toRecommendation(): RecipeRecommendation = RecipeRecommendation(
    id = id,
    emoji = emoji,
    name = name,
    xp = xpReward,
    minutes = timeMinutes,
    difficulty = difficulty,
)

internal fun CookingSession.toLastCooked(): LastCooked = LastCooked(
    recipeName = recipeName,
    daysAgo = cookedAt.wholeDaysAgo(),
    stars = rating ?: 0,
)

@OptIn(ExperimentalTime::class)
private fun Instant.wholeDaysAgo(): Int = (Clock.System.now() - this).inWholeDays.toInt().coerceAtLeast(0)
