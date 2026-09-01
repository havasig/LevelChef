package com.levelchef.core.model

import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

@Serializable
data class CookingSession(
    val id: String,
    val recipeId: String,
    val recipeName: String,
    @Serializable(with = InstantIsoSerializer::class) val cookedAt: Instant,
    val xpEarned: Int,
    val rating: Int? = null,
    val improvementNote: String? = null,
    val kcal: Int? = null,
    val proteinGrams: Int? = null,
    val carbsGrams: Int? = null,
    val fatGrams: Int? = null,
)
