package com.levelchef.feature.ingredients

import com.levelchef.core.model.Ingredient
import com.levelchef.core.model.IngredientCategory
import com.levelchef.core.model.IngredientMacros
import com.levelchef.core.model.MeasurementUnit

private fun sample(
    id: String,
    name: String,
    category: IngredientCategory,
    emoji: String,
    unit: MeasurementUnit = MeasurementUnit.GRAM,
    macros: IngredientMacros? = null,
) = Ingredient(id, name, category, emoji, unit, macros)

private val chickenBreast = sample(
    "chicken-breast", "Chicken breast", IngredientCategory.MEAT, "🍗",
    macros = IngredientMacros(165, 31.0, 0.0, 3.6),
)

internal val sampleIngredients: List<Ingredient> = listOf(
    chickenBreast,
    sample("beef-brisket", "Beef brisket", IngredientCategory.MEAT, "🥩"),
    sample("turkey-breast", "Turkey breast", IngredientCategory.MEAT, "🦃"),
    sample("salmon-fillet", "Salmon fillet", IngredientCategory.MEAT, "🐟"),
    sample("greek-yogurt", "Greek yogurt", IngredientCategory.DAIRY, "🥛", MeasurementUnit.MILLILITER),
    sample("parmesan", "Parmesan", IngredientCategory.DAIRY, "🧀"),
    sample("broccoli", "Broccoli", IngredientCategory.VEGETABLE, "🥦"),
    sample("avocado", "Avocado", IngredientCategory.VEGETABLE, "🥑", MeasurementUnit.PIECE),
    sample("spinach", "Spinach", IngredientCategory.VEGETABLE, "🥬"),
    sample("bell-pepper", "Bell pepper", IngredientCategory.VEGETABLE, "🌶️", MeasurementUnit.PIECE),
    sample("zucchini", "Zucchini", IngredientCategory.VEGETABLE, "🥒", MeasurementUnit.PIECE),
    sample("lemon", "Lemon", IngredientCategory.FRUIT, "🍋", MeasurementUnit.PIECE),
    sample("apple", "Apple", IngredientCategory.FRUIT, "🍎", MeasurementUnit.PIECE),
    sample("banana", "Banana", IngredientCategory.FRUIT, "🍌", MeasurementUnit.PIECE),
)

internal val sampleIngredientsListState = IngredientsListUiState(
    loading = false,
    sections = IngredientCategory.entries.mapNotNull { category ->
        sampleIngredients.filter { it.category == category }
            .takeIf { it.isNotEmpty() }
            ?.let { IngredientCategorySection(category, it) }
    },
)

internal val sampleIngredientDetailState = IngredientDetailUiState(loading = false, ingredient = chickenBreast)

internal val sampleIngredientFormState = IngredientFormUiState(
    name = "Chicken breast",
    category = IngredientCategory.MEAT,
    unit = MeasurementUnit.GRAM,
    calories = "165",
    protein = "31",
)
