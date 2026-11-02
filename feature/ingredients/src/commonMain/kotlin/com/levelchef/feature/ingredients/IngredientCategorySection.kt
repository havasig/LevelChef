package com.levelchef.feature.ingredients

import com.levelchef.core.model.Ingredient
import com.levelchef.core.model.IngredientCategory

/** One category block on the list screen: its heading and the ingredients filed under it.
 * [expanded] opens the block past its first few items — set when it holds an ingredient that was
 * just added, so the new item is visible on return from the form instead of hidden behind "Show all". */
data class IngredientCategorySection(
    val category: IngredientCategory,
    val ingredients: List<Ingredient>,
    val expanded: Boolean = false,
)
