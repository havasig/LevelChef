package com.levelchef.feature.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.levelchef.core.designsystem.LevelChefPreview
import com.levelchef.core.designsystem.LevelChefSnackbar
import com.levelchef.core.designsystem.LevelChefTopAppBarInner
import com.levelchef.core.ui.theme.LevelChefTheme

@Composable
fun SettingsScreen(
    state: SettingsUiState = SettingsUiState(),
    actions: SettingsActions = SettingsActions(),
    onBackClick: () -> Unit = {},
) {
    Box(modifier = Modifier.fillMaxSize()) {
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
                CookingPreferencesSection(state, actions)
                AppPreferencesSection(state, actions)
                SupportSection(state, actions)
                DangerZoneSection(actions.onDeleteAccount)
                DeveloperSection(actions.onClearOnboardingStorage)
            }
        }

        if (state.snackbarMessage != null) {
            LevelChefSnackbar(
                message = state.snackbarMessage.text(),
                modifier = Modifier.align(Alignment.BottomCenter).padding(16.dp),
            )
        }
    }
}

@Composable
private fun SettingsSnackbarMessage.text(): String = stringResource(
    when (this) {
        SettingsSnackbarMessage.FEEDBACK_SENT -> R.string.settings_feedback_sent
        SettingsSnackbarMessage.ACCOUNT_DELETION_SUCCESS -> R.string.settings_account_deletion_success
    },
)

@LevelChefPreview
@Composable
private fun SettingsScreenPreview() {
    LevelChefTheme { SettingsScreen(state = SettingsUiState(appVersion = "0.1.0")) }
}

@LevelChefPreview
@Composable
private fun SettingsScreenDarkSelectedPreview() {
    LevelChefTheme {
        SettingsScreen(
            state = SettingsUiState(
                themeMode = ThemeMode.DARK,
                language = AppLanguage.HUNGARIAN,
                appVersion = "0.1.0",
            ),
        )
    }
}
