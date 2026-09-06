package com.levelchef.feature.settings

import com.levelchef.core.model.CookingExperience
import com.levelchef.core.model.DietaryPreference
import com.levelchef.core.model.HouseholdSize

/** Callbacks [SettingsScreen] needs, bundled so the stateless screen stays easy to preview. */
data class SettingsActions(
    val onDietaryPreferenceChange: (DietaryPreference) -> Unit = {},
    val onCookingExperienceChange: (CookingExperience) -> Unit = {},
    val onHouseholdSizeChange: (HouseholdSize) -> Unit = {},
    val onThemeModeChange: (ThemeMode) -> Unit = {},
    val onLanguageChange: (AppLanguage) -> Unit = {},
    val onSendFeedback: (String) -> Unit = {},
    val onReviewClick: () -> Unit = {},
    val onDeleteAccount: () -> Unit = {},
    val onClearOnboardingStorage: () -> Unit = {},
)
