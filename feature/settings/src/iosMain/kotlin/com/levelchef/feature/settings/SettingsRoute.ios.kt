package com.levelchef.feature.settings

import androidx.compose.runtime.Composable

// Minimal iOS stub: no iosApp exists yet (see AGENTS.md's "Not yet done" iOS item), so there's no
// store listing or email composer to open. Revisit once it does.

@Composable
internal actual fun rememberAppVersionName(): String = ""

@Composable
internal actual fun rememberOpenPlayStoreListing(): () -> Unit = {}

@Composable
internal actual fun rememberSendFeedbackEmail(subject: String): (body: String, onUnavailable: () -> Unit) -> Unit =
    { _, onUnavailable -> onUnavailable() }
