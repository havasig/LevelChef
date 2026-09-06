package com.levelchef.feature.home

import com.levelchef.core.designsystem.TagColor

/** Placeholder sample data for [HomeUiState]'s default preview state. */
internal val sampleRecommendations = listOf(
    RecipeRecommendation(
        "chicken-curry", "🍲", "Chicken curry with coconut milk", 45, 25, "Easy", "🌿", "New ingredient", TagColor.GREEN,
    ),
    RecipeRecommendation(
        "steak-quinoa-bowl", "🥩", "Steak quinoa bowl", 120, 35, "Medium", "💪", "High protein", TagColor.YELLOW,
    ),
    RecipeRecommendation("jucy-pasta", "🍝", "Jucy pasta", 80, 14, "Easy", "⚡", "High carb", TagColor.RED),
)
