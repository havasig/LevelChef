package com.levelchef.core.model

import kotlinx.serialization.Serializable

@Serializable
data class WeeklyChallenge(
    val id: String,
    val title: String,
    val description: String,
    val xpReward: Int,
    val isCompleted: Boolean = false,
)
