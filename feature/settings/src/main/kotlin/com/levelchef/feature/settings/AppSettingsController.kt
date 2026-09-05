package com.levelchef.feature.settings

/**
 * App-shell configuration that lives outside the domain layer: the light/dark theme and the app
 * language. Both are applied through the platform (see [AndroidAppSettingsController]) rather than
 * stored as domain state — the theme in `SharedPreferences`, the language in AppCompat's own store.
 */
interface AppSettingsController {

    /** The persisted theme choice (defaults to [ThemeMode.SYSTEM] when nothing is stored). */
    fun themeMode(): ThemeMode

    /** Persists [mode] and applies it immediately. */
    fun setThemeMode(mode: ThemeMode)

    /** Re-applies the persisted theme — called once on process start. */
    fun applyPersistedThemeMode()

    /** The currently applied app language. */
    fun language(): AppLanguage

    /** Applies [language]; AppCompat persists it and recreates the activity. */
    fun setLanguage(language: AppLanguage)
}
