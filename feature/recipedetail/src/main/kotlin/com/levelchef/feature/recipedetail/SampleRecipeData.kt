package com.levelchef.feature.recipedetail

import com.levelchef.core.model.Difficulty
import com.levelchef.core.model.Recipe
import com.levelchef.core.model.RecipeIngredient
import com.levelchef.core.model.RecipeStep

/** Sample data for [RecipeDetailScreen] previews and screenshot baselines (Figma node 371:728). */
internal val sampleRecipe = Recipe(
    id = "lemon-chicken-breast",
    name = "Lemon chicken breast",
    emoji = "🍗",
    xpReward = 60,
    timeMinutes = 25,
    difficulty = Difficulty.EASY,
    servings = 2,
    caloriesKcal = 320,
    proteinGrams = 38,
    carbsGrams = 8,
    fatGrams = 12,
    ingredients = listOf(
        RecipeIngredient("chicken breast", quantity = 300.0, unit = "g"),
        RecipeIngredient("lemon juice", quantity = 1.0),
        RecipeIngredient("garlic", quantity = 2.0, unit = "cloves"),
        RecipeIngredient("olive oil", quantity = 1.0, unit = "tbsp"),
        RecipeIngredient("Rosemary, salt, pepper", isNewToUser = true),
    ),
    steps = listOf(
        RecipeStep("Pound the chicken breast to an even thickness."),
        RecipeStep("Mix the lemon juice, minced garlic, and olive oil."),
        RecipeStep("Marinate for 15 minutes at room temperature.", timerMinutes = 15),
        RecipeStep("Pan-fry for 5-6 minutes per side over medium heat.", timerMinutes = 6),
        RecipeStep("Rest for 3 minutes before serving."),
    ),
    videoUrl = "https://www.youtube.com/results?search_query=lemon+chicken+breast",
)

internal val sampleRecipeDetailState = RecipeDetailUiState(
    loading = false,
    recipe = sampleRecipe,
    servings = 2,
    checkedIngredients = setOf(0, 1),
    isSaved = false,
)
