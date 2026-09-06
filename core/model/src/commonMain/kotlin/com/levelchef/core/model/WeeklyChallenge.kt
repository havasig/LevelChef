package com.levelchef.core.model

import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

@Serializable
data class WeeklyChallenge(
    val id: String,
    val title: String,
    val description: String,
    val xpReward: Int,
    val progressCurrent: Int = 0,
    val progressTarget: Int = 1,
    @Serializable(with = InstantIsoSerializer::class) val completedAt: Instant? = null,
) {
    val isCompleted: Boolean get() = completedAt != null
}
