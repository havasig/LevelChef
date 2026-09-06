@file:OptIn(ExperimentalCoroutinesApi::class, ExperimentalTime::class)

package com.levelchef.feature.settings

import com.levelchef.core.model.Allergen
import com.levelchef.core.model.CookingExperience
import com.levelchef.core.model.CookingGoal
import com.levelchef.core.model.CookingSession
import com.levelchef.core.model.Cuisine
import com.levelchef.core.model.DietaryPreference
import com.levelchef.core.model.HouseholdSize
import com.levelchef.core.model.Ingredient
import com.levelchef.core.model.IngredientCategory
import com.levelchef.core.model.SpiceTolerance
import com.levelchef.core.model.SurveyResponse
import com.levelchef.core.model.WeeknightTime
import com.levelchef.domain.repository.CookingSessionRepository
import com.levelchef.domain.repository.IngredientRepository
import com.levelchef.domain.repository.SurveyRepository
import com.levelchef.domain.usecase.ClearSurveyResponseUseCase
import com.levelchef.domain.usecase.DeleteAccountDataUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue
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
    override fun resetToDefaults() {
        theme = ThemeMode.SYSTEM
        lang = AppLanguage.SYSTEM
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

private class FakeIngredientRepository(initial: List<Ingredient> = emptyList()) : IngredientRepository {
    private val state = MutableStateFlow(initial)
    val stored: List<Ingredient> get() = state.value
    override fun observeAll(): Flow<List<Ingredient>> = state
    override suspend fun getById(id: String): Ingredient? = state.value.firstOrNull { it.id == id }
    override suspend fun save(ingredient: Ingredient) {
        state.value = state.value.filterNot { it.id == ingredient.id } + ingredient
    }
    override suspend fun delete(id: String) {
        state.value = state.value.filterNot { it.id == id }
    }
    override suspend fun deleteAll() {
        state.value = emptyList()
    }
    override suspend fun count(): Int = state.value.size
    override suspend fun seedDefaults() = Unit
}

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
        surveyRepository: FakeSurveyRepository = FakeSurveyRepository(storedResponse),
        ingredientRepository: FakeIngredientRepository = FakeIngredientRepository(),
        cookingSessionRepository: FakeCookingSessionRepository = FakeCookingSessionRepository(),
    ) = SettingsViewModel(
        controller,
        ClearSurveyResponseUseCase(surveyRepository),
        surveyRepository,
        DeleteAccountDataUseCase(ingredientRepository, cookingSessionRepository),
    )

    @Test
    fun initial_state_reflects_the_controller_and_the_stored_survey_response() = runTest(dispatcher) {
        val vm = viewModel(FakeAppSettingsController(ThemeMode.DARK, AppLanguage.HUNGARIAN))
        advanceUntilIdle()

        val state = vm.uiState.value
        assertEquals(ThemeMode.DARK, state.themeMode)
        assertEquals(AppLanguage.HUNGARIAN, state.language)
        assertEquals(DietaryPreference.OMNIVORE, state.dietaryPreference)
        assertEquals(CookingExperience.PRO, state.cookingExperience)
        assertEquals(HouseholdSize.SOLO, state.householdSize)
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
    fun set_dietary_preference_saves_only_that_field_on_the_survey_response() = runTest(dispatcher) {
        val surveyRepository = FakeSurveyRepository(storedResponse)
        val vm = viewModel(surveyRepository = surveyRepository)
        advanceUntilIdle()

        vm.setDietaryPreference(DietaryPreference.VEGAN)
        advanceUntilIdle()

        assertEquals(DietaryPreference.VEGAN, surveyRepository.stored?.dietaryPreference)
        assertEquals(storedResponse.cookingExperience, surveyRepository.stored?.cookingExperience)
    }

    @Test
    fun set_cooking_experience_saves_only_that_field_on_the_survey_response() = runTest(dispatcher) {
        val surveyRepository = FakeSurveyRepository(storedResponse)
        val vm = viewModel(surveyRepository = surveyRepository)
        advanceUntilIdle()

        vm.setCookingExperience(CookingExperience.BEGINNER)
        advanceUntilIdle()

        assertEquals(CookingExperience.BEGINNER, surveyRepository.stored?.cookingExperience)
        assertEquals(storedResponse.dietaryPreference, surveyRepository.stored?.dietaryPreference)
    }

    @Test
    fun set_household_size_saves_only_that_field_on_the_survey_response() = runTest(dispatcher) {
        val surveyRepository = FakeSurveyRepository(storedResponse)
        val vm = viewModel(surveyRepository = surveyRepository)
        advanceUntilIdle()

        vm.setHouseholdSize(HouseholdSize.FIVE_PLUS)
        advanceUntilIdle()

        assertEquals(HouseholdSize.FIVE_PLUS, surveyRepository.stored?.householdSize)
    }

    @Test
    fun submit_feedback_shows_then_clears_a_snackbar_message() = runTest(dispatcher) {
        val vm = viewModel()

        vm.submitFeedback("Great app!")
        runCurrent()
        assertEquals(SettingsSnackbarMessage.FEEDBACK_SENT, vm.uiState.value.snackbarMessage)

        advanceUntilIdle()
        assertNull(vm.uiState.value.snackbarMessage)
    }

    @Test
    fun submit_feedback_is_a_no_op_for_blank_text() = runTest(dispatcher) {
        val vm = viewModel()

        vm.submitFeedback("   ")
        advanceUntilIdle()

        assertNull(vm.uiState.value.snackbarMessage)
    }

    @Test
    fun delete_account_wipes_ingredients_sessions_and_survey_and_resets_settings() = runTest(dispatcher) {
        val controller = FakeAppSettingsController(ThemeMode.DARK, AppLanguage.HUNGARIAN)
        val surveyRepository = FakeSurveyRepository(storedResponse)
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
        val vm = viewModel(controller, surveyRepository, ingredientRepository, cookingSessionRepository)

        vm.deleteAccount()
        advanceUntilIdle()

        assertTrue(ingredientRepository.stored.isEmpty())
        assertTrue(cookingSessionRepository.stored.isEmpty())
        assertNull(surveyRepository.stored)
        assertEquals(ThemeMode.SYSTEM, controller.themeMode())
        assertEquals(AppLanguage.SYSTEM, controller.language())
        assertEquals(SettingsSnackbarMessage.ACCOUNT_DELETION_SUCCESS, vm.uiState.value.snackbarMessage)
    }

    @Test
    fun clear_onboarding_wipes_the_stored_survey_response() = runTest(dispatcher) {
        val surveyRepository = FakeSurveyRepository(storedResponse)
        val vm = viewModel(surveyRepository = surveyRepository)

        vm.clearOnboarding()
        advanceUntilIdle()

        assertNull(surveyRepository.stored)
    }
}
