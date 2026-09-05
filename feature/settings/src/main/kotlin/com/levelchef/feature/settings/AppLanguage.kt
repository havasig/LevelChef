package com.levelchef.feature.settings

/**
 * The user's app-language choice. [tag] is a BCP-47 language tag, or `null` for [SYSTEM] (follow the
 * OS languages).
 */
enum class AppLanguage(val tag: String?) {
    SYSTEM(null),
    ENGLISH("en"),
    HUNGARIAN("hu"),
}
