package com.levelchef.feature.settings

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext

private const val PLAY_STORE_APP_ID = "com.levelchef.android"
private const val PLAY_STORE_URI = "market://details?id=$PLAY_STORE_APP_ID"
private const val PLAY_STORE_WEB_URL = "https://play.google.com/store/apps/details?id=$PLAY_STORE_APP_ID"
private const val FEEDBACK_EMAIL = "havasi.gaabor@gmail.com"

@Composable
internal actual fun rememberAppVersionName(): String {
    val context = LocalContext.current
    return remember(context) { context.appVersionName() }
}

@Composable
internal actual fun rememberOpenPlayStoreListing(): () -> Unit {
    val context = LocalContext.current
    return remember(context) { { context.openPlayStoreListing() } }
}

@Composable
internal actual fun rememberSendFeedbackEmail(subject: String): (body: String, onUnavailable: () -> Unit) -> Unit {
    val context = LocalContext.current
    return remember(context, subject) {
        { body, onUnavailable -> context.sendFeedbackEmail(subject, body, onUnavailable) }
    }
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

private fun Context.sendFeedbackEmail(subject: String, body: String, onUnavailable: () -> Unit) {
    val intent = Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:")).apply {
        putExtra(Intent.EXTRA_EMAIL, arrayOf(FEEDBACK_EMAIL))
        putExtra(Intent.EXTRA_SUBJECT, subject)
        putExtra(Intent.EXTRA_TEXT, body)
    }
    try {
        startActivity(intent)
    } catch (_: ActivityNotFoundException) {
        onUnavailable()
    }
}
