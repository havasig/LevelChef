@file:OptIn(ExperimentalTime::class)

package com.levelchef.domain.usecase

import com.levelchef.core.model.CookingSession
import com.levelchef.core.model.Difficulty
import com.levelchef.core.model.Recipe
import com.levelchef.domain.repository.CookingSessionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

private class CapturingCookingSessionRepository : CookingSessionRepository {
    val recorded = mutableListOf<CookingSession>()
    override fun observeAll(): Flow<List<CookingSession>> = flowOf(recorded)
    override suspend fun recordSession(session: CookingSession) {
        recorded += session
    }
    override suspend fun mostRecent(): CookingSession? = recorded.lastOrNull()
    override suspend fun totalXp(): Int = recorded.sumOf { it.xpEarned }
    override suspend fun sessionCount(): Int = recorded.size
}

private class StoppedClock(private val instant: Instant) : Clock {
    override fun now(): Instant = instant
}

class RecordCookingSessionUseCaseTest {

    private val recipe = Recipe(
        id = "lemon-chicken",
        name = "Lemon chicken breast",
        emoji = "🍗",
        xpReward = 60,
        timeMinutes = 25,
        difficulty = Difficulty.EASY,
        caloriesKcal = 320,
        proteinGrams = 38,
        carbsGrams = 8,
        fatGrams = 12,
    )

    @Test
    fun records_a_session_stamped_with_the_clock_and_the_recipe_reward() = runTest {
        val repository = CapturingCookingSessionRepository()
        val now = Instant.parse("2026-03-04T18:30:00Z")

        RecordCookingSessionUseCase(repository, StoppedClock(now)) { "session-1" }(recipe)

        val session = repository.recorded.single()
        assertEquals("session-1", session.id)
        assertEquals("lemon-chicken", session.recipeId)
        assertEquals("Lemon chicken breast", session.recipeName)
        assertEquals(now, session.cookedAt)
        assertEquals(60, session.xpEarned)
        assertEquals(320, session.kcal)
        assertEquals(38, session.proteinGrams)
        assertEquals(8, session.carbsGrams)
        assertEquals(12, session.fatGrams)
    }

    @Test
    fun carries_null_macros_through_when_the_recipe_has_none() = runTest {
        val repository = CapturingCookingSessionRepository()

        RecordCookingSessionUseCase(repository, StoppedClock(Instant.parse("2026-03-04T00:00:00Z"))) { "s" }(
            recipe.copy(caloriesKcal = null, proteinGrams = null, carbsGrams = null, fatGrams = null),
        )

        val session = repository.recorded.single()
        assertEquals(null, session.kcal)
        assertEquals(null, session.proteinGrams)
    }

    @Test
    fun generates_a_fresh_id_per_call_by_default() = runTest {
        val repository = CapturingCookingSessionRepository()
        val useCase = RecordCookingSessionUseCase(repository, StoppedClock(Instant.parse("2026-03-04T00:00:00Z")))

        useCase(recipe)
        useCase(recipe)

        assertEquals(2, repository.recorded.map { it.id }.toSet().size)
    }
}
