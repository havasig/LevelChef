package com.levelchef.domain.usecase

import com.levelchef.core.model.ChefLevel
import com.levelchef.core.model.UserProfile
import com.levelchef.domain.repository.UserProfileRepository
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

private class FakeUserProfileRepository(private val profile: UserProfile) : UserProfileRepository {
    override suspend fun getProfile(): UserProfile = profile
}

class GetChefLevelUseCaseTest {

    @Test
    fun returns_kitchen_novice_below_first_threshold() = runTest {
        val useCase = GetChefLevelUseCase(
            FakeUserProfileRepository(UserProfile(totalXp = 0, cookingSessionsCount = 0, newIngredientsCount = 0)),
        )

        assertEquals(ChefLevel.KITCHEN_NOVICE, useCase())
    }

    @Test
    fun returns_wok_warrior_at_exact_threshold() = runTest {
        val useCase = GetChefLevelUseCase(
            FakeUserProfileRepository(UserProfile(totalXp = 800, cookingSessionsCount = 10, newIngredientsCount = 5)),
        )

        assertEquals(ChefLevel.WOK_WARRIOR, useCase())
    }

    @Test
    fun returns_michelin_contender_at_high_xp() = runTest {
        val useCase = GetChefLevelUseCase(
            FakeUserProfileRepository(UserProfile(totalXp = 9999, cookingSessionsCount = 100, newIngredientsCount = 50)),
        )

        assertEquals(ChefLevel.MICHELIN_CONTENDER, useCase())
    }
}
