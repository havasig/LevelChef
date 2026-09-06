package com.levelchef.data.repository

import com.levelchef.core.model.Ingredient
import com.levelchef.core.model.IngredientCategory
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class UserProfileRepositoryImplTest {

    private fun ingredient(id: String) = Ingredient(id, id, IngredientCategory.OTHER, "🥕")

    @Test
    fun profile_aggregates_xp_and_session_count_from_cooking_sessions() = runTest {
        val repository = UserProfileRepositoryImpl(
            FakeCookingSessionRepository(xp = 450, count = 7),
            FakeIngredientRepository(),
        )

        val profile = repository.getProfile()

        assertEquals(450, profile.totalXp)
        assertEquals(7, profile.cookingSessionsCount)
    }

    @Test
    fun new_ingredients_count_reflects_the_pantry_size() = runTest {
        val repository = UserProfileRepositoryImpl(
            FakeCookingSessionRepository(),
            FakeIngredientRepository(listOf(ingredient("a"), ingredient("b"), ingredient("c"))),
        )

        assertEquals(3, repository.getProfile().newIngredientsCount)
    }
}
