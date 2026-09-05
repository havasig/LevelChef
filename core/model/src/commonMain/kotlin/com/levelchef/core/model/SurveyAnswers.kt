package com.levelchef.core.model

/**
 * The user's in-progress survey selections, before completion is recorded. Turned into a
 * [SurveyResponse] (which adds the completion timestamp) by
 * [com.levelchef.domain.usecase.SaveSurveyResponseUseCase].
 */
data class SurveyAnswers(
    val cookingExperience: CookingExperience,
    val dietaryPreference: DietaryPreference,
    val allergens: Set<Allergen>,
    val cuisines: Set<Cuisine>,
    val spiceTolerance: SpiceTolerance,
    val cookingGoal: CookingGoal,
    val weeknightTime: WeeknightTime,
    val householdSize: HouseholdSize,
)
