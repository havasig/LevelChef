package com.levelchef.feature.recipedetail

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import platform.AudioToolbox.AudioServicesPlayAlertSound
import platform.UIKit.UIApplication

/** iOS system sound "new mail"-style tri-tone; plays as a vibration in silent mode. */
private const val TIMER_ALERT_SOUND_ID = 1005u

@Composable
internal actual fun KeepScreenOn(enabled: Boolean) {
    DisposableEffect(enabled) {
        UIApplication.sharedApplication.idleTimerDisabled = enabled
        onDispose { UIApplication.sharedApplication.idleTimerDisabled = false }
    }
}

@Composable
internal actual fun rememberTimerAlarm(): () -> Unit = remember { { AudioServicesPlayAlertSound(TIMER_ALERT_SOUND_ID) } }
