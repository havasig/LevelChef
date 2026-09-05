@file:OptIn(ExperimentalCoroutinesApi::class, ExperimentalTime::class)

package com.levelchef.feature.settings

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
import com.levelchef.domain.usecase.ClearSurveyResponseUseCase
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
import kotlin.test.assertNull
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

private class FakeAppSettingsController(
    private var theme: ThemeMode = ThemeMode.SYSTEM,
    private var lang: AppLanguage = AppLanguage.SYSTEM,
) : AppSettingsController {
    override fun themeMode() = theme
    override fun setThemeMode(mode: ThemeMode) {
        theme = mode
    }
    override fun applyPersistedThemeMode() = Unit
    override fun language() = lang
    override fun setLanguage(language: AppLanguage) {
        lang = language
    }
}

private class FakeSurveyRepository(initial: SurveyResponse? = null) : SurveyRepository {
    private val state = MutableStateFlow(initial)
    val stored: SurveyResponse? get() = state.value
    override fun observeResponse(): Flow<SurveyResponse?> = state
    override suspend fun save(response: SurveyResponse) {
        state.value = response
    }
    override suspend fun clear() {
        state.value = null
    }
}

private val storedResponse = SurveyResponse(
    completedAt = Instant.parse("2026-01-01T00:00:00Z"),
    cookingExperience = CookingExperience.PRO,
    dietaryPreference = DietaryPreference.OMNIVORE,
    allergens = setOf(Allergen.DAIRY),
    cuisines = setOf(Cuisine.ITALIAN),
    spiceTolerance = SpiceTolerance.MILD,
    cookingGoal = CookingGoal.QUICK_MEALS,
    weeknightTime = WeeknightTime.UNDER_15,
    householdSize = HouseholdSize.SOLO,
)

class SettingsViewModelTest {

    private val dispatcher = StandardTestDispatcher()

    @BeforeTest
    fun setUp() = Dispatchers.setMain(dispatcher)

    @AfterTest
    fun tearDown() = Dispatchers.resetMain()

    private fun viewModel(
        controller: AppSettingsController = FakeAppSettingsController(),
        repository: FakeSurveyRepository = FakeSurveyRepository(),
    ) = SettingsViewModel(controller, ClearSurveyResponseUseCase(repository))

    @Test
    fun initial_state_reflects_the_controller() = runTest(dispatcher) {
        val vm = viewModel(FakeAppSettingsController(ThemeMode.DARK, AppLanguage.HUNGARIAN))

        vm.uiState.test {
            val state = awaitItem()
            assertEquals(ThemeMode.DARK, state.themeMode)
            assertEquals(AppLanguage.HUNGARIAN, state.language)
        }
    }

    @Test
    fun set_theme_mode_updates_state_and_controller() = runTest(dispatcher) {
        val controller = FakeAppSettingsController()
        val vm = viewModel(controller)

        vm.setThemeMode(ThemeMode.LIGHT)

        assertEquals(ThemeMode.LIGHT, vm.uiState.value.themeMode)
        assertEquals(ThemeMode.LIGHT, controller.themeMode())
    }

    @Test
    fun set_language_updates_state_and_controller() = runTest(dispatcher) {
        val controller = FakeAppSettingsController()
        val vm = viewModel(controller)

        vm.setLanguage(AppLanguage.HUNGARIAN)

        assertEquals(AppLanguage.HUNGARIAN, vm.uiState.value.language)
        assertEquals(AppLanguage.HUNGARIAN, controller.language())
    }

    @Test
    fun clear_onboarding_wipes_the_stored_survey_response() = runTest(dispatcher) {
        val repository = FakeSurveyRepository(storedResponse)
        val vm = viewModel(repository = repository)

        vm.clearOnboarding()
        advanceUntilIdle()

        assertNull(repository.stored)
    }
}
