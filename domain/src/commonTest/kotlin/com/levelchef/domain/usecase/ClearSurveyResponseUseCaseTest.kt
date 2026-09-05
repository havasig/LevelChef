@file:OptIn(ExperimentalTime::class)

package com.levelchef.domain.usecase

import com.levelchef.core.model.Allergen
import com.levelchef.core.model.CookingExperience
import com.levelchef.core.model.CookingGoal
import com.levelchef.core.model.Cuisine
import com.levelchef.core.model.DietaryPreference
import com.levelchef.core.model.HouseholdSize
import com.levelchef.core.model.SpiceTolerance
import com.levelchef.core.model.SurveyResponse
import com.levelchef.core.model.WeeknightTime
import com.levelchef.domain.repository.SurveyRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertNull
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

private class InMemorySurveyRepository(var saved: SurveyResponse?) : SurveyRepository {
    override fun observeResponse(): Flow<SurveyResponse?> = flowOf(saved)
    override suspend fun save(response: SurveyResponse) {
        saved = response
    }
    override suspend fun clear() {
        saved = null
    }
}

class ClearSurveyResponseUseCaseTest {

    private val storedResponse = SurveyResponse(
        completedAt = Instant.parse("2026-02-01T12:00:00Z"),
        cookingExperience = CookingExperience.CONFIDENT,
        dietaryPreference = DietaryPreference.VEGETARIAN,
        allergens = setOf(Allergen.NUTS),
        cuisines = setOf(Cuisine.ASIAN),
        spiceTolerance = SpiceTolerance.MEDIUM,
        cookingGoal = CookingGoal.MORE_VARIETY,
        weeknightTime = WeeknightTime.FROM_30_TO_60,
        householdSize = HouseholdSize.THREE_TO_FOUR,
    )

    @Test
    fun clears_the_stored_response() = runTest {
        val repository = InMemorySurveyRepository(storedResponse)

        ClearSurveyResponseUseCase(repository)()

        assertNull(repository.saved)
    }

    @Test
    fun is_a_no_op_when_nothing_is_stored() = runTest {
        val repository = InMemorySurveyRepository(null)

        ClearSurveyResponseUseCase(repository)()

        assertNull(repository.saved)
    }
}
