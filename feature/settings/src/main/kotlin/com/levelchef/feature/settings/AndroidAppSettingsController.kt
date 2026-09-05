package com.levelchef.feature.settings

import android.content.Context
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat

/**
 * Android implementation of [AppSettingsController].
 *
 * - **Theme** — persisted in a private `SharedPreferences` file and applied via
 *   [AppCompatDelegate.setDefaultNightMode] (AppCompat does not persist the night mode itself).
 * - **Language** — applied via [AppCompatDelegate.setApplicationLocales]; AppCompat stores it
 *   (see the `AppLocalesMetadataHolderService` / `autoStoreLocales` entry in the app manifest) and
 *   recreates the activity.
 */
class AndroidAppSettingsController(context: Context) : AppSettingsController {

    private val prefs = context.applicationContext
        .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    override fun themeMode(): ThemeMode =
        prefs.getString(KEY_THEME_MODE, null)
            ?.let { runCatching { ThemeMode.valueOf(it) }.getOrNull() }
            ?: ThemeMode.SYSTEM

    override fun setThemeMode(mode: ThemeMode) {
        prefs.edit().putString(KEY_THEME_MODE, mode.name).apply()
        AppCompatDelegate.setDefaultNightMode(mode.toNightMode())
    }

    override fun applyPersistedThemeMode() {
        AppCompatDelegate.setDefaultNightMode(themeMode().toNightMode())
    }

    override fun language(): AppLanguage {
        val primaryTag = AppCompatDelegate.getApplicationLocales()
            .toLanguageTags()
            .substringBefore(',')
            .substringBefore('-')
        return AppLanguage.entries.firstOrNull { it.tag == primaryTag } ?: AppLanguage.SYSTEM
    }

    override fun setLanguage(language: AppLanguage) {
        AppCompatDelegate.setApplicationLocales(
            language.tag
                ?.let { LocaleListCompat.forLanguageTags(it) }
                ?: LocaleListCompat.getEmptyLocaleList(),
        )
    }

    private fun ThemeMode.toNightMode(): Int = when (this) {
        ThemeMode.SYSTEM -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
        ThemeMode.LIGHT -> AppCompatDelegate.MODE_NIGHT_NO
        ThemeMode.DARK -> AppCompatDelegate.MODE_NIGHT_YES
    }

    private companion object {
        const val PREFS_NAME = "levelchef_settings"
        const val KEY_THEME_MODE = "theme_mode"
    }
}
