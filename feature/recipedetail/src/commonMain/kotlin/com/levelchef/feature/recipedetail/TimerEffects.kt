package com.levelchef.feature.recipedetail

import androidx.compose.runtime.Composable

/** Keeps the display on while [enabled] (a step timer is running). Platform-specific. */
@Composable
internal expect fun KeepScreenOn(enabled: Boolean)

/** Returns a callback that plays the "timer finished" alert (sound + vibration where available). */
@Composable
internal expect fun rememberTimerAlarm(): () -> Unit
