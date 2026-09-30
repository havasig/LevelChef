package com.levelchef.feature.settings

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine

/**
 * Combines the persisted [ThemeMode] (set via [IosAppSettingsController]) with the live OS
 * light/dark appearance (reported by `ContentView.swift`'s trait-collection observation) into one
 * reactive signal that `shared`'s `MainViewController()` feeds straight into `LevelChefTheme`'s
 * `darkTheme` parameter.
 *
 * This app computes `darkTheme` explicitly rather than relying on Compose's own
 * `isSystemInDarkTheme()` default because that default doesn't reliably track iOS appearance
 * changes — see https://github.com/JetBrains/compose-multiplatform/issues/3575. Public (not
 * `internal`): called from `:shared`, a separate Gradle module, and from Swift as
 * `IosThemeBridge.shared` (standard Kotlin/Native object interop).
 */
object IosThemeBridge {
    private val themeMode = MutableStateFlow(ThemeMode.SYSTEM)
    private val systemIsDark = MutableStateFlow(false)

    val effectiveDarkTheme = combine(themeMode, systemIsDark) { mode, sysDark ->
        when (mode) {
            ThemeMode.DARK -> true
            ThemeMode.LIGHT -> false
            ThemeMode.SYSTEM -> sysDark
        }
    }

    fun onThemeModeChanged(mode: ThemeMode) {
        themeMode.value = mode
    }

    /** Called from `ContentView.swift`'s `traitCollectionDidChange` (and once at launch). */
    fun onSystemAppearanceChanged(isDark: Boolean) {
        systemIsDark.value = isDark
    }
}
