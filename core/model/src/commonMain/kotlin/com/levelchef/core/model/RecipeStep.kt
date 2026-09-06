package com.levelchef.core.model

import kotlinx.serialization.Serializable

/**
 * One numbered step in a [Recipe]. [timerMinutes], when set, drives the "Start timer" chip on the
 * recipe detail screen (a visual affordance for now — no running countdown yet).
 */
@Serializable
data class RecipeStep(
    val text: String,
    val timerMinutes: Int? = null,
)
