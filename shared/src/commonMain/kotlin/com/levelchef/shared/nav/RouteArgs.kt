package com.levelchef.shared.nav

/**
 * Percent-encodes [value] for use as a route path/query argument (RFC 3986: everything but the
 * unreserved characters `A-Z a-z 0-9 - . _ ~` is encoded) — the multiplatform counterpart of
 * androidApp's `Uri.encode` call. A recipe id is data (cached JSON), and a `/`, `?`, `#` or `%` in it
 * would otherwise match no route and crash `navigate`; Navigation decodes arguments itself.
 */
internal fun encodeRouteArg(value: String): String = buildString {
    value.encodeToByteArray().forEach { byte ->
        val code = byte.toInt() and BYTE_MASK
        val c = code.toChar()
        if (c in 'A'..'Z' || c in 'a'..'z' || c in '0'..'9' || c in UNRESERVED_MARKS) {
            append(c)
        } else {
            append('%').append(HEX[code shr NIBBLE_BITS]).append(HEX[code and NIBBLE_MASK])
        }
    }
}

private const val UNRESERVED_MARKS = "-._~"
private const val HEX = "0123456789ABCDEF"
private const val BYTE_MASK = 0xFF
private const val NIBBLE_BITS = 4
private const val NIBBLE_MASK = 0x0F
