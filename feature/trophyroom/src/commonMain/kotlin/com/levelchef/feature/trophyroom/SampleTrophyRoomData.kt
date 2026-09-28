package com.levelchef.feature.trophyroom

/** Placeholder sample data for [TrophyRoomUiState]'s default preview state. */
internal val sampleStreakBadges = listOf(
    BadgeUiModel("marathon-cook", "⏱️", "Marathon Cook", "Rack up 10 hours of kitchen time.", 240, 600, earned = false),
    BadgeUiModel("night-owl", "🦉", "Night Owl", "Cook a meal after 10pm.", 0, 1, earned = false),
)

internal val sampleBadges = listOf(
    BadgeUiModel("first-bite", "🍽️", "First Bite", "Log your very first cooking session.", 1, 1, earned = true),
    BadgeUiModel("ten-meals-deep", "🍲", "Ten Meals Deep", "Cook 10 meals.", 4, 10, earned = false),
    BadgeUiModel("pantry-starter", "🌱", "Pantry Starter", "Log 5 different ingredients in your pantry.", 5, 5, earned = true),
)
