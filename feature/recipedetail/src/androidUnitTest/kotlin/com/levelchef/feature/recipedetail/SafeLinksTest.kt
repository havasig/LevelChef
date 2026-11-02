package com.levelchef.feature.recipedetail

import androidx.compose.ui.platform.UriHandler
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class SafeLinksTest {

    private class RecordingUriHandler(private val fail: Boolean = false) : UriHandler {
        val opened = mutableListOf<String>()
        override fun openUri(uri: String) {
            if (fail) throw IllegalArgumentException("Can't open $uri.")
            opened += uri
        }
    }

    @Test
    fun only_https_links_are_considered_safe() {
        assertTrue(isSafeWebLink("https://www.youtube.com/results?search_query=pasta"))
        assertFalse(isSafeWebLink("http://example.com"))
        assertFalse(isSafeWebLink("intent://evil#Intent;end"))
        assertFalse(isSafeWebLink("javascript:alert(1)"))
        assertFalse(isSafeWebLink("youtube.com/watch"))
        assertFalse(isSafeWebLink("https://"))
        assertFalse(isSafeWebLink("https://a b"))
    }

    @Test
    fun an_unsafe_link_is_never_opened() {
        val handler = RecordingUriHandler()

        handler.openWebLinkSafely("intent://evil#Intent;end")

        assertTrue(handler.opened.isEmpty())
    }

    @Test
    fun a_safe_link_is_opened() {
        val handler = RecordingUriHandler()

        handler.openWebLinkSafely("https://www.youtube.com/")

        assertEquals(listOf("https://www.youtube.com/"), handler.opened)
    }

    @Test
    fun no_app_to_handle_the_link_does_not_crash() {
        RecordingUriHandler(fail = true).openWebLinkSafely("https://www.youtube.com/")
    }
}
