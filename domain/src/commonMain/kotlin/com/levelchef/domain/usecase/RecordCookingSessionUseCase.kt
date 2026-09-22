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
 * [CookingSession] carrying the recipe's XP reward, plus whatever review details (rating, note,
 * duration, macros) the Meal Review screen collected — each defaults to the recipe's own values
 * (or unset) when the caller doesn't supply one.
 */
class RecordCookingSessionUseCase(
    private val cookingSessionRepository: CookingSessionRepository,
    private val clock: Clock = Clock.System,
    private val newId: () -> String = ::randomCookingSessionId,
) {
    suspend operator fun invoke(
        recipe: Recipe,
        rating: Int? = null,
        improvementNote: String? = null,
        durationMinutes: Int = 0,
        kcal: Int? = recipe.caloriesKcal,
        proteinGrams: Int? = recipe.proteinGrams,
        carbsGrams: Int? = recipe.carbsGrams,
        fatGrams: Int? = recipe.fatGrams,
    ) {
        cookingSessionRepository.recordSession(
            CookingSession(
                id = newId(),
                recipeId = recipe.id,
                recipeName = recipe.name,
                cookedAt = clock.now(),
                xpEarned = recipe.xpReward,
                durationMinutes = durationMinutes,
                rating = rating,
                improvementNote = improvementNote,
                kcal = kcal,
                proteinGrams = proteinGrams,
                carbsGrams = carbsGrams,
                fatGrams = fatGrams,
            ),
        )
    }
}
