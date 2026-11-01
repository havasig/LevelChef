package com.levelchef.feature.home

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performScrollTo
import com.levelchef.core.ui.theme.LevelChefTheme
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.PreviewContextConfigurationEffect
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/** Home's recommendation area: a loading row while the first batch generates, never fake data. */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], qualifiers = "w411dp-h891dp-420dpi")
class HomeScreenUiTest {

    @get:Rule
    val compose = createComposeRule()

    @OptIn(ExperimentalResourceApi::class)
    private fun launch(state: HomeUiState) {
        compose.setContent {
            CompositionLocalProvider(LocalInspectionMode provides true) { PreviewContextConfigurationEffect() }
            LevelChefTheme(darkTheme = false) { HomeScreen(state = state) }
        }
    }

    @Test
    fun the_initial_state_shows_a_loading_row_and_no_sample_stats() {
        launch(HomeUiState.Initial)

        compose.onNodeWithText("Finding recipes for you…").performScrollTo().assertExists()
        compose.onNodeWithText("Tofu stir-fry").assertDoesNotExist()
        compose.onNodeWithText("27").assertDoesNotExist()
    }

    @Test
    fun loaded_recommendations_replace_the_loading_row() {
        val loaded = HomeUiState.Initial.copy(recommendations = sampleRecommendations, recommendationsLoading = false)
        launch(loaded)

        compose.onNodeWithText("Finding recipes for you…").assertDoesNotExist()
        compose.onNodeWithText(sampleRecommendations.first().name).performScrollTo().assertExists()
    }
}
