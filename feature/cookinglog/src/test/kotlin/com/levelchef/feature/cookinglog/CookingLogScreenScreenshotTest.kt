package com.levelchef.feature.cookinglog

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

private val screenshotOptions = RoborazziOptions(
    compareOptions = RoborazziOptions.CompareOptions(
        changeThreshold = 0.02f,
        imageComparator = SimpleImageComparator(maxDistance = 0.01f, hShift = 2, vShift = 2),
    ),
)

/**
 * Screenshot baselines for the Recipes tab (Cooking Log / "My saved recipes") in both themes.
 * Regenerate with `./gradlew :feature:cookinglog:recordRoborazziDebug` and review the PNG diff in the PR.
 */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [34], qualifiers = "w411dp-h891dp-420dpi")
class CookingLogScreenScreenshotTest {

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
    fun cooking_log_screen() = captureLightAndDark("cooking_log_screen") {
        CookingLogScreen(state = sampleCookingLogState)
    }

    @Test
    fun cooking_log_screen_empty() = captureLightAndDark("cooking_log_screen_empty") {
        CookingLogScreen(state = sampleCookingLogState.copy(allItems = emptyList()))
    }

    @Test
    fun cooking_log_screen_no_search_match() = captureLightAndDark("cooking_log_screen_no_search_match") {
        CookingLogScreen(state = sampleCookingLogState.copy(query = "zzz"))
    }
}
