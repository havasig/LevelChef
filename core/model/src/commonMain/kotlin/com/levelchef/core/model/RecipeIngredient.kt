package com.levelchef.core.model

import kotlinx.serialization.Serializable

/** One line item in a [Recipe]'s ingredient list. Distinct from the pantry [Ingredient] entity. */
@Serializable
data class RecipeIngredient(
    val name: String,
    val isNewToUser: Boolean = false,
)
