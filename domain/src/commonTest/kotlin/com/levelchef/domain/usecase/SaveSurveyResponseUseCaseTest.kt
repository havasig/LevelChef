@file:OptIn(ExperimentalTime::class)

package com.levelchef.domain.usecase

import com.levelchef.core.model.Allergen
import com.levelchef.core.model.CookingExperience
import com.levelchef.core.model.CookingGoal
import com.levelchef.core.model.Cuisine
import com.levelchef.core.model.DietaryPreference
import com.levelchef.core.model.HouseholdSize
import com.levelchef.core.model.SpiceTolerance
import com.levelchef.core.model.SurveyAnswers
import com.levelchef.core.model.SurveyResponse
import com.levelchef.core.model.WeeknightTime
import com.levelchef.domain.repository.SurveyRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

private class FakeSurveyRepository : SurveyRepository {
    var saved: SurveyResponse? = null
    override fun observeResponse(): Flow<SurveyResponse?> = flowOf(saved)
    override suspend fun save(response: SurveyResponse) {
        saved = response
    }
}

private class FixedClock(private val instant: Instant) : Clock {
    override fun now(): Instant = instant
}

class SaveSurveyResponseUseCaseTest {

    private val answers = SurveyAnswers(
        cookingExperience = CookingExperience.COMFORTABLE,
        dietaryPreference = DietaryPreference.PESCATARIAN,
        allergens = setOf(Allergen.GLUTEN, Allergen.DAIRY),
        cuisines = setOf(Cuisine.ITALIAN, Cuisine.INDIAN),
        spiceTolerance = SpiceTolerance.HOT,
        cookingGoal = CookingGoal.HIGH_PROTEIN,
        weeknightTime = WeeknightTime.FROM_15_TO_30,
        householdSize = HouseholdSize.TWO,
    )

    @Test
    fun stamps_completion_time_and_persists_every_answer() = runTest {
        val repository = FakeSurveyRepository()
        val now = Instant.parse("2026-02-01T12:00:00Z")

        SaveSurveyResponseUseCase(repository, FixedClock(now))(answers)

        val saved = repository.saved!!
        assertEquals(now, saved.completedAt)
        assertEquals(answers.cookingExperience, saved.cookingExperience)
        assertEquals(answers.dietaryPreference, saved.dietaryPreference)
        assertEquals(answers.allergens, saved.allergens)
        assertEquals(answers.cuisines, saved.cuisines)
        assertEquals(answers.spiceTolerance, saved.spiceTolerance)
        assertEquals(answers.cookingGoal, saved.cookingGoal)
        assertEquals(answers.weeknightTime, saved.weeknightTime)
        assertEquals(answers.householdSize, saved.householdSize)
    }

    @Test
    fun keeps_an_empty_allergen_set_empty() = runTest {
        val repository = FakeSurveyRepository()

        SaveSurveyResponseUseCase(repository, FixedClock(Instant.parse("2026-02-01T00:00:00Z")))(
            answers.copy(allergens = emptySet()),
        )

        assertEquals(emptySet(), repository.saved!!.allergens)
    }
}
