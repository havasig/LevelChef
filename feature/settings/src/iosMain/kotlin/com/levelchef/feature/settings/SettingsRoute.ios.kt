package com.levelchef.feature.settings

import androidx.compose.runtime.Composable
import platform.Foundation.NSBundle

// The Play Store listing and system mail composer have no iOS equivalent reachable from here yet
// (see AGENTS.md's "Not yet done" iOS item), so those two stay stubs. The version number, however,
// is a real NSBundle read now that iosApp/Info.plist defines CFBundleShortVersionString.

@Composable
internal actual fun rememberAppVersionName(): String =
    NSBundle.mainBundle.infoDictionary?.get("CFBundleShortVersionString") as? String ?: ""

@Composable
internal actual fun rememberOpenPlayStoreListing(): () -> Unit = {}

@Composable
internal actual fun rememberSendFeedbackEmail(subject: String): (body: String, onUnavailable: () -> Unit) -> Unit =
    { _, onUnavailable -> onUnavailable() }
