@file:OptIn(ExperimentalTime::class, ExperimentalUuidApi::class)

package com.levelchef.domain.usecase

import com.levelchef.core.model.CookingSession
import com.levelchef.core.model.Recipe
import com.levelchef.domain.repository.CookingSessionRepository
import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

/** Generates a random cooking-session id. */
fun randomCookingSessionId(): String = Uuid.random().toString()

/**
 * Records that the user cooked [recipe] ("I made it"): stamps the current time and persists a
 * [CookingSession] carrying the recipe's XP reward and per-serving macros.
 */
class RecordCookingSessionUseCase(
    private val cookingSessionRepository: CookingSessionRepository,
    private val clock: Clock = Clock.System,
    private val newId: () -> String = ::randomCookingSessionId,
) {
    suspend operator fun invoke(recipe: Recipe) {
        cookingSessionRepository.recordSession(
            CookingSession(
                id = newId(),
                recipeId = recipe.id,
                recipeName = recipe.name,
                cookedAt = clock.now(),
                xpEarned = recipe.xpReward,
                kcal = recipe.caloriesKcal,
                proteinGrams = recipe.proteinGrams,
                carbsGrams = recipe.carbsGrams,
                fatGrams = recipe.fatGrams,
            ),
        )
    }
}
