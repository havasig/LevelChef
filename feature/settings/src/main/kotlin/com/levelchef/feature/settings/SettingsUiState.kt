package com.levelchef.feature.settings

import com.levelchef.core.model.CookingExperience
import com.levelchef.core.model.DietaryPreference
import com.levelchef.core.model.HouseholdSize

/** Screen model for [SettingsScreen]. */
data class SettingsUiState(
    val dietaryPreference: DietaryPreference = DietaryPreference.OMNIVORE,
    val cookingExperience: CookingExperience = CookingExperience.BEGINNER,
    val householdSize: HouseholdSize = HouseholdSize.TWO,
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val language: AppLanguage = AppLanguage.SYSTEM,
    val appVersion: String = "",
    val snackbarMessage: SettingsSnackbarMessage? = null,
)
