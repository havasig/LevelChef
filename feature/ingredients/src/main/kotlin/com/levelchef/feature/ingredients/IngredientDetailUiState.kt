package com.levelchef.feature.ingredients

import com.levelchef.core.model.Ingredient

/** Screen model for [IngredientDetailScreen]. */
data class IngredientDetailUiState(
    val loading: Boolean = true,
    val ingredient: Ingredient? = null,
    val deleted: Boolean = false,
)
