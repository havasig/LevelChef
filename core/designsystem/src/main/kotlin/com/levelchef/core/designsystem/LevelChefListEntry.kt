package com.levelchef.core.designsystem

/** A single row rendered by [LevelChefList]. */
data class LevelChefListEntry(
    val avatarInitials: String,
    val title: String,
    val subtitle: String,
    val badgeText: String? = "Active",
)
