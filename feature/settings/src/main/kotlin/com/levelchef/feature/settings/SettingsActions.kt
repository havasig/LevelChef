package com.levelchef.feature.settings

/** Callbacks [SettingsScreen] needs, bundled so the stateless screen stays easy to preview. */
data class SettingsActions(
    val onThemeModeChange: (ThemeMode) -> Unit = {},
    val onLanguageChange: (AppLanguage) -> Unit = {},
    val onRetakeSurvey: () -> Unit = {},
    val onClearOnboardingStorage: () -> Unit = {},
)
