package com.levelchef.feature.onboarding

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertIsNotSelected
import androidx.compose.ui.test.assertIsOff
import androidx.compose.ui.test.assertIsOn
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import com.levelchef.core.model.Allergen
import com.levelchef.core.model.CookingExperience
import com.levelchef.core.model.Cuisine
import com.levelchef.core.model.DietaryPreference
import com.levelchef.core.model.HouseholdSize
import com.levelchef.core.model.SurveyResponse
import com.levelchef.core.ui.theme.LevelChefTheme
import com.levelchef.domain.repository.SurveyRepository
import com.levelchef.domain.usecase.SaveSurveyResponseUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.PreviewContextConfigurationEffect
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

private class InMemorySurveyRepository : SurveyRepository {
    private val state = MutableStateFlow<SurveyResponse?>(null)
    val stored: SurveyResponse? get() = state.value
    override fun observeResponse(): Flow<SurveyResponse?> = state
    override suspend fun save(response: SurveyResponse) {
        state.value = response
    }
    override suspend fun clear() {
        state.value = null
    }
}

/**
 * Interaction tests for the mandatory first-launch survey, driven through the real
 * [OnboardingViewModel] (only the repository is faked): gating of Continue, the selected state
 * that accessibility services read, and that finishing the wizard stores exactly what was picked.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], qualifiers = "w411dp-h891dp-420dpi")
class OnboardingFlowUiTest {

    @get:Rule
    val compose = createComposeRule()

    private val repository = InMemorySurveyRepository()

    @OptIn(ExperimentalResourceApi::class)
    private fun launch() {
        val viewModel = OnboardingViewModel(repository, SaveSurveyResponseUseCase(repository))
        compose.setContent {
            // Compose Multiplatform resources need a Context Robolectric doesn't initialize itself.
            CompositionLocalProvider(LocalInspectionMode provides true) { PreviewContextConfigurationEffect() }
            val state by viewModel.uiState.collectAsState()
            LevelChefTheme(darkTheme = false) {
                OnboardingScreen(
                    state = state,
                    actions = OnboardingActions(
                        selectExperience = viewModel::selectExperience,
                        selectDiet = viewModel::selectDiet,
                        toggleAllergen = viewModel::toggleAllergen,
                        noAllergies = viewModel::setNoAllergies,
                        toggleCuisine = viewModel::toggleCuisine,
                        selectSpice = viewModel::selectSpice,
                        selectGoal = viewModel::selectGoal,
                        selectTime = viewModel::selectTime,
                        selectHousehold = viewModel::selectHousehold,
                        back = viewModel::back,
                        next = viewModel::next,
                    ),
                )
            }
        }
    }

    /** Taps [text], scrolling to it first when it sits in a scrollable list (buttons don't). */
    private fun tap(text: String) {
        val node = compose.onNodeWithText(text)
        runCatching { node.performScrollTo() }
        node.performClick()
    }

    @Test
    fun continue_stays_disabled_until_an_option_is_picked_and_the_pick_is_announced_as_selected() {
        launch()
        tap("Get started")

        compose.onNodeWithText("Continue").assertIsNotEnabled()
        compose.onNodeWithText("Beginner").assertIsNotSelected()

        tap("Beginner")

        compose.onNodeWithText("Beginner").assertIsSelected()
        compose.onNodeWithText("Pro").assertIsNotSelected()
        compose.onNodeWithText("Continue").assertIsEnabled()
    }

    @Test
    fun multi_select_allergens_are_checkboxes_and_no_allergies_clears_them() {
        launch()
        tap("Get started")
        tap("Beginner")
        tap("Continue")
        tap("Vegan")
        tap("Continue")

        tap("Gluten")
        tap("Nuts")
        compose.onNodeWithText("Gluten").assertIsOn()
        compose.onNodeWithText("Nuts").assertIsOn()

        tap("No allergies")
        compose.onNodeWithText("Gluten").assertIsOff()
        compose.onNodeWithText("Nuts").assertIsOff()
    }

    @Test
    fun back_keeps_the_earlier_answer() {
        launch()
        tap("Get started")
        tap("Confident")
        tap("Continue")

        compose.onNodeWithText("How do you eat?").assertExists()
        compose.onNodeWithContentDescription("Back").performClick()

        compose.onNodeWithText("Confident").assertIsSelected()
    }

    @Test
    fun finishing_the_wizard_stores_exactly_the_picked_answers() {
        launch()
        tap("Get started")
        tap("Beginner")
        tap("Continue")
        tap("Vegan")
        tap("Continue")
        tap("Gluten")
        tap("Nuts")
        tap("Continue")
        tap("Italian")
        tap("Continue")
        tap("Mild")
        tap("Continue")
        tap("Quick meals")
        tap("Continue")
        tap("15–30 minutes")
        tap("Continue")
        assertNull(repository.stored)
        tap("Two")
        tap("Let's cook")
        compose.waitForIdle()

        val stored = assertNotNull(repository.stored)
        assertEquals(CookingExperience.BEGINNER, stored.cookingExperience)
        assertEquals(DietaryPreference.VEGAN, stored.dietaryPreference)
        assertEquals(setOf(Allergen.GLUTEN, Allergen.NUTS), stored.allergens)
        assertEquals(setOf(Cuisine.ITALIAN), stored.cuisines)
        assertEquals(HouseholdSize.TWO, stored.householdSize)
    }
}
