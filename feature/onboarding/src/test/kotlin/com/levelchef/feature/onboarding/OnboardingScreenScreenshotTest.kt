package com.levelchef.feature.onboarding

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import com.dropbox.differ.SimpleImageComparator
import com.github.takahirom.roborazzi.RoborazziOptions
import com.github.takahirom.roborazzi.captureRoboImage
import com.levelchef.core.model.CookingExperience
import com.levelchef.core.model.Cuisine
import com.levelchef.core.model.HouseholdSize
import com.levelchef.core.ui.theme.LevelChefTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

private val screenshotOptions = RoborazziOptions(
    compareOptions = RoborazziOptions.CompareOptions(
        changeThreshold = 0.02f,
        imageComparator = SimpleImageComparator(maxDistance = 0.01f, hShift = 2, vShift = 2),
    ),
)

/**
 * Screenshot baselines for the first-launch survey, captured in both themes since the app follows
 * the system light/dark setting. Regenerate with
 * `./gradlew :feature:onboarding:recordRoborazziDebug` and review the PNG diff in the PR.
 */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [34], qualifiers = "w411dp-h891dp-420dpi")
class OnboardingScreenScreenshotTest {

    @get:Rule
    val compose = createComposeRule()

    private fun captureLightAndDark(name: String, content: @Composable () -> Unit) {
        var dark by mutableStateOf(false)
        compose.setContent { LevelChefTheme(darkTheme = dark) { content() } }

        compose.onRoot().captureRoboImage("src/test/screenshots/${name}_light.png", screenshotOptions)
        dark = true
        compose.waitForIdle()
        compose.onRoot().captureRoboImage("src/test/screenshots/${name}_dark.png", screenshotOptions)
    }

    @Test
    fun onboarding_welcome() = captureLightAndDark("onboarding_welcome") {
        OnboardingScreen(state = OnboardingUiState(loading = false, stepIndex = OnboardingStep.WELCOME.ordinal))
    }

    @Test
    fun onboarding_single_choice_step() = captureLightAndDark("onboarding_single_choice_step") {
        OnboardingScreen(
            state = OnboardingUiState(
                loading = false,
                stepIndex = OnboardingStep.EXPERIENCE.ordinal,
                cookingExperience = CookingExperience.COMFORTABLE,
            ),
        )
    }

    @Test
    fun onboarding_multi_choice_step() = captureLightAndDark("onboarding_multi_choice_step") {
        OnboardingScreen(
            state = OnboardingUiState(
                loading = false,
                stepIndex = OnboardingStep.CUISINES.ordinal,
                cuisines = setOf(Cuisine.ITALIAN, Cuisine.ASIAN),
            ),
        )
    }

    @Test
    fun onboarding_final_step() = captureLightAndDark("onboarding_final_step") {
        OnboardingScreen(
            state = OnboardingUiState(
                loading = false,
                stepIndex = OnboardingStep.HOUSEHOLD.ordinal,
                householdSize = HouseholdSize.THREE_TO_FOUR,
            ),
        )
    }
}
