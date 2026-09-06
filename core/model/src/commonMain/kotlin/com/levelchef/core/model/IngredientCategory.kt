package com.levelchef.core.model

/** The shelf an [Ingredient] belongs to. Stored as [name]; drives the grouping on the list screen. */
enum class IngredientCategory {
    MEAT,
    DAIRY,
    VEGETABLE,
    FRUIT,
    GRAIN,
    PANTRY,
    OTHER,
}
