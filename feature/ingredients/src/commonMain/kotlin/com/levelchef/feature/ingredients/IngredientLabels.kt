package com.levelchef.feature.ingredients

import androidx.compose.runtime.Composable
import com.levelchef.core.model.IngredientCategory
import com.levelchef.core.model.MeasurementUnit
import com.levelchef.feature.ingredients.generated.resources.Res
import com.levelchef.feature.ingredients.generated.resources.ingredient_category_dairy
import com.levelchef.feature.ingredients.generated.resources.ingredient_category_fruit
import com.levelchef.feature.ingredients.generated.resources.ingredient_category_grain
import com.levelchef.feature.ingredients.generated.resources.ingredient_category_meat
import com.levelchef.feature.ingredients.generated.resources.ingredient_category_other
import com.levelchef.feature.ingredients.generated.resources.ingredient_category_pantry
import com.levelchef.feature.ingredients.generated.resources.ingredient_category_vegetable
import com.levelchef.feature.ingredients.generated.resources.ingredient_unit_gram
import com.levelchef.feature.ingredients.generated.resources.ingredient_unit_milliliter
import com.levelchef.feature.ingredients.generated.resources.ingredient_unit_piece
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun IngredientCategory.label(): String = stringResource(
    when (this) {
        IngredientCategory.MEAT -> Res.string.ingredient_category_meat
        IngredientCategory.DAIRY -> Res.string.ingredient_category_dairy
        IngredientCategory.VEGETABLE -> Res.string.ingredient_category_vegetable
        IngredientCategory.FRUIT -> Res.string.ingredient_category_fruit
        IngredientCategory.GRAIN -> Res.string.ingredient_category_grain
        IngredientCategory.PANTRY -> Res.string.ingredient_category_pantry
        IngredientCategory.OTHER -> Res.string.ingredient_category_other
    },
)

@Composable
internal fun MeasurementUnit.label(): String = stringResource(
    when (this) {
        MeasurementUnit.GRAM -> Res.string.ingredient_unit_gram
        MeasurementUnit.MILLILITER -> Res.string.ingredient_unit_milliliter
        MeasurementUnit.PIECE -> Res.string.ingredient_unit_piece
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
