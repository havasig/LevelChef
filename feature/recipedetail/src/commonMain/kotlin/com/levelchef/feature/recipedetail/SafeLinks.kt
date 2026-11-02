package com.levelchef.feature.recipedetail

import androidx.compose.ui.platform.UriHandler
import co.touchlab.kermit.Logger

/**
 * Opens [url] only if it's an `https://` link, and never lets a failure crash the app: on Android,
 * `UriHandler.openUri` throws when no installed app can handle the link. A recipe's video link is
 * data (cached JSON), so it's treated as untrusted here even though the repository builds it.
 */
@Suppress("TooGenericExceptionCaught")
internal fun UriHandler.openWebLinkSafely(url: String) {
    if (!isSafeWebLink(url)) {
        Logger.w { "Refusing to open a non-https link" }
        return
    }
    try {
        openUri(url)
    } catch (e: Exception) {
        Logger.w(e) { "No app could open the recipe video link" }
    }
}

internal fun isSafeWebLink(url: String): Boolean =
    url.startsWith("https://", ignoreCase = true) && url.length > "https://".length && url.none { it.isWhitespace() }
