@file:OptIn(ExperimentalTime::class)

package com.levelchef.feature.onboarding.di

import com.levelchef.domain.usecase.SaveSurveyResponseUseCase
import com.levelchef.feature.onboarding.OnboardingViewModel
import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val onboardingModule = module {
    factory { SaveSurveyResponseUseCase(get(), Clock.System) }
    viewModel { OnboardingViewModel(get(), get()) }
}
