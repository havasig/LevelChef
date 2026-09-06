package com.levelchef.core.model

import kotlinx.serialization.Serializable

/** Nutritional values for an [Ingredient], per 100 g / 100 ml. */
@Serializable
data class IngredientMacros(
    val calories: Int,
    val proteinGrams: Double,
    val carbsGrams: Double,
    val fatGrams: Double,
)
