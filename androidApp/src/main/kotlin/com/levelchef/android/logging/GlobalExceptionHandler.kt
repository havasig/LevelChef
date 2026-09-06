package com.levelchef.android.logging

import co.touchlab.kermit.Logger

/**
 * Routes every uncaught exception through [Logger], then hands off to the platform's default handler
 * so the OS still shows the crash dialog and records the crash.
 */
internal fun installGlobalExceptionHandler() {
    val platformDefault = Thread.getDefaultUncaughtExceptionHandler()
    Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
        Logger.e(throwable) { "Uncaught exception on thread \"${thread.name}\"" }
        platformDefault?.uncaughtException(thread, throwable)
    }
}
