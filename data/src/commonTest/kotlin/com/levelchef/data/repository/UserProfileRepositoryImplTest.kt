package com.levelchef.data.repository

import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class UserProfileRepositoryImplTest {

    @Test
    fun profile_aggregates_xp_and_session_count_from_cooking_sessions() = runTest {
        val repository = UserProfileRepositoryImpl(
            FakeCookingSessionRepository(xp = 450, count = 7),
        )

        val profile = repository.getProfile()

        assertEquals(450, profile.totalXp)
        assertEquals(7, profile.cookingSessionsCount)
    }

    @Test
    fun new_ingredients_count_is_zero_until_ingredient_logging_exists() = runTest {
        val repository = UserProfileRepositoryImpl(FakeCookingSessionRepository())

        assertEquals(0, repository.getProfile().newIngredientsCount)
    }
}
