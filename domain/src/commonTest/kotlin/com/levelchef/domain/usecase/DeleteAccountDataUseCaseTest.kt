package com.levelchef.domain.usecase

import com.levelchef.core.model.CookingSession
import com.levelchef.core.model.Ingredient
import com.levelchef.core.model.IngredientCategory
import com.levelchef.domain.repository.CookingSessionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
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
    override suspend fun deleteAll() {
        state.value = emptyList()
    }
}

class DeleteAccountDataUseCaseTest {

    @Test
    fun wipes_every_ingredient_and_cooking_session() = runTest {
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

        DeleteAccountDataUseCase(ingredientRepository, cookingSessionRepository)()

        assertTrue(ingredientRepository.stored.isEmpty())
        assertTrue(cookingSessionRepository.stored.isEmpty())
        assertEquals(listOf("apple"), ingredientRepository.deleted)
    }
}
