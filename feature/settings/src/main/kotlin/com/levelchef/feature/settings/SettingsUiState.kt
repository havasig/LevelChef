package com.levelchef.feature.settings

/** Screen model for [SettingsScreen]. */
data class SettingsUiState(
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val language: AppLanguage = AppLanguage.SYSTEM,
)
