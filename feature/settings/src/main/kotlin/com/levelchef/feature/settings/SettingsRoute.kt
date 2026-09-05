package com.levelchef.feature.settings

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import org.koin.compose.viewmodel.koinViewModel

/** Stateful entry point: collects [SettingsViewModel]'s state and wires it to [SettingsScreen]. */
@Composable
fun SettingsRoute(
    onBackClick: () -> Unit,
    viewModel: SettingsViewModel = koinViewModel(),
) {
    val state by viewModel.uiState.collectAsState()
    SettingsScreen(
        state = state,
        actions = SettingsActions(
            onThemeModeChange = viewModel::setThemeMode,
            onLanguageChange = viewModel::setLanguage,
            onRetakeSurvey = viewModel::clearOnboarding,
            onClearOnboardingStorage = viewModel::clearOnboarding,
        ),
        onBackClick = onBackClick,
    )
}
