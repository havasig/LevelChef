package com.levelchef.feature.home

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import com.github.takahirom.roborazzi.captureRoboImage
import com.levelchef.core.ui.theme.LevelChefTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/**
 * Screenshot baselines for the Home screen. Regenerate with
 * `./gradlew :feature:home:recordRoborazziDebug` and review the PNG diff in the PR.
 */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [34], qualifiers = "w411dp-h891dp-420dpi")
class HomeScreenScreenshotTest {

    @get:Rule
    val compose = createComposeRule()

    @Test
    fun home_screen() {
        compose.setContent {
            LevelChefTheme { HomeScreen() }
        }
        compose.onRoot().captureRoboImage()
    }

    @Test
    fun home_screen_new_cook() {
        compose.setContent {
            LevelChefTheme {
                HomeScreen(
                    state = HomeUiState(
                        levelLabel = "Kitchen Novice · Level 1",
                        currentXp = 20,
                        xpForNextLevel = 300,
                        cookingSessions = 1,
                        ingredientsTried = 0,
                        challengeInProgress = false,
                    ),
                )
            }
        }
        compose.onRoot().captureRoboImage()
    }
}
