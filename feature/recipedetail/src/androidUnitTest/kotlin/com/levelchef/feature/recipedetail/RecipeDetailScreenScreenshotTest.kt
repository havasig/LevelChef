package com.levelchef.feature.recipedetail

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import com.dropbox.differ.SimpleImageComparator
import com.github.takahirom.roborazzi.RoborazziOptions
import com.github.takahirom.roborazzi.captureRoboImage
import com.levelchef.core.ui.theme.LevelChefTheme
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.PreviewContextConfigurationEffect
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
 * Screenshot baselines for the recipe detail screen in both themes. Regenerate with
 * `./gradlew :feature:recipedetail:recordRoborazziDebug` and review the PNG diff in the PR.
 */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [34], qualifiers = "w411dp-h891dp-420dpi")
class RecipeDetailScreenScreenshotTest {

    @get:Rule
    val compose = createComposeRule()

    @OptIn(ExperimentalResourceApi::class)
    private fun captureLightAndDark(name: String, content: @Composable () -> Unit) {
        var dark by mutableStateOf(false)
        compose.setContent {
            // Compose Multiplatform resources need an Android Context that Robolectric never
            // initializes automatically (its ContentProvider is skipped) -- without this, any
            // composeResources read (LevelChefTheme's font, core:designsystem's strings) throws
            // IllegalStateException: Android context is not initialized.
            CompositionLocalProvider(LocalInspectionMode provides true) {
                PreviewContextConfigurationEffect()
            }
            LevelChefTheme(darkTheme = dark) { content() }
        }

        compose.onRoot().captureRoboImage("src/androidUnitTest/screenshots/${name}_light.png", screenshotOptions)
        dark = true
        compose.waitForIdle()
        compose.onRoot().captureRoboImage("src/androidUnitTest/screenshots/${name}_dark.png", screenshotOptions)
    }

    @Test
    fun recipe_detail_screen() = captureLightAndDark("recipe_detail_screen") {
        RecipeDetailScreen(state = sampleRecipeDetailState)
    }

    @Test
    fun recipe_detail_screen_saved() = captureLightAndDark("recipe_detail_screen_saved") {
        RecipeDetailScreen(state = sampleRecipeDetailState.copy(isSaved = true, servings = 4))
    }

    @Test
    fun recipe_detail_screen_timer_running() = captureLightAndDark("recipe_detail_screen_timer_running") {
        RecipeDetailScreen(
            state = sampleRecipeDetailState.copy(runningTimerStepIndex = 2, timerSecondsRemaining = 754),
        )
    }
}
