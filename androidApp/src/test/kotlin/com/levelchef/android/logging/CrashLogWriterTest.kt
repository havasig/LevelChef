package com.levelchef.android.logging

import co.touchlab.kermit.Severity
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertSame
import kotlin.test.assertTrue

class CrashLogWriterTest {

    private val writer = CrashLogWriter()

    @Test
    fun ignores_verbose_debug_and_info() {
        assertFalse(writer.isLoggable("tag", Severity.Verbose))
        assertFalse(writer.isLoggable("tag", Severity.Debug))
        assertFalse(writer.isLoggable("tag", Severity.Info))
    }

    @Test
    fun keeps_warn_and_above() {
        assertTrue(writer.isLoggable("tag", Severity.Warn))
        assertTrue(writer.isLoggable("tag", Severity.Error))
        assertTrue(writer.isLoggable("tag", Severity.Assert))
    }

    @Test
    fun forwards_the_message_and_throwable_to_the_reporter() {
        var message: String? = null
        var throwable: Throwable? = null
        val reporting = CrashLogWriter { m, t -> message = m; throwable = t }
        val boom = IllegalStateException("boom")

        reporting.log(Severity.Error, "kaboom", "tag", boom)

        assertEquals("kaboom", message)
        assertSame(boom, throwable)
    }
}
