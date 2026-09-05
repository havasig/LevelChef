package com.levelchef.feature.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.sp
import com.levelchef.core.ui.theme.LevelChefTextStyles
import com.levelchef.core.ui.theme.LevelChefTheme
import org.koin.compose.viewmodel.koinViewModel

/**
 * Gates the whole app behind the mandatory first-launch survey: shows a splash while the stored
 * response loads, the full-screen [OnboardingScreen] until it is completed, then [content].
 */
@Composable
fun OnboardingGate(content: @Composable () -> Unit) {
    val viewModel: OnboardingViewModel = koinViewModel()
    val state by viewModel.uiState.collectAsState()

    when {
        state.loading -> OnboardingSplash()
        state.completed -> content()
        else -> OnboardingScreen(
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

@Composable
private fun OnboardingSplash() {
    Box(
        modifier = Modifier.fillMaxSize().background(LevelChefTheme.colors.background),
        contentAlignment = Alignment.Center,
    ) {
        Text("👨‍🍳", style = LevelChefTextStyles.h1.copy(fontSize = 64.sp))
    }
}
