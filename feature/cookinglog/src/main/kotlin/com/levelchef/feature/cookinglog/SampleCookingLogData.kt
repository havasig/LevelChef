package com.levelchef.feature.cookinglog

import com.levelchef.core.model.Difficulty

/** Sample data for [CookingLogScreen] previews and screenshot baselines (Figma node 489:1362). */
internal val sampleCookingLogState = CookingLogUiState(
    loading = false,
    allItems = listOf(
        SavedRecipeItem.Uncooked(
            recipeId = "steak-quinoa-bowl",
            name = "Steak quinoa bowl",
            emoji = "🥩",
            xpReward = 120,
            timeMinutes = 35,
            difficulty = Difficulty.MEDIUM,
        ),
        SavedRecipeItem.Cooked(
            recipeId = "lemon-chicken",
            name = "Lemon chicken breast",
            emoji = "🍗",
            xpReward = 60,
            rating = 3,
            cookedAgo = CookedAgo.DaysAgo(3),
        ),
        SavedRecipeItem.Uncooked(
            recipeId = "tuna-wrap",
            name = "Tuna wrap with vegetables",
            emoji = "🌯",
            xpReward = 90,
            timeMinutes = 15,
            difficulty = Difficulty.EASY,
        ),
        SavedRecipeItem.Cooked(
            recipeId = "greek-yogurt-bowl",
            name = "Greek yogurt bowl",
            emoji = "🥣",
            xpReward = 30,
            rating = 4,
            cookedAgo = CookedAgo.Yesterday,
        ),
        SavedRecipeItem.Cooked(
            recipeId = "tomato-pasta",
            name = "Tomato pasta with basil",
            emoji = "🍝",
            xpReward = 80,
            rating = 3,
            cookedAgo = CookedAgo.Today,
        ),
    ),
)
