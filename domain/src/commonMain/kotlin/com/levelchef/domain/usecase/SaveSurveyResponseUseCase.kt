@file:OptIn(ExperimentalTime::class)

package com.levelchef.domain.usecase

import com.levelchef.core.model.SurveyAnswers
import com.levelchef.core.model.SurveyResponse
import com.levelchef.domain.repository.SurveyRepository
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

/** Stamps the completion time onto the user's [SurveyAnswers] and persists the [SurveyResponse]. */
class SaveSurveyResponseUseCase(
    private val surveyRepository: SurveyRepository,
    private val clock: Clock = Clock.System,
) {
    suspend operator fun invoke(answers: SurveyAnswers) {
        surveyRepository.save(
            SurveyResponse(
                completedAt = clock.now(),
                cookingExperience = answers.cookingExperience,
                dietaryPreference = answers.dietaryPreference,
                allergens = answers.allergens,
                cuisines = answers.cuisines,
                spiceTolerance = answers.spiceTolerance,
                cookingGoal = answers.cookingGoal,
                weeknightTime = answers.weeknightTime,
                householdSize = answers.householdSize,
            ),
        )
    }
}
