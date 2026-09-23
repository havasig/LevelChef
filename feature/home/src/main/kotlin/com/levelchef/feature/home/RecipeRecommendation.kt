package com.levelchef.feature.home

import com.levelchef.core.designsystem.RecipeCardTag
import com.levelchef.core.model.Difficulty

data class RecipeRecommendation(
    val id: String,
    val emoji: String,
    val name: String,
    val xp: Int,
    val minutes: Int,
    val difficulty: Difficulty,
    val tag: RecipeCardTag? = null,
)
