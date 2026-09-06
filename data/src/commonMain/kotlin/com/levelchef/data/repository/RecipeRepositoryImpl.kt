package com.levelchef.data.repository

import com.levelchef.core.model.Difficulty
import com.levelchef.core.model.Recipe
import com.levelchef.core.model.RecipeIngredient
import com.levelchef.core.model.RecipeStep
import com.levelchef.domain.repository.RecipeRepository

/**
 * Stub recipe source backed by a static sample list.
 * TODO: replace with the Gemini-powered recommender described in the README's next steps.
 */
class RecipeRepositoryImpl : RecipeRepository {

    private val sampleRecipes = listOf(
        Recipe(
            id = "chicken-curry",
            name = "Chicken curry with coconut milk",
            emoji = "🍲",
            xpReward = 45,
            timeMinutes = 25,
            difficulty = Difficulty.EASY,
            servings = 2,
            caloriesKcal = 520,
            proteinGrams = 38,
            carbsGrams = 18,
            fatGrams = 32,
            tags = listOf("Comfort", "One pot"),
            ingredients = listOf(
                RecipeIngredient("chicken thigh", quantity = 300.0, unit = "g"),
                RecipeIngredient("coconut milk", quantity = 400.0, unit = "ml"),
                RecipeIngredient("yellow curry paste", quantity = 2.0, unit = "tbsp"),
                RecipeIngredient("onion", quantity = 1.0),
                RecipeIngredient("Cilantro, lime, salt"),
            ),
            steps = listOf(
                RecipeStep("Dice the chicken and slice the onion."),
                RecipeStep("Fry the curry paste with the onion until fragrant."),
                RecipeStep("Add the chicken and sear on all sides."),
                RecipeStep("Pour in the coconut milk and simmer.", timerMinutes = 15),
                RecipeStep("Finish with lime juice and fresh cilantro."),
            ),
            videoUrl = "https://www.youtube.com/results?search_query=chicken+curry+coconut+milk",
        ),
        Recipe(
            id = "steak-quinoa-bowl",
            name = "Steak quinoa bowl",
            emoji = "🥩",
            xpReward = 120,
            timeMinutes = 35,
            difficulty = Difficulty.MEDIUM,
            servings = 2,
            caloriesKcal = 610,
            proteinGrams = 46,
            carbsGrams = 52,
            fatGrams = 24,
            tags = listOf("High protein", "Meal prep"),
            ingredients = listOf(
                RecipeIngredient("flank steak", quantity = 250.0, unit = "g"),
                RecipeIngredient("quinoa", quantity = 150.0, unit = "g"),
                RecipeIngredient("cherry tomatoes", quantity = 100.0, unit = "g"),
                RecipeIngredient("avocado", quantity = 1.0),
                RecipeIngredient("Olive oil, lemon, salt, pepper"),
            ),
            steps = listOf(
                RecipeStep("Rinse the quinoa, then simmer in salted water.", timerMinutes = 15),
                RecipeStep("Season the steak generously with salt and pepper."),
                RecipeStep("Sear the steak 3-4 minutes per side, then rest.", timerMinutes = 5),
                RecipeStep("Halve the tomatoes and slice the avocado."),
                RecipeStep("Slice the steak against the grain and build the bowl."),
            ),
            videoUrl = "https://www.youtube.com/results?search_query=steak+quinoa+bowl",
        ),
        Recipe(
            id = "jucy-pasta",
            name = "Jucy pasta",
            emoji = "🍝",
            xpReward = 80,
            timeMinutes = 14,
            difficulty = Difficulty.EASY,
            servings = 2,
            caloriesKcal = 480,
            proteinGrams = 17,
            carbsGrams = 72,
            fatGrams = 14,
            tags = listOf("Quick", "Vegetarian"),
            ingredients = listOf(
                RecipeIngredient("spaghetti", quantity = 200.0, unit = "g"),
                RecipeIngredient("canned tomatoes", quantity = 400.0, unit = "g"),
                RecipeIngredient("garlic", quantity = 2.0, unit = "cloves"),
                RecipeIngredient("Basil, olive oil, salt", isNewToUser = true),
            ),
            steps = listOf(
                RecipeStep("Boil the spaghetti in well-salted water.", timerMinutes = 9),
                RecipeStep("Gently fry the sliced garlic in olive oil."),
                RecipeStep("Add the tomatoes and simmer until thickened."),
                RecipeStep("Toss the drained pasta through the sauce with basil."),
            ),
            videoUrl = "https://www.youtube.com/results?search_query=easy+tomato+pasta",
        ),
    )

    override suspend fun getRecommendations(): List<Recipe> = sampleRecipes

    override suspend fun getById(id: String): Recipe? = sampleRecipes.find { it.id == id }
}
