@file:OptIn(ExperimentalTime::class)

package com.levelchef.core.model

import kotlin.time.ExperimentalTime
import kotlin.time.Instant

/**
 * The user's answers to the mandatory first-launch survey. Persisted once, on completion, and
 * read back to gate the survey and (later) to seed recipe recommendations.
 *
 * Not `@Serializable`: it never crosses a wire — [com.levelchef.data.repository] persists it
 * field-by-field into SQLDelight.
 */
data class SurveyResponse(
    val completedAt: Instant,
    val cookingExperience: CookingExperience,
    val dietaryPreference: DietaryPreference,
    val allergens: Set<Allergen>,
    val cuisines: Set<Cuisine>,
    val spiceTolerance: SpiceTolerance,
    val cookingGoal: CookingGoal,
    val weeknightTime: WeeknightTime,
    val householdSize: HouseholdSize,
)
