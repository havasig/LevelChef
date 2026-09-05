package com.levelchef.domain.usecase

import com.levelchef.domain.repository.SurveyRepository

/**
 * Wipes the stored survey response. Because `OnboardingGate` observes the response, clearing it
 * makes the mandatory onboarding survey appear again — this backs both "retake the survey" and the
 * developer "clear onboarding storage" option in Settings.
 */
class ClearSurveyResponseUseCase(private val surveyRepository: SurveyRepository) {
    suspend operator fun invoke() = surveyRepository.clear()
}
