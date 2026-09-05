package com.levelchef.feature.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.levelchef.core.designsystem.ButtonType
import com.levelchef.core.designsystem.LevelChefButton
import com.levelchef.core.designsystem.LevelChefPreview
import com.levelchef.core.ui.theme.LevelChefTheme

/**
 * Stateless first-launch survey wizard — one question per step, a page indicator, Back / Continue.
 * Rendered full-screen by [OnboardingGate]; the app's top bar and bottom nav are not composed
 * while it is shown.
 */
@Composable
fun OnboardingScreen(
    state: OnboardingUiState = OnboardingUiState(loading = false),
    actions: OnboardingActions = OnboardingActions(),
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(LevelChefTheme.colors.background)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = 16.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        if (state.currentStep != OnboardingStep.WELCOME) {
            OnboardingHeader(
                questionNumber = state.questionNumber,
                showBack = !state.isFirstStep,
                onBack = actions.back,
            )
        }

        if (state.currentStep == OnboardingStep.WELCOME) {
            Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                OnboardingStepContent(state = state, actions = actions)
            }
        } else {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                OnboardingStepContent(state = state, actions = actions)
            }
        }

        LevelChefButton(
            label = stringResource(bottomButtonLabel(state)),
            type = ButtonType.PRIMARY,
            onClick = actions.next,
            enabled = state.canAdvance,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

private fun bottomButtonLabel(state: OnboardingUiState): Int = when {
    state.currentStep == OnboardingStep.WELCOME -> R.string.onboarding_welcome_cta
    state.isLastStep -> R.string.onboarding_finish
    else -> R.string.onboarding_continue
}

@LevelChefPreview
@Composable
private fun OnboardingScreenWelcomePreview() {
    LevelChefTheme { OnboardingScreen(state = OnboardingUiState(loading = false, stepIndex = 0)) }
}

@LevelChefPreview
@Composable
private fun OnboardingScreenQuestionPreview() {
    LevelChefTheme { OnboardingScreen(state = sampleOnboardingState) }
}
