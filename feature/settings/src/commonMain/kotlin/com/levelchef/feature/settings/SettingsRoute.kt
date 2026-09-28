package com.levelchef.feature.settings

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.levelchef.feature.settings.generated.resources.Res
import com.levelchef.feature.settings.generated.resources.settings_feedback_email_subject
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

/** Stateful entry point: collects [SettingsViewModel]'s state and wires it to [SettingsScreen]. */
@Composable
fun SettingsRoute(
    onBackClick: () -> Unit,
    showDeveloperOptions: Boolean,
    viewModel: SettingsViewModel = koinViewModel(),
) {
    val vmState by viewModel.uiState.collectAsState()
    val appVersion = rememberAppVersionName()
    val feedbackSubject = stringResource(Res.string.settings_feedback_email_subject)
    val sendFeedbackEmail = rememberSendFeedbackEmail(feedbackSubject)
    val openPlayStoreListing = rememberOpenPlayStoreListing()
    val state = vmState.copy(appVersion = appVersion, showDeveloperSection = showDeveloperOptions)

    SettingsScreen(
        state = state,
        actions = SettingsActions(
            onDietaryPreferenceChange = viewModel::setDietaryPreference,
            onCookingExperienceChange = viewModel::setCookingExperience,
            onHouseholdSizeChange = viewModel::setHouseholdSize,
            onThemeModeChange = viewModel::setThemeMode,
            onLanguageChange = viewModel::setLanguage,
            onSendFeedback = { text -> sendFeedbackEmail(text, viewModel::onFeedbackEmailUnavailable) },
            onReviewClick = openPlayStoreListing,
            onDeleteAccount = viewModel::deleteAccount,
            onClearOnboardingStorage = viewModel::clearOnboarding,
        ),
        onBackClick = onBackClick,
    )
}

/** The installed app's version name, or blank where it can't be determined yet (see the iOS `actual`). */
@Composable
internal expect fun rememberAppVersionName(): String

/** Opens the platform's store listing for the app (the Play Store on Android). */
@Composable
internal expect fun rememberOpenPlayStoreListing(): () -> Unit

/** Opens the platform's email composer pre-addressed to the feedback inbox with [subject] and the
 * given body; falls back to `onUnavailable` when no email app/account is available. */
@Composable
internal expect fun rememberSendFeedbackEmail(subject: String): (body: String, onUnavailable: () -> Unit) -> Unit
