package com.levelchef.feature.settings

import platform.Foundation.NSLocale
import platform.Foundation.NSUserDefaults
import platform.Foundation.preferredLanguages

/**
 * iOS implementation of [AppSettingsController].
 *
 * - **Theme** — persisted under [KEY_THEME_MODE] in `NSUserDefaults`. Unlike
 *   `AppCompatDelegate.setDefaultNightMode`, iOS has no app-wide "recreate every window with this
 *   interface style" call, so [setThemeMode] and [applyPersistedThemeMode] instead push the mode
 *   into [IosThemeBridge], which `shared`'s `MainViewController()` reads to theme Compose directly
 *   — see [IosThemeBridge]'s doc comment for why.
 * - **Language** — persisted the way Apple documents for in-app language overrides: writing the
 *   `AppleLanguages` `NSUserDefaults` key (see
 *   https://developer.apple.com/library/archive/qa/qa1828/_index.html). [language] reads that key
 *   back directly rather than [NSLocale.preferredLanguages] because the OS only folds an
 *   `AppleLanguages` change into `NSLocale.preferredLanguages` on the next launch — reading our own
 *   key keeps this in-process reflection immediate, matching how `AppCompatDelegate`'s locale is
 *   readable right after `setApplicationLocales` on Android. `data`'s iosMain `databaseModule`
 *   still reads `NSLocale.preferredLanguages` directly (it can't depend on `feature:settings`,
 *   mirroring the same constraint on Android), so a language switch here only take full effect
 *   for recipe generation after the app is relaunched — same as the Android seam's own doc comment.
 */
class IosAppSettingsController : AppSettingsController {

    private val defaults = NSUserDefaults.standardUserDefaults

    override fun themeMode(): ThemeMode =
        defaults.stringForKey(KEY_THEME_MODE)
            ?.let { runCatching { ThemeMode.valueOf(it) }.getOrNull() }
            ?: ThemeMode.SYSTEM

    override fun setThemeMode(mode: ThemeMode) {
        defaults.setObject(mode.name, forKey = KEY_THEME_MODE)
        IosThemeBridge.onThemeModeChanged(mode)
    }

    override fun applyPersistedThemeMode() {
        IosThemeBridge.onThemeModeChanged(themeMode())
    }

    override fun language(): AppLanguage {
        val overrideTag = defaults.arrayForKey(KEY_APPLE_LANGUAGES)?.firstOrNull() as? String
        val primaryTag = (overrideTag ?: NSLocale.preferredLanguages.firstOrNull() as? String)
            ?.substringBefore('-')
        return AppLanguage.entries.firstOrNull { it.tag == primaryTag } ?: AppLanguage.SYSTEM
    }

    override fun setLanguage(language: AppLanguage) {
        val tag = language.tag
        if (tag == null) {
            defaults.removeObjectForKey(KEY_APPLE_LANGUAGES)
        } else {
            defaults.setObject(listOf(tag), forKey = KEY_APPLE_LANGUAGES)
        }
    }

    override fun resetToDefaults() {
        setThemeMode(ThemeMode.SYSTEM)
        setLanguage(AppLanguage.SYSTEM)
    }

    private companion object {
        const val KEY_THEME_MODE = "theme_mode"
        const val KEY_APPLE_LANGUAGES = "AppleLanguages"
    }
}
