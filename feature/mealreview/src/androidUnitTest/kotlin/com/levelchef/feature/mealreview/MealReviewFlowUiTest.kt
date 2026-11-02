package com.levelchef.feature.mealreview

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.test.assertHeightIsAtLeast
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertIsNotSelected
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.assertWidthIsAtLeast
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.unit.dp
import com.levelchef.core.model.Difficulty
import com.levelchef.core.model.Recipe
import com.levelchef.core.model.RecipeStep
import com.levelchef.core.ui.theme.LevelChefTheme
import com.levelchef.domain.usecase.RecordCookingSessionUseCase
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.PreviewContextConfigurationEffect
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Interaction tests for "Log experience", driven through the real [MealReviewViewModel]: Save is
 * gated on a rating (with a visible reason), stars are accessible radio buttons with a 48dp target,
 * and Save records the cook exactly once.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], qualifiers = "w411dp-h891dp-420dpi")
class MealReviewFlowUiTest {

    @get:Rule
    val compose = createComposeRule()

    private val sessions = RecordingCookingSessionRepository()
    private var savedSignals = 0

    private val recipe = Recipe(
        id = "stew",
        name = "Chickpea stew",
        emoji = "🥘",
        xpReward = 60,
        timeMinutes = 30,
        difficulty = Difficulty.EASY,
        steps = listOf(RecipeStep("Simmer.")),
    )

    @OptIn(ExperimentalResourceApi::class)
    private fun launch() {
        val viewModel = MealReviewViewModel(
            recipeId = recipe.id,
            recipeRepository = FakeRecipeRepository(listOf(recipe)),
            recordCookingSession = RecordCookingSessionUseCase(sessions),
        )
        compose.setContent {
            CompositionLocalProvider(LocalInspectionMode provides true) { PreviewContextConfigurationEffect() }
            val state by viewModel.uiState.collectAsState()
            if (state.saved) savedSignals++
            LevelChefTheme(darkTheme = false) {
                MealReviewScreen(
                    state = state,
                    actions = MealReviewActions(
                        onRatingChange = viewModel::changeRating,
                        onSaveClick = viewModel::save,
                    ),
                )
            }
        }
        compose.waitForIdle()
    }

    private fun star(count: Int) =
        compose.onNode(hasContentDescription(if (count == 1) "Rate 1 star" else "Rate $count stars"), useUnmergedTree = true)

    private fun saveButton() = compose.onNode(hasText("Save"))

    @Test
    fun save_is_disabled_with_a_visible_reason_until_the_meal_is_rated() {
        launch()

        saveButton().performScrollTo().assertIsNotEnabled()
        compose.onNodeWithText("Rate your meal above to save it.").assertExists()

        compose.onNode(hasContentDescription("Rate 4 stars"), useUnmergedTree = true).performScrollTo()
        compose.onNode(hasContentDescription("Rate 4 stars")).performClick()

        saveButton().performScrollTo().assertIsEnabled()
        compose.onNodeWithText("Rate your meal above to save it.").assertDoesNotExist()
    }

    @Test
    fun stars_are_announced_as_a_radio_group_with_full_size_touch_targets() {
        launch()

        compose.onNode(hasContentDescription("Rate 3 stars")).performClick()

        compose.onNode(hasContentDescription("Rate 3 stars")).assertIsSelected()
        compose.onNode(hasContentDescription("Rate 2 stars")).assertIsNotSelected()
        compose.onNode(hasContentDescription("Rate 3 stars")).assertWidthIsAtLeast(48.dp).assertHeightIsAtLeast(48.dp)
        star(1).assertExists()
    }

    @Test
    fun double_tapping_save_logs_the_cook_once() {
        launch()
        compose.onNode(hasContentDescription("Rate 5 stars")).performClick()

        saveButton().performScrollTo()
        saveButton().performClick()
        // A second tap that lands before navigation pops the screen.
        runCatching { saveButton().performClick() }
        compose.waitForIdle()

        assertEquals(1, sessions.recorded.size)
        assertEquals(5, sessions.recorded.single().rating)
        assertTrue(savedSignals > 0)
    }
}
