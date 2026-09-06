package com.levelchef.feature.ingredients

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.levelchef.core.model.IngredientCategory
import com.levelchef.core.model.MeasurementUnit

@Composable
internal fun IngredientCategory.label(): String = stringResource(
    when (this) {
        IngredientCategory.MEAT -> R.string.ingredient_category_meat
        IngredientCategory.DAIRY -> R.string.ingredient_category_dairy
        IngredientCategory.VEGETABLE -> R.string.ingredient_category_vegetable
        IngredientCategory.FRUIT -> R.string.ingredient_category_fruit
        IngredientCategory.GRAIN -> R.string.ingredient_category_grain
        IngredientCategory.PANTRY -> R.string.ingredient_category_pantry
        IngredientCategory.OTHER -> R.string.ingredient_category_other
    },
)

@Composable
internal fun MeasurementUnit.label(): String = stringResource(
    when (this) {
        MeasurementUnit.GRAM -> R.string.ingredient_unit_gram
        MeasurementUnit.MILLILITER -> R.string.ingredient_unit_milliliter
        MeasurementUnit.PIECE -> R.string.ingredient_unit_piece
    },
)

/** Emoji shown for an ingredient with no hand-picked one — derived from its category. */
internal fun categoryEmoji(category: IngredientCategory): String = when (category) {
    IngredientCategory.MEAT -> "🥩"
    IngredientCategory.DAIRY -> "🥛"
    IngredientCategory.VEGETABLE -> "🥦"
    IngredientCategory.FRUIT -> "🍎"
    IngredientCategory.GRAIN -> "🌾"
    IngredientCategory.PANTRY -> "🧂"
    IngredientCategory.OTHER -> "🥕"
}

/** "9.5" not "9.5000000001", "0" not "0.0". */
internal fun Double.trimZeros(): String = if (this % 1.0 == 0.0) toLong().toString() else toString()
