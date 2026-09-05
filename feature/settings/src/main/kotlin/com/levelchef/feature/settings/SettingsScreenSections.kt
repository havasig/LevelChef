package com.levelchef.feature.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.levelchef.core.designsystem.ButtonType
import com.levelchef.core.designsystem.LevelChefButton
import com.levelchef.core.designsystem.LevelChefDropdown
import com.levelchef.core.designsystem.LevelChefModal
import com.levelchef.core.ui.theme.LevelChefTextStyles
import com.levelchef.core.ui.theme.LevelChefTheme

@Composable
internal fun AppearanceSection(themeMode: ThemeMode, onChange: (ThemeMode) -> Unit) {
    val labels = ThemeMode.entries.associateWith { it.label() }
    SettingsSection(stringResource(R.string.settings_appearance_section)) {
        LevelChefDropdown(
            label = stringResource(R.string.settings_theme_label),
            selectedOption = labels.getValue(themeMode),
            options = labels.values.toList(),
            onOptionSelected = { picked -> labels.entries.first { it.value == picked }.key.let(onChange) },
        )
    }
}

@Composable
internal fun LanguageSection(language: AppLanguage, onChange: (AppLanguage) -> Unit) {
    val labels = AppLanguage.entries.associateWith { it.label() }
    SettingsSection(stringResource(R.string.settings_language_section)) {
        LevelChefDropdown(
            label = stringResource(R.string.settings_language_label),
            selectedOption = labels.getValue(language),
            options = labels.values.toList(),
            onOptionSelected = { picked -> labels.entries.first { it.value == picked }.key.let(onChange) },
        )
    }
}

@Composable
internal fun OnboardingSection(onRetake: () -> Unit) {
    SettingsSection(stringResource(R.string.settings_onboarding_section)) {
        Text(
            stringResource(R.string.settings_retake_survey_description),
            color = LevelChefTheme.colors.textSecondary,
            style = LevelChefTextStyles.bodyRegular,
        )
        ConfirmableButton(
            label = stringResource(R.string.settings_retake_survey),
            type = ButtonType.SECONDARY,
            confirmTitle = stringResource(R.string.settings_retake_confirm_title),
            confirmMessage = stringResource(R.string.settings_retake_confirm_message),
            onConfirmed = onRetake,
        )
    }
}

@Composable
internal fun DeveloperSection(onClearStorage: () -> Unit) {
    SettingsSection(stringResource(R.string.settings_developer_section)) {
        Text(
            stringResource(R.string.settings_clear_storage_description),
            color = LevelChefTheme.colors.textSecondary,
            style = LevelChefTextStyles.bodyRegular,
        )
        ConfirmableButton(
            label = stringResource(R.string.settings_clear_storage),
            type = ButtonType.DESTRUCTIVE,
            confirmTitle = stringResource(R.string.settings_clear_storage_confirm_title),
            confirmMessage = stringResource(R.string.settings_clear_storage_confirm_message),
            onConfirmed = onClearStorage,
        )
    }
}

@Composable
private fun SettingsSection(title: String, content: @Composable ColumnScope.() -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(title, color = LevelChefTheme.colors.textPrimary, style = LevelChefTextStyles.bodyLargeBold)
        content()
    }
}

@Composable
private fun ConfirmableButton(
    label: String,
    type: ButtonType,
    confirmTitle: String,
    confirmMessage: String,
    onConfirmed: () -> Unit,
) {
    var showConfirm by remember { mutableStateOf(false) }
    LevelChefButton(label = label, type = type, onClick = { showConfirm = true }, modifier = Modifier.fillMaxWidth())
    if (showConfirm) {
        LevelChefModal(
            title = confirmTitle,
            message = confirmMessage,
            onDismiss = { showConfirm = false },
            onConfirm = {
                showConfirm = false
                onConfirmed()
            },
            cancelLabel = stringResource(R.string.settings_dialog_cancel),
            confirmLabel = stringResource(R.string.settings_dialog_confirm),
        )
    }
}

@Composable
private fun ThemeMode.label(): String = stringResource(
    when (this) {
        ThemeMode.SYSTEM -> R.string.settings_theme_system
        ThemeMode.LIGHT -> R.string.settings_theme_light
        ThemeMode.DARK -> R.string.settings_theme_dark
    },
)

@Composable
private fun AppLanguage.label(): String = stringResource(
    when (this) {
        AppLanguage.SYSTEM -> R.string.settings_language_system
        AppLanguage.ENGLISH -> R.string.settings_language_english
        AppLanguage.HUNGARIAN -> R.string.settings_language_hungarian
    },
)
