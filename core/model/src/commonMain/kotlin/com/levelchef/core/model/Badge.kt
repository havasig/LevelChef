package com.levelchef.core.model

import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

@Serializable
data class Badge(
    val id: String,
    val name: String,
    val category: BadgeCategory,
    val description: String,
    val progressCurrent: Int,
    val progressTarget: Int,
    @Serializable(with = InstantIsoSerializer::class) val earnedAt: Instant? = null,
) {
    val isEarned: Boolean get() = earnedAt != null
}
