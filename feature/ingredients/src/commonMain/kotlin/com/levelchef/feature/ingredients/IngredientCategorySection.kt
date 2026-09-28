package com.levelchef.feature.ingredients

import com.levelchef.core.model.Ingredient
import com.levelchef.core.model.IngredientCategory

/** One category block on the list screen: its heading and the ingredients filed under it. */
data class IngredientCategorySection(
    val category: IngredientCategory,
    val ingredients: List<Ingredient>,
)
