@file:OptIn(ExperimentalCoroutinesApi::class, ExperimentalTime::class)

package com.levelchef.feature.onboarding

import app.cash.turbine.test
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
import com.levelchef.domain.usecase.SaveSurveyResponseUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

private class FakeSurveyRepository(initial: SurveyResponse? = null) : SurveyRepository {
    private val state = MutableStateFlow(initial)
    val saved: SurveyResponse? get() = state.value
    override fun observeResponse(): Flow<SurveyResponse?> = state
    override suspend fun save(response: SurveyResponse) {
        state.value = response
    }
}

private val storedResponse = SurveyResponse(
    completedAt = Instant.parse("2026-01-01T00:00:00Z"),
    cookingExperience = CookingExperience.PRO,
    dietaryPreference = DietaryPreference.OMNIVORE,
    allergens = emptySet(),
    cuisines = setOf(Cuisine.ITALIAN),
    spiceTolerance = SpiceTolerance.MILD,
    cookingGoal = CookingGoal.QUICK_MEALS,
    weeknightTime = WeeknightTime.UNDER_15,
    householdSize = HouseholdSize.SOLO,
)

class OnboardingViewModelTest {

    private val dispatcher = StandardTestDispatcher()

    @BeforeTest
    fun setUp() = Dispatchers.setMain(dispatcher)

    @AfterTest
    fun tearDown() = Dispatchers.resetMain()

    private fun viewModel(repository: FakeSurveyRepository = FakeSurveyRepository()) =
        OnboardingViewModel(repository, SaveSurveyResponseUseCase(repository, Clock.System))

    @Test
    fun starts_loading_then_shows_the_survey_when_nothing_is_stored() = runTest(dispatcher) {
        viewModel().uiState.test {
            assertTrue(awaitItem().loading)
            advanceUntilIdle()
            val loaded = expectMostRecentItem()
            assertFalse(loaded.loading)
            assertFalse(loaded.completed)
        }
    }

    @Test
    fun completed_is_true_when_a_response_already_exists() = runTest(dispatcher) {
        viewModel(FakeSurveyRepository(storedResponse)).uiState.test {
            advanceUntilIdle()
            assertTrue(expectMostRecentItem().completed)
        }
    }

    @Test
    fun next_only_advances_once_the_current_question_is_answered() = runTest(dispatcher) {
        val vm = viewModel()
        advanceUntilIdle()

        vm.next() // WELCOME needs no answer
        assertEquals(OnboardingStep.EXPERIENCE, vm.uiState.value.currentStep)

        vm.next() // not answered yet -> no-op
        assertEquals(OnboardingStep.EXPERIENCE, vm.uiState.value.currentStep)

        vm.selectExperience(CookingExperience.BEGINNER)
        vm.next()
        assertEquals(OnboardingStep.DIET, vm.uiState.value.currentStep)

        vm.back()
        assertEquals(OnboardingStep.EXPERIENCE, vm.uiState.value.currentStep)
        assertEquals(CookingExperience.BEGINNER, vm.uiState.value.cookingExperience)
    }

    @Test
    fun back_on_the_first_step_is_a_no_op() = runTest(dispatcher) {
        val vm = viewModel()
        advanceUntilIdle()

        vm.back()

        assertEquals(0, vm.uiState.value.stepIndex)
    }

    @Test
    fun allergen_and_cuisine_toggles_update_selections() = runTest(dispatcher) {
        val vm = viewModel()
        advanceUntilIdle()

        vm.toggleAllergen(Allergen.GLUTEN)
        assertEquals(setOf(Allergen.GLUTEN), vm.uiState.value.allergens)
        vm.toggleAllergen(Allergen.GLUTEN)
        assertTrue(vm.uiState.value.allergens.isEmpty())

        vm.setNoAllergies()
        assertTrue(vm.uiState.value.noAllergies)
        vm.toggleAllergen(Allergen.NUTS)
        assertEquals(setOf(Allergen.NUTS), vm.uiState.value.allergens)
        assertFalse(vm.uiState.value.noAllergies)

        vm.toggleCuisine(Cuisine.ITALIAN)
        vm.toggleCuisine(Cuisine.ASIAN)
        vm.toggleCuisine(Cuisine.ITALIAN)
        assertEquals(setOf(Cuisine.ASIAN), vm.uiState.value.cuisines)
    }

    @Test
    fun completing_every_step_persists_the_response_and_flips_completed() = runTest(dispatcher) {
        val repository = FakeSurveyRepository()
        val vm = viewModel(repository)
        advanceUntilIdle()

        vm.next() // leave WELCOME
        vm.selectExperience(CookingExperience.COMFORTABLE); vm.next()
        vm.selectDiet(DietaryPreference.VEGAN); vm.next()
        vm.setNoAllergies(); vm.next()
        vm.toggleCuisine(Cuisine.MEXICAN); vm.next()
        vm.selectSpice(SpiceTolerance.HOT); vm.next()
        vm.selectGoal(CookingGoal.HIGH_PROTEIN); vm.next()
        vm.selectTime(WeeknightTime.FROM_15_TO_30); vm.next()
        vm.selectHousehold(HouseholdSize.TWO)
        vm.next() // last step -> submit

        advanceUntilIdle()

        val saved = repository.saved
        assertNotNull(saved)
        assertEquals(CookingExperience.COMFORTABLE, saved.cookingExperience)
        assertEquals(DietaryPreference.VEGAN, saved.dietaryPreference)
        assertEquals(emptySet(), saved.allergens)
        assertEquals(setOf(Cuisine.MEXICAN), saved.cuisines)
        assertEquals(HouseholdSize.TWO, saved.householdSize)
        assertTrue(vm.uiState.value.completed)
    }
}
