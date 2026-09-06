package com.levelchef.feature.trophyroom

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import com.dropbox.differ.SimpleImageComparator
import com.github.takahirom.roborazzi.RoborazziOptions
import com.github.takahirom.roborazzi.captureRoboImage
import com.levelchef.core.ui.theme.LevelChefTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/**
 * Tolerance for sub-pixel antialiasing / font-hinting differences between the machine that
 * recorded the baseline and the CI runner. A real layout or colour change is far above this.
 */
private val screenshotOptions = RoborazziOptions(
    compareOptions = RoborazziOptions.CompareOptions(
        changeThreshold = 0.02f,
        imageComparator = SimpleImageComparator(maxDistance = 0.01f, hShift = 2, vShift = 2),
    ),
)

/**
 * Screenshot baselines for the Trophy Room screen, captured in both themes since the app follows
 * the system light/dark setting. Regenerate with `./gradlew :feature:trophyroom:recordRoborazziDebug`
 * and review the PNG diff in the PR.
 */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [34], qualifiers = "w411dp-h891dp-420dpi")
class TrophyRoomScreenScreenshotTest {

    @get:Rule
    val compose = createComposeRule()

    /** Renders [content] once, then captures it in light and again in dark (a single `setContent`,
     * flipping a theme flag — `ComposeContentTestRule.setContent` may only be called once). */
    private fun captureLightAndDark(name: String, content: @Composable () -> Unit) {
        var dark by mutableStateOf(false)
        compose.setContent { LevelChefTheme(darkTheme = dark) { content() } }

        compose.onRoot().captureRoboImage("src/test/screenshots/${name}_light.png", screenshotOptions)
        dark = true
        compose.waitForIdle()
        compose.onRoot().captureRoboImage("src/test/screenshots/${name}_dark.png", screenshotOptions)
    }

    @Test
    fun trophy_room_screen() = captureLightAndDark("trophy_room_screen") { TrophyRoomScreen() }

    @Test
    fun trophy_room_screen_max_level_and_completed_challenge() = captureLightAndDark("trophy_room_screen_max_level") {
        TrophyRoomScreen(
            state = TrophyRoomUiState(
                levelEmoji = "👑",
                levelName = "Legendary Tastemaker",
                levelIndex = 12,
                levelCount = 12,
                isMaxLevel = true,
                currentXp = 32000,
                xpForNextLevel = 32000,
                streakDays = 21,
                avgRatingPercent = 95,
                weeklyChallengeCompleted = true,
                kitchenTimeLabel = "48h",
                cookingSessions = 120,
                ingredientsTried = 40,
                streakBadges = sampleStreakBadges.map { it.copy(progressCurrent = it.progressTarget, earned = true) },
                badges = sampleBadges.map { it.copy(earned = true, progressCurrent = it.progressTarget) },
            ),
        )
    }
}
