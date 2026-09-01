package com.levelchef.core.model

import kotlinx.serialization.Serializable

@Serializable
data class UserProfile(
    val totalXp: Int,
    val cookingSessionsCount: Int,
    val newIngredientsCount: Int,
)
