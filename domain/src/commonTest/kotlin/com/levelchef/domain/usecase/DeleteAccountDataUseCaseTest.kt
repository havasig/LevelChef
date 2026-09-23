package com.levelchef.domain.usecase

import com.levelchef.core.model.Badge
import com.levelchef.core.model.CookingSession
import com.levelchef.core.model.Ingredient
import com.levelchef.core.model.IngredientCategory
import com.levelchef.core.model.WeeklyChallenge
import com.levelchef.domain.repository.BadgeRepository
import com.levelchef.domain.repository.CookingSessionRepository
import com.levelchef.domain.repository.SavedRecipeRepository
import com.levelchef.domain.repository.WeeklyChallengeRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.Instant
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

private class FakeCookingSessionRepository(initial: List<CookingSession> = emptyList()) : CookingSessionRepository {
    private val state = MutableStateFlow(initial)
    val stored: List<CookingSession> get() = state.value

    override fun observeAll(): Flow<List<CookingSession>> = state
    override suspend fun recordSession(session: CookingSession) {
        state.value = state.value + session
    }
    override suspend fun mostRecent(): CookingSession? = state.value.firstOrNull()
    override suspend fun totalXp(): Int = state.value.sumOf { it.xpEarned }
    override suspend fun sessionCount(): Int = state.value.size
    override suspend fun totalDurationMinutes(): Int = state.value.sumOf { it.durationMinutes }
    override suspend fun deleteAll() {
        state.value = emptyList()
    }
}

/** Records every call so the test can assert what was wiped and in which order. */
private class CallLog {
    val calls = mutableListOf<String>()
}

private class FakeSavedRecipeRepository(private val log: CallLog) : SavedRecipeRepository {
    override fun observeIsSaved(recipeId: String): Flow<Boolean> = emptyFlow()
    override fun observeSavedRecipeIds(): Flow<List<String>> = emptyFlow()
    override suspend fun setSaved(recipeId: String, saved: Boolean) = Unit
    override suspend fun deleteAll() {
        log.calls += "savedRecipes"
    }
}

private class FakeBadgeRepository(private val log: CallLog) : BadgeRepository {
    override fun observeAll(): Flow<List<Badge>> = emptyFlow()
    override suspend fun refreshEarned() = Unit
    override suspend fun deleteAll() {
        log.calls += "badges"
    }
}

private class FakeWeeklyChallengeRepository(private val log: CallLog) : WeeklyChallengeRepository {
    override fun observeCurrent(): Flow<WeeklyChallenge> = emptyFlow()
    override suspend fun complete(id: String) = Unit
    override suspend fun totalAwardedXp(): Int = 0
    override suspend fun deleteAll() {
        log.calls += "weeklyChallenges"
    }
}

class DeleteAccountDataUseCaseTest {

    @Test
    fun wipes_every_data_source_then_reseeds_the_starter_pantry() = runTest {
        val ingredientRepository = FakeIngredientRepository(
            listOf(Ingredient("apple", "Apple", IngredientCategory.FRUIT, "🍎")),
        )
        val cookingSessionRepository = FakeCookingSessionRepository(
            listOf(
                CookingSession(
                    id = "session-1",
                    recipeId = "recipe-1",
                    recipeName = "Pasta",
                    cookedAt = Instant.parse("2026-01-01T00:00:00Z"),
                    xpEarned = 50,
                ),
            ),
        )

        val log = CallLog()

        DeleteAccountDataUseCase(
            ingredientRepository,
            cookingSessionRepository,
            FakeSavedRecipeRepository(log),
            FakeBadgeRepository(log),
            FakeWeeklyChallengeRepository(log),
        )()

        assertTrue(ingredientRepository.stored.isEmpty())
        assertTrue(cookingSessionRepository.stored.isEmpty())
        assertEquals(listOf("apple"), ingredientRepository.deleted)
        assertEquals(listOf("savedRecipes", "badges", "weeklyChallenges"), log.calls)
        assertEquals(1, ingredientRepository.seededCount)
    }
}
