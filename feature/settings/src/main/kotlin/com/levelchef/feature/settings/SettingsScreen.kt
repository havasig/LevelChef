package com.levelchef.feature.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.levelchef.core.designsystem.LevelChefPreview
import com.levelchef.core.designsystem.LevelChefTopAppBarInner
import com.levelchef.core.ui.theme.LevelChefTheme

@Composable
fun SettingsScreen(
    state: SettingsUiState = SettingsUiState(),
    actions: SettingsActions = SettingsActions(),
    onBackClick: () -> Unit = {},
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(LevelChefTheme.colors.background)
            .navigationBarsPadding(),
    ) {
        LevelChefTopAppBarInner(
            title = stringResource(R.string.settings_title),
            onBackClick = onBackClick,
            modifier = Modifier.statusBarsPadding(),
        )
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 24.dp),
            verticalArrangement = Arrangement.spacedBy(28.dp),
        ) {
            AppearanceSection(state.themeMode, actions.onThemeModeChange)
            LanguageSection(state.language, actions.onLanguageChange)
            OnboardingSection(actions.onRetakeSurvey)
            DeveloperSection(actions.onClearOnboardingStorage)
        }
    }
}

@LevelChefPreview
@Composable
private fun SettingsScreenPreview() {
    LevelChefTheme { SettingsScreen() }
}

@LevelChefPreview
@Composable
private fun SettingsScreenDarkSelectedPreview() {
    LevelChefTheme {
        SettingsScreen(state = SettingsUiState(themeMode = ThemeMode.DARK, language = AppLanguage.HUNGARIAN))
    }
}
