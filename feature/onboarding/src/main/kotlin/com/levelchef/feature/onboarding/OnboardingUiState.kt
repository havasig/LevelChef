package com.levelchef.feature.onboarding

import com.levelchef.core.model.Allergen
import com.levelchef.core.model.CookingExperience
import com.levelchef.core.model.CookingGoal
import com.levelchef.core.model.Cuisine
import com.levelchef.core.model.DietaryPreference
import com.levelchef.core.model.HouseholdSize
import com.levelchef.core.model.SpiceTolerance
import com.levelchef.core.model.SurveyAnswers
import com.levelchef.core.model.WeeknightTime

/**
 * State of the first-launch survey wizard. While [loading] the gate shows a splash; once the
 * survey is [completed] the gate shows the app; otherwise it renders the current [currentStep].
 */
data class OnboardingUiState(
    val loading: Boolean = true,
    val completed: Boolean = false,
    val stepIndex: Int = 0,
    val cookingExperience: CookingExperience? = null,
    val dietaryPreference: DietaryPreference? = null,
    val allergens: Set<Allergen> = emptySet(),
    val noAllergies: Boolean = false,
    val cuisines: Set<Cuisine> = emptySet(),
    val spiceTolerance: SpiceTolerance? = null,
    val cookingGoal: CookingGoal? = null,
    val weeknightTime: WeeknightTime? = null,
    val householdSize: HouseholdSize? = null,
) {
    val currentStep: OnboardingStep get() = OnboardingStep.entries[stepIndex]

    val isFirstStep: Boolean get() = stepIndex == 0

    val isLastStep: Boolean get() = stepIndex == OnboardingStep.entries.lastIndex

    /** 1-based position among the [QUESTION_COUNT] questions; 0 on the welcome step. */
    val questionNumber: Int get() = stepIndex

    /** Whether the current step is validly answered and the user may move on. */
    val canAdvance: Boolean
        get() = when (currentStep) {
            OnboardingStep.WELCOME -> true
            OnboardingStep.EXPERIENCE -> cookingExperience != null
            OnboardingStep.DIET -> dietaryPreference != null
            OnboardingStep.ALLERGENS -> noAllergies || allergens.isNotEmpty()
            OnboardingStep.CUISINES -> cuisines.isNotEmpty()
            OnboardingStep.SPICE -> spiceTolerance != null
            OnboardingStep.GOAL -> cookingGoal != null
            OnboardingStep.TIME -> weeknightTime != null
            OnboardingStep.HOUSEHOLD -> householdSize != null
        }

    /** The completed answer set, or `null` if any question is still unanswered. */
    val answers: SurveyAnswers?
        get() {
            if (cookingExperience == null || dietaryPreference == null || spiceTolerance == null ||
                cookingGoal == null || weeknightTime == null || householdSize == null
            ) {
                return null
            }
            return SurveyAnswers(
                cookingExperience = cookingExperience,
                dietaryPreference = dietaryPreference,
                allergens = allergens,
                cuisines = cuisines,
                spiceTolerance = spiceTolerance,
                cookingGoal = cookingGoal,
                weeknightTime = weeknightTime,
                householdSize = householdSize,
            )
        }
}
