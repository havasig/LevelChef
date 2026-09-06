package com.levelchef.core.model

import kotlinx.serialization.Serializable

/**
 * One line item in a [Recipe]'s ingredient list. Distinct from the pantry [Ingredient] entity.
 *
 * [quantity] + [unit] are optional so the servings stepper can scale measurable amounts
 * ("300 g" → "600 g") while free-form entries ("Rosemary, salt, pepper") keep their [name] as-is.
 */
@Serializable
data class RecipeIngredient(
    val name: String,
    val quantity: Double? = null,
    val unit: String? = null,
    val isNewToUser: Boolean = false,
)
