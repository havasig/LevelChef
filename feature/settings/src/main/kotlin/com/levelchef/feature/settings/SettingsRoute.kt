package com.levelchef.feature.settings

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import org.koin.compose.viewmodel.koinViewModel

private const val PLAY_STORE_APP_ID = "com.levelchef.android"
private const val PLAY_STORE_URI = "market://details?id=$PLAY_STORE_APP_ID"
private const val PLAY_STORE_WEB_URL = "https://play.google.com/store/apps/details?id=$PLAY_STORE_APP_ID"

/** Stateful entry point: collects [SettingsViewModel]'s state and wires it to [SettingsScreen]. */
@Composable
fun SettingsRoute(
    onBackClick: () -> Unit,
    viewModel: SettingsViewModel = koinViewModel(),
) {
    val vmState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val appVersion = remember { context.appVersionName() }
    val state = vmState.copy(appVersion = appVersion)

    SettingsScreen(
        state = state,
        actions = SettingsActions(
            onDietaryPreferenceChange = viewModel::setDietaryPreference,
            onCookingExperienceChange = viewModel::setCookingExperience,
            onHouseholdSizeChange = viewModel::setHouseholdSize,
            onThemeModeChange = viewModel::setThemeMode,
            onLanguageChange = viewModel::setLanguage,
            onSendFeedback = viewModel::submitFeedback,
            onReviewClick = { context.openPlayStoreListing() },
            onDeleteAccount = viewModel::deleteAccount,
            onClearOnboardingStorage = viewModel::clearOnboarding,
        ),
        onBackClick = onBackClick,
    )
}

private fun Context.appVersionName(): String =
    runCatching { packageManager.getPackageInfo(packageName, 0).versionName }.getOrNull().orEmpty()

private fun Context.openPlayStoreListing() {
    try {
        startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(PLAY_STORE_URI)))
    } catch (_: ActivityNotFoundException) {
        startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(PLAY_STORE_WEB_URL)))
    }
}
