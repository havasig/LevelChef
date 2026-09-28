package com.levelchef.feature.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.levelchef.core.designsystem.ButtonType
import com.levelchef.core.designsystem.LevelChefButton
import com.levelchef.core.designsystem.LevelChefCard
import com.levelchef.core.designsystem.LevelChefDivider
import com.levelchef.core.designsystem.LevelChefInputField
import com.levelchef.core.designsystem.LevelChefModal
import com.levelchef.core.designsystem.LevelChefRadioButton
import com.levelchef.core.model.CookingExperience
import com.levelchef.core.model.DietaryPreference
import com.levelchef.core.model.HouseholdSize
import com.levelchef.core.ui.theme.LevelChefTextStyles
import com.levelchef.core.ui.theme.LevelChefTheme
import com.levelchef.feature.settings.generated.resources.Res
import com.levelchef.feature.settings.generated.resources.settings_app_preferences_section
import com.levelchef.feature.settings.generated.resources.settings_clear_storage
import com.levelchef.feature.settings.generated.resources.settings_clear_storage_confirm_message
import com.levelchef.feature.settings.generated.resources.settings_clear_storage_confirm_title
import com.levelchef.feature.settings.generated.resources.settings_clear_storage_description
import com.levelchef.feature.settings.generated.resources.settings_cooking_preferences_section
import com.levelchef.feature.settings.generated.resources.settings_danger_zone_section
import com.levelchef.feature.settings.generated.resources.settings_delete_account_confirm_label
import com.levelchef.feature.settings.generated.resources.settings_delete_account_confirm_message
import com.levelchef.feature.settings.generated.resources.settings_delete_account_title
import com.levelchef.feature.settings.generated.resources.settings_developer_section
import com.levelchef.feature.settings.generated.resources.settings_dialog_cancel
import com.levelchef.feature.settings.generated.resources.settings_dialog_confirm
import com.levelchef.feature.settings.generated.resources.settings_diet_flexitarian
import com.levelchef.feature.settings.generated.resources.settings_diet_omnivore
import com.levelchef.feature.settings.generated.resources.settings_diet_pescatarian
import com.levelchef.feature.settings.generated.resources.settings_diet_vegan
import com.levelchef.feature.settings.generated.resources.settings_diet_vegetarian
import com.levelchef.feature.settings.generated.resources.settings_dietary_restrictions_title
import com.levelchef.feature.settings.generated.resources.settings_feedback_dialog_title
import com.levelchef.feature.settings.generated.resources.settings_feedback_input_label
import com.levelchef.feature.settings.generated.resources.settings_feedback_input_placeholder
import com.levelchef.feature.settings.generated.resources.settings_feedback_send
import com.levelchef.feature.settings.generated.resources.settings_household_five_plus
import com.levelchef.feature.settings.generated.resources.settings_household_solo
import com.levelchef.feature.settings.generated.resources.settings_household_three_four
import com.levelchef.feature.settings.generated.resources.settings_household_two
import com.levelchef.feature.settings.generated.resources.settings_language_english
import com.levelchef.feature.settings.generated.resources.settings_language_hungarian
import com.levelchef.feature.settings.generated.resources.settings_language_label
import com.levelchef.feature.settings.generated.resources.settings_language_system
import com.levelchef.feature.settings.generated.resources.settings_rate_app_subtitle
import com.levelchef.feature.settings.generated.resources.settings_rate_app_title
import com.levelchef.feature.settings.generated.resources.settings_send_feedback_subtitle
import com.levelchef.feature.settings.generated.resources.settings_send_feedback_title
import com.levelchef.feature.settings.generated.resources.settings_serving_size_title
import com.levelchef.feature.settings.generated.resources.settings_skill_beginner
import com.levelchef.feature.settings.generated.resources.settings_skill_comfortable
import com.levelchef.feature.settings.generated.resources.settings_skill_confident
import com.levelchef.feature.settings.generated.resources.settings_skill_level_title
import com.levelchef.feature.settings.generated.resources.settings_skill_never_cooked
import com.levelchef.feature.settings.generated.resources.settings_skill_pro
import com.levelchef.feature.settings.generated.resources.settings_support_section
import com.levelchef.feature.settings.generated.resources.settings_theme_dark
import com.levelchef.feature.settings.generated.resources.settings_theme_label
import com.levelchef.feature.settings.generated.resources.settings_theme_light
import com.levelchef.feature.settings.generated.resources.settings_theme_system
import com.levelchef.feature.settings.generated.resources.settings_version_title
import com.levelchef.feature.settings.generated.resources.settings_version_value
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun CookingPreferencesSection(state: SettingsUiState, actions: SettingsActions) {
    var editing by remember { mutableStateOf<CookingPreferenceField?>(null) }

    SettingsSection(stringResource(Res.string.settings_cooking_preferences_section)) {
        LevelChefCard {
            SettingsRow(
                title = stringResource(Res.string.settings_dietary_restrictions_title),
                subtitle = state.dietaryPreference.label(),
                onClick = { editing = CookingPreferenceField.DIET },
            )
            LevelChefDivider()
            SettingsRow(
                title = stringResource(Res.string.settings_skill_level_title),
                subtitle = state.cookingExperience.label(),
                onClick = { editing = CookingPreferenceField.SKILL },
            )
            LevelChefDivider()
            SettingsRow(
                title = stringResource(Res.string.settings_serving_size_title),
                subtitle = state.householdSize.label(),
                onClick = { editing = CookingPreferenceField.HOUSEHOLD },
            )
        }
    }

    when (editing) {
        CookingPreferenceField.DIET -> {
            val labels = DietaryPreference.entries.associateWith { it.label() }
            OptionPickerDialog(
                title = stringResource(Res.string.settings_dietary_restrictions_title),
                options = DietaryPreference.entries,
                selected = state.dietaryPreference,
                labelFor = { labels.getValue(it) },
                onSelect = actions.onDietaryPreferenceChange,
                onDismiss = { editing = null },
            )
        }

        CookingPreferenceField.SKILL -> {
            val labels = CookingExperience.entries.associateWith { it.label() }
            OptionPickerDialog(
                title = stringResource(Res.string.settings_skill_level_title),
                options = CookingExperience.entries,
                selected = state.cookingExperience,
                labelFor = { labels.getValue(it) },
                onSelect = actions.onCookingExperienceChange,
                onDismiss = { editing = null },
            )
        }

        CookingPreferenceField.HOUSEHOLD -> {
            val labels = HouseholdSize.entries.associateWith { it.label() }
            OptionPickerDialog(
                title = stringResource(Res.string.settings_serving_size_title),
                options = HouseholdSize.entries,
                selected = state.householdSize,
                labelFor = { labels.getValue(it) },
                onSelect = actions.onHouseholdSizeChange,
                onDismiss = { editing = null },
            )
        }

        null -> Unit
    }
}

private enum class CookingPreferenceField { DIET, SKILL, HOUSEHOLD }

@Composable
internal fun AppPreferencesSection(state: SettingsUiState, actions: SettingsActions) {
    var editing by remember { mutableStateOf<AppPreferenceField?>(null) }

    SettingsSection(stringResource(Res.string.settings_app_preferences_section)) {
        LevelChefCard {
            SettingsRow(
                title = stringResource(Res.string.settings_theme_label),
                subtitle = state.themeMode.label(),
                onClick = { editing = AppPreferenceField.THEME },
            )
            LevelChefDivider()
            SettingsRow(
                title = stringResource(Res.string.settings_language_label),
                subtitle = state.language.label(),
                onClick = { editing = AppPreferenceField.LANGUAGE },
            )
        }
    }

    when (editing) {
        AppPreferenceField.THEME -> {
            val labels = ThemeMode.entries.associateWith { it.label() }
            OptionPickerDialog(
                title = stringResource(Res.string.settings_theme_label),
                options = ThemeMode.entries,
                selected = state.themeMode,
                labelFor = { labels.getValue(it) },
                onSelect = actions.onThemeModeChange,
                onDismiss = { editing = null },
            )
        }

        AppPreferenceField.LANGUAGE -> {
            val labels = AppLanguage.entries.associateWith { it.label() }
            OptionPickerDialog(
                title = stringResource(Res.string.settings_language_label),
                options = AppLanguage.entries,
                selected = state.language,
                labelFor = { labels.getValue(it) },
                onSelect = actions.onLanguageChange,
                onDismiss = { editing = null },
            )
        }

        null -> Unit
    }
}

private enum class AppPreferenceField { THEME, LANGUAGE }

@Composable
internal fun SupportSection(state: SettingsUiState, actions: SettingsActions) {
    var showFeedbackDialog by remember { mutableStateOf(false) }
    var feedbackText by remember { mutableStateOf("") }

    SettingsSection(stringResource(Res.string.settings_support_section)) {
        LevelChefCard {
            SettingsRow(
                title = stringResource(Res.string.settings_send_feedback_title),
                subtitle = stringResource(Res.string.settings_send_feedback_subtitle),
                onClick = { showFeedbackDialog = true },
            )
            LevelChefDivider()
            SettingsRow(
                title = stringResource(Res.string.settings_rate_app_title),
                subtitle = stringResource(Res.string.settings_rate_app_subtitle),
                onClick = actions.onReviewClick,
            )
            LevelChefDivider()
            SettingsRow(
                title = stringResource(Res.string.settings_version_title),
                subtitle = stringResource(Res.string.settings_version_value, state.appVersion),
                onClick = null,
            )
        }
    }

    if (showFeedbackDialog) {
        FeedbackDialog(
            value = feedbackText,
            onValueChange = { feedbackText = it },
            onCancel = { showFeedbackDialog = false },
            onSend = {
                actions.onSendFeedback(feedbackText)
                showFeedbackDialog = false
                feedbackText = ""
            },
        )
    }
}

@Composable
internal fun DangerZoneSection(onDeleteAccount: () -> Unit) {
    SettingsSection(stringResource(Res.string.settings_danger_zone_section)) {
        ConfirmableButton(
            label = stringResource(Res.string.settings_delete_account_title),
            type = ButtonType.DESTRUCTIVE,
            confirmTitle = stringResource(Res.string.settings_delete_account_title),
            confirmMessage = stringResource(Res.string.settings_delete_account_confirm_message),
            confirmLabel = stringResource(Res.string.settings_delete_account_confirm_label),
            onConfirmed = onDeleteAccount,
        )
    }
}

@Composable
internal fun DeveloperSection(onClearStorage: () -> Unit) {
    SettingsSection(stringResource(Res.string.settings_developer_section)) {
        Text(
            stringResource(Res.string.settings_clear_storage_description),
            color = LevelChefTheme.colors.textSecondary,
            style = LevelChefTextStyles.bodyRegular,
        )
        ConfirmableButton(
            label = stringResource(Res.string.settings_clear_storage),
            type = ButtonType.DESTRUCTIVE,
            confirmTitle = stringResource(Res.string.settings_clear_storage_confirm_title),
            confirmMessage = stringResource(Res.string.settings_clear_storage_confirm_message),
            onConfirmed = onClearStorage,
        )
    }
}

@Composable
private fun SettingsSection(title: String, content: @Composable ColumnScope.() -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(title, color = LevelChefTheme.colors.textSecondary, style = LevelChefTextStyles.captionBold)
        content()
    }
}

/** One tappable (or, when [onClick] is `null`, static) row: bold title, secondary subtitle, trailing chevron. */
@Composable
private fun SettingsRow(title: String, subtitle: String, onClick: (() -> Unit)?) {
    val rowModifier = if (onClick != null) {
        Modifier.fillMaxWidth().clickable(interactionSource = null, indication = null, onClick = onClick)
    } else {
        Modifier.fillMaxWidth()
    }
    Row(
        modifier = rowModifier.padding(vertical = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(title, color = LevelChefTheme.colors.textPrimary, style = LevelChefTextStyles.bodyRegularBold)
            Text(subtitle, color = LevelChefTheme.colors.textSecondary, style = LevelChefTextStyles.bodySmall)
        }
        if (onClick != null) {
            Icon(
                Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = LevelChefTheme.colors.textSecondary,
            )
        }
    }
}

/** A single-choice picker shared by every "pick one of a fixed set of values" row on this screen. */
@Composable
private fun <T> OptionPickerDialog(
    title: String,
    options: List<T>,
    selected: T,
    labelFor: (T) -> String,
    onSelect: (T) -> Unit,
    onDismiss: () -> Unit,
) {
    Dialog(onDismissRequest = onDismiss) {
        LevelChefCard(modifier = Modifier.fillMaxWidth()) {
            Text(title, color = LevelChefTheme.colors.textPrimary, style = LevelChefTextStyles.bodyLargeBold)
            options.forEach { option ->
                LevelChefRadioButton(
                    selected = option == selected,
                    onClick = {
                        onSelect(option)
                        onDismiss()
                    },
                    label = labelFor(option),
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}

@Composable
private fun FeedbackDialog(value: String, onValueChange: (String) -> Unit, onCancel: () -> Unit, onSend: () -> Unit) {
    Dialog(onDismissRequest = onCancel) {
        LevelChefCard(modifier = Modifier.fillMaxWidth()) {
            Text(
                stringResource(Res.string.settings_feedback_dialog_title),
                color = LevelChefTheme.colors.textPrimary,
                style = LevelChefTextStyles.bodyLargeBold,
            )
            LevelChefInputField(
                label = stringResource(Res.string.settings_feedback_input_label),
                value = value,
                onValueChange = onValueChange,
                placeholder = stringResource(Res.string.settings_feedback_input_placeholder),
                singleLine = false,
            )
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    LevelChefButton(
                        label = stringResource(Res.string.settings_dialog_cancel),
                        type = ButtonType.SECONDARY,
                        onClick = onCancel,
                    )
                    LevelChefButton(
                        label = stringResource(Res.string.settings_feedback_send),
                        type = ButtonType.PRIMARY,
                        onClick = onSend,
                        enabled = value.isNotBlank(),
                    )
                }
            }
        }
    }
}

@Composable
private fun ConfirmableButton(
    label: String,
    type: ButtonType,
    confirmTitle: String,
    confirmMessage: String,
    onConfirmed: () -> Unit,
    confirmLabel: String = stringResource(Res.string.settings_dialog_confirm),
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
            cancelLabel = stringResource(Res.string.settings_dialog_cancel),
            confirmLabel = confirmLabel,
        )
    }
}

@Composable
private fun ThemeMode.label(): String = stringResource(
    when (this) {
        ThemeMode.SYSTEM -> Res.string.settings_theme_system
        ThemeMode.LIGHT -> Res.string.settings_theme_light
        ThemeMode.DARK -> Res.string.settings_theme_dark
    },
)

@Composable
private fun AppLanguage.label(): String = stringResource(
    when (this) {
        AppLanguage.SYSTEM -> Res.string.settings_language_system
        AppLanguage.ENGLISH -> Res.string.settings_language_english
        AppLanguage.HUNGARIAN -> Res.string.settings_language_hungarian
    },
)

@Composable
private fun DietaryPreference.label(): String = stringResource(
    when (this) {
        DietaryPreference.OMNIVORE -> Res.string.settings_diet_omnivore
        DietaryPreference.VEGETARIAN -> Res.string.settings_diet_vegetarian
        DietaryPreference.VEGAN -> Res.string.settings_diet_vegan
        DietaryPreference.PESCATARIAN -> Res.string.settings_diet_pescatarian
        DietaryPreference.FLEXITARIAN -> Res.string.settings_diet_flexitarian
    },
)

@Composable
private fun CookingExperience.label(): String = stringResource(
    when (this) {
        CookingExperience.NEVER_COOKED -> Res.string.settings_skill_never_cooked
        CookingExperience.BEGINNER -> Res.string.settings_skill_beginner
        CookingExperience.COMFORTABLE -> Res.string.settings_skill_comfortable
        CookingExperience.CONFIDENT -> Res.string.settings_skill_confident
        CookingExperience.PRO -> Res.string.settings_skill_pro
    },
)

@Composable
private fun HouseholdSize.label(): String = stringResource(
    when (this) {
        HouseholdSize.SOLO -> Res.string.settings_household_solo
        HouseholdSize.TWO -> Res.string.settings_household_two
        HouseholdSize.THREE_TO_FOUR -> Res.string.settings_household_three_four
        HouseholdSize.FIVE_PLUS -> Res.string.settings_household_five_plus
    },
)
