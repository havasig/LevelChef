package com.levelchef.feature.ingredients

import com.levelchef.core.model.IngredientCategory
import com.levelchef.core.model.MeasurementUnit

/** Screen model for [IngredientFormScreen] — add (all defaults) or edit (fields pre-filled). */
data class IngredientFormUiState(
    val editing: Boolean = false,
    val name: String = "",
    val category: IngredientCategory = IngredientCategory.VEGETABLE,
    val unit: MeasurementUnit? = null,
    val calories: String = "",
    val protein: String = "",
    val carbs: String = "",
    val fat: String = "",
    val saved: Boolean = false,
    /** True while Save is writing — further taps are ignored, so one tap adds one ingredient. */
    val saving: Boolean = false,
) {
    val canSave: Boolean get() = name.isNotBlank() && !saving && !saved

    companion object {
        const val MAX_NAME_LENGTH = 60

        /** kcal per 100 g/ml tops out around 900 (pure fat); 4 digits leaves headroom without overflow. */
        const val MAX_CALORIE_DIGITS = 4

        /** Grams per 100 g/ml can't exceed 100; 3 integer + 2 decimal digits. */
        const val MAX_GRAM_INTEGER_DIGITS = 3
        const val MAX_GRAM_DECIMALS = 2
    }
}
