package com.levelchef.feature.settings

/**
 * Minimal iOS implementation of [AppSettingsController]. No `iosApp` exists yet (see `AGENTS.md`'s
 * "Not yet done" iOS item), so this is intentionally not persisted — in-memory state only, enough
 * to satisfy the interface and let `feature:settings` compile for the iOS targets. Swap this for a
 * real `NSUserDefaults`-backed (theme) / `NSLocale`-based (language) implementation once `iosApp`
 * exists, mirroring `data`'s existing iosMain `databaseModule` pattern for platform code.
 */
class IosAppSettingsController : AppSettingsController {

    private var theme = ThemeMode.SYSTEM
    private var lang = AppLanguage.SYSTEM

    override fun themeMode(): ThemeMode = theme

    override fun setThemeMode(mode: ThemeMode) {
        theme = mode
    }

    override fun applyPersistedThemeMode() = Unit

    override fun language(): AppLanguage = lang

    override fun setLanguage(language: AppLanguage) {
        lang = language
    }

    override fun resetToDefaults() {
        theme = ThemeMode.SYSTEM
        lang = AppLanguage.SYSTEM
    }
}
