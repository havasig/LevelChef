package com.levelchef.feature.home

import com.levelchef.core.designsystem.TagColors

data class RecipeRecommendation(
    val emoji: String,
    val name: String,
    val xp: Int,
    val minutes: Int,
    val difficulty: String,
    val tagEmoji: String,
    val tagLabel: String,
    val tagColors: TagColors,
)
