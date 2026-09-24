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

/** [nameOverride] is the recipe's current (app-language) name; the logged [CookingSession.recipeName] is the fallback. */
internal fun CookingSession.toLastCooked(nameOverride: String? = null): LastCooked = LastCooked(
    recipeName = nameOverride ?: recipeName,
    daysAgo = cookedAt.wholeDaysAgo(),
    stars = rating ?: 0,
)

@OptIn(ExperimentalTime::class)
private fun Instant.wholeDaysAgo(): Int = (Clock.System.now() - this).inWholeDays.toInt().coerceAtLeast(0)
