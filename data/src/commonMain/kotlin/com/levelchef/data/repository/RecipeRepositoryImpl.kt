package com.levelchef.data.repository

import com.levelchef.core.model.Difficulty
import com.levelchef.core.model.Recipe
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
        ),
        Recipe(
            id = "steak-quinoa-bowl",
            name = "Steak quinoa bowl",
            emoji = "🥩",
            xpReward = 120,
            timeMinutes = 35,
            difficulty = Difficulty.MEDIUM,
        ),
        Recipe(
            id = "jucy-pasta",
            name = "Jucy pasta",
            emoji = "🍝",
            xpReward = 80,
            timeMinutes = 14,
            difficulty = Difficulty.EASY,
        ),
    )

    override suspend fun getRecommendations(): List<Recipe> = sampleRecipes

    override suspend fun getById(id: String): Recipe? = sampleRecipes.find { it.id == id }
}
