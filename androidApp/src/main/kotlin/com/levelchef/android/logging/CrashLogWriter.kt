package com.levelchef.android.logging

import co.touchlab.kermit.LogWriter
import co.touchlab.kermit.Severity

/**
 * The seam for a crash-reporting SDK. Only WARN and above reach it. Today it forwards to [report],
 * which is `null` — pass a lambda (e.g. `Firebase.crashlytics::recordException`) once a reporter is
 * added.
 */
internal class CrashLogWriter(
    private val report: ((message: String, throwable: Throwable?) -> Unit)? = null,
) : LogWriter() {

    override fun isLoggable(tag: String, severity: Severity): Boolean = severity >= Severity.Warn

    override fun log(severity: Severity, message: String, tag: String, throwable: Throwable?) {
        report?.invoke(message, throwable)
    }
}
