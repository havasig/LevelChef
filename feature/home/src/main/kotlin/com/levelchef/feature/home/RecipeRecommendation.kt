package com.levelchef.feature.home

import com.levelchef.core.designsystem.TagColor

data class RecipeRecommendation(
    val emoji: String,
    val name: String,
    val xp: Int,
    val minutes: Int,
    val difficulty: String,
    val tagEmoji: String,
    val tagLabel: String,
    val tagColor: TagColor,
)
