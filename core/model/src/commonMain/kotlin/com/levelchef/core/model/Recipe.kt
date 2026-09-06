package com.levelchef.core.model

import kotlinx.serialization.Serializable

@Serializable
data class Recipe(
    val id: String,
    val name: String,
    val emoji: String,
    val xpReward: Int,
    val timeMinutes: Int,
    val difficulty: Difficulty,
    val proteinGrams: Int? = null,
    val tags: List<String> = emptyList(),
    val ingredients: List<RecipeIngredient> = emptyList(),
    val steps: List<String> = emptyList(),
)
