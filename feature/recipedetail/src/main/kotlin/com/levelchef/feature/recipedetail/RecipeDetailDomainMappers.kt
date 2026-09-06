package com.levelchef.feature.recipedetail

import com.levelchef.core.model.RecipeIngredient
import kotlin.math.roundToLong

/**
 * Renders a recipe ingredient as one display line, scaling its [RecipeIngredient.quantity] from the
 * recipe's [baseServings] to the user-chosen [servings]. Quantity-less entries (e.g. "salt, pepper")
 * render their [RecipeIngredient.name] unchanged.
 */
internal fun RecipeIngredient.toDisplayLine(servings: Int, baseServings: Int): String {
    val rawQuantity = quantity ?: return name
    val scaled = rawQuantity * servings / baseServings.coerceAtLeast(1)
    val amount = if (scaled % 1.0 == 0.0) {
        scaled.roundToLong().toString()
    } else {
        ((scaled * HUNDREDTHS).roundToLong() / HUNDREDTHS).toString()
    }
    return listOfNotNull(amount, unit, name).joinToString(" ")
}

private const val HUNDREDTHS = 100.0
