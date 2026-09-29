package com.levelchef.feature.onboarding

import com.levelchef.core.model.Allergen
import com.levelchef.core.model.CookingExperience
import com.levelchef.core.model.CookingGoal
import com.levelchef.core.model.Cuisine
import com.levelchef.core.model.DietaryPreference
import com.levelchef.core.model.HouseholdSize
import com.levelchef.core.model.SpiceTolerance
import com.levelchef.core.model.WeeknightTime

/** The callbacks [OnboardingScreen] needs, bundled so the screen stays a two-parameter composable. */
data class OnboardingActions(
    val selectExperience: (CookingExperience) -> Unit = {},
    val selectDiet: (DietaryPreference) -> Unit = {},
    val toggleAllergen: (Allergen) -> Unit = {},
    val noAllergies: () -> Unit = {},
    val toggleCuisine: (Cuisine) -> Unit = {},
    val selectSpice: (SpiceTolerance) -> Unit = {},
    val selectGoal: (CookingGoal) -> Unit = {},
    val selectTime: (WeeknightTime) -> Unit = {},
    val selectHousehold: (HouseholdSize) -> Unit = {},
    val back: () -> Unit = {},
    val next: () -> Unit = {},
)
