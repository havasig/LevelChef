package com.levelchef.feature.ingredients

/** Screen model for [IngredientsListScreen]. */
data class IngredientsListUiState(
    val loading: Boolean = true,
    val sections: List<IngredientCategorySection> = emptyList(),
) {
    val isEmpty: Boolean get() = !loading && sections.isEmpty()
}
