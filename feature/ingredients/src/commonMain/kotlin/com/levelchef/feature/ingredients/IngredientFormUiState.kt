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
) {
    val canSave: Boolean get() = name.isNotBlank()
}
