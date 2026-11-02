package com.levelchef.feature.settings

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import com.levelchef.core.ui.theme.LevelChefTheme
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.PreviewContextConfigurationEffect
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.test.assertEquals

/** Interaction tests for Settings: the release-visible "Retake the survey" entry and the
 * delete-account confirmation dialog. */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], qualifiers = "w411dp-h891dp-420dpi")
class SettingsScreenUiTest {

    @get:Rule
    val compose = createComposeRule()

    private var retakes = 0
    private var deletions = 0

    @OptIn(ExperimentalResourceApi::class)
    private fun launch(showDeveloperSection: Boolean = false) {
        compose.setContent {
            CompositionLocalProvider(LocalInspectionMode provides true) { PreviewContextConfigurationEffect() }
            LevelChefTheme(darkTheme = false) {
                SettingsScreen(
                    state = SettingsUiState(showDeveloperSection = showDeveloperSection),
                    actions = SettingsActions(
                        onRetakeSurvey = { retakes++ },
                        onDeleteAccount = { deletions++ },
                    ),
                )
            }
        }
    }

    @Test
    fun retake_the_survey_is_available_without_developer_options() {
        launch(showDeveloperSection = false)

        compose.onNodeWithText("Clear onboarding storage").assertDoesNotExist()
        compose.onNodeWithText("Retake the survey").performScrollTo().performClick()

        assertEquals(1, retakes)
    }

    @Test
    fun delete_account_asks_for_confirmation_and_cancel_deletes_nothing() {
        launch()

        compose.onNode(hasText("Delete account") and hasClickAction()).performScrollTo().performClick()
        compose.onNodeWithText("Cancel").performClick()

        assertEquals(0, deletions)
        compose.onNodeWithText("Delete").assertDoesNotExist()
    }

    @Test
    fun confirming_the_dialog_deletes_the_account() {
        launch()

        compose.onNode(hasText("Delete account") and hasClickAction()).performScrollTo().performClick()
        compose.onNodeWithText("Delete").performClick()

        assertEquals(1, deletions)
    }
}
