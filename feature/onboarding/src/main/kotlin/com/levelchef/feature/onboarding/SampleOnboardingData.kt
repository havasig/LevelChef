package com.levelchef.feature.onboarding

import com.levelchef.core.model.CookingExperience

/** A mid-survey [OnboardingUiState] for `@Preview`s and screenshot baselines. */
internal val sampleOnboardingState = OnboardingUiState(
    loading = false,
    stepIndex = OnboardingStep.EXPERIENCE.ordinal,
    cookingExperience = CookingExperience.COMFORTABLE,
)
