package com.levelchef.feature.recipedetail

import android.content.Context
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import co.touchlab.kermit.Logger

private val VIBRATION_PATTERN_MS = longArrayOf(0, 400, 200, 400, 200, 400)

@Composable
internal actual fun KeepScreenOn(enabled: Boolean) {
    val view = LocalView.current
    DisposableEffect(view, enabled) {
        view.keepScreenOn = enabled
        onDispose { view.keepScreenOn = false }
    }
}

@Composable
internal actual fun rememberTimerAlarm(): () -> Unit {
    val context = LocalContext.current.applicationContext
    return remember(context) { { playAlarm(context) } }
}

/** Plays the default notification sound (respects silent mode) and a short vibration. */
@Suppress("TooGenericExceptionCaught")
private fun playAlarm(context: Context) {
    try {
        val uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        RingtoneManager.getRingtone(context, uri)?.apply {
            audioAttributes = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_NOTIFICATION_EVENT)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()
            play()
        }
        vibrator(context)?.vibrate(VibrationEffect.createWaveform(VIBRATION_PATTERN_MS, -1))
    } catch (e: Exception) {
        // An alert that can't play must never take the cooking screen down with it.
        Logger.w(e) { "Could not play the timer alert" }
    }
}

private fun vibrator(context: Context): Vibrator? =
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        context.getSystemService(VibratorManager::class.java)?.defaultVibrator
    } else {
        @Suppress("DEPRECATION")
        context.getSystemService(Vibrator::class.java)
    }
