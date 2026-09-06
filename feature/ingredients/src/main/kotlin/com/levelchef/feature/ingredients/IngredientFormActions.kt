package com.levelchef.feature.ingredients

import com.levelchef.core.model.IngredientCategory
import com.levelchef.core.model.MeasurementUnit

/** Callbacks [IngredientFormScreen] needs, bundled so the stateless screen stays easy to preview. */
data class IngredientFormActions(
    val onNameChange: (String) -> Unit = {},
    val onCategoryChange: (IngredientCategory) -> Unit = {},
    val onUnitChange: (MeasurementUnit?) -> Unit = {},
    val onCaloriesChange: (String) -> Unit = {},
    val onProteinChange: (String) -> Unit = {},
    val onCarbsChange: (String) -> Unit = {},
    val onFatChange: (String) -> Unit = {},
    val onSave: () -> Unit = {},
)
