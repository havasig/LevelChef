package com.levelchef.feature.ingredients

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextInput
import com.levelchef.core.ui.theme.LevelChefTheme
import com.levelchef.domain.usecase.SaveIngredientUseCase
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.PreviewContextConfigurationEffect
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.test.assertEquals

/**
 * Interaction tests for the add-ingredient form through the real [IngredientFormViewModel]:
 * typed values are capped as you type (an overlong calorie value used to be saved as 0), and one
 * Save tap adds exactly one ingredient.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], qualifiers = "w411dp-h891dp-420dpi")
class IngredientFormUiTest {

    @get:Rule
    val compose = createComposeRule()

    private val repository = FakeIngredientRepository()

    @OptIn(ExperimentalResourceApi::class)
    private fun launch() {
        val viewModel = IngredientFormViewModel(null, repository, SaveIngredientUseCase(repository))
        compose.setContent {
            CompositionLocalProvider(LocalInspectionMode provides true) { PreviewContextConfigurationEffect() }
            val state by viewModel.uiState.collectAsState()
            LevelChefTheme(darkTheme = false) {
                IngredientFormScreen(
                    state = state,
                    actions = IngredientFormActions(
                        onNameChange = viewModel::setName,
                        onCaloriesChange = viewModel::setCalories,
                        onProteinChange = viewModel::setProtein,
                        onCarbsChange = viewModel::setCarbs,
                        onFatChange = viewModel::setFat,
                        onSave = viewModel::save,
                    ),
                )
            }
        }
    }

    // Field order on screen: name, calories, protein, carbs, fat.
    private fun field(index: Int) = compose.onAllNodes(hasSetTextAction())[index]

    private fun save() = compose.onNode(hasText("Save"))

    @Test
    fun save_needs_a_name() {
        launch()

        save().performScrollTo().assertIsNotEnabled()
        field(0).performTextInput("Tofu")
        save().performScrollTo().assertIsEnabled()
    }

    @Test
    fun numeric_fields_cap_what_can_be_typed_and_save_stores_the_capped_values() {
        launch()
        field(0).performTextInput("Butter")
        field(1).performScrollTo().performTextInput("99999999999")
        field(4).performScrollTo().performTextInput("81,456")

        compose.onNode(hasSetTextAction() and hasContentDescription("Calories (kcal)")).assertTextEquals("9999")
        save().performScrollTo().performClick()
        compose.waitForIdle()

        val macros = repository.stored.single().macros!!
        assertEquals(9999, macros.calories)
        assertEquals(81.45, macros.fatGrams)
    }

    @Test
    fun a_double_tap_on_save_adds_one_ingredient() {
        launch()
        field(0).performTextInput("Tofu")

        save().performScrollTo().performClick()
        runCatching { save().performClick() }
        compose.waitForIdle()

        assertEquals(1, repository.stored.size)
    }
}
