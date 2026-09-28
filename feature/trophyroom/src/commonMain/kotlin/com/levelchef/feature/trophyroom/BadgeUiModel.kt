package com.levelchef.feature.trophyroom

/** Presentation model for one badge tile — [com.levelchef.core.model.Badge] mapped for this screen. */
data class BadgeUiModel(
    val id: String,
    val emoji: String,
    val name: String,
    val description: String,
    val progressCurrent: Int,
    val progressTarget: Int,
    val earned: Boolean,
)
