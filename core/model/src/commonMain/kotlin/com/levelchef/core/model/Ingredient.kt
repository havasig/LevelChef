package com.levelchef.core.model

import kotlinx.serialization.Serializable

/**
 * A pantry ingredient the user has logged. [emoji] is the visual today; [imageUrl] is reserved for
 * the future AI-generated image. Distinct from [RecipeIngredient], which is a recipe line item.
 */
@Serializable
data class Ingredient(
    val id: String,
    val name: String,
    val category: IngredientCategory,
    val emoji: String,
    val defaultUnit: MeasurementUnit? = null,
    val macros: IngredientMacros? = null,
    val imageUrl: String? = null,
)
