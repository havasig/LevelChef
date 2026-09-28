package com.levelchef.feature.home

import com.levelchef.core.designsystem.RecipeCardTag
import com.levelchef.core.designsystem.TagColor
import com.levelchef.core.model.Difficulty

/** Placeholder sample data for [HomeUiState]'s default preview state. */
internal val sampleRecommendations = listOf(
    RecipeRecommendation(
        "chicken-curry", "🍲", "Chicken curry with coconut milk", 45, 25, Difficulty.EASY,
        RecipeCardTag(label = "New ingredient", emoji = "🌿", color = TagColor.GREEN),
    ),
    RecipeRecommendation(
        "steak-quinoa-bowl", "🥩", "Steak quinoa bowl", 120, 35, Difficulty.MEDIUM,
        RecipeCardTag(label = "High protein", emoji = "💪", color = TagColor.YELLOW),
    ),
    RecipeRecommendation(
        "juicy-pasta", "🍝", "Juicy pasta", 80, 14, Difficulty.EASY,
        RecipeCardTag(label = "High carb", emoji = "⚡", color = TagColor.RED),
    ),
)
