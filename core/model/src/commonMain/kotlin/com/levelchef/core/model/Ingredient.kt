package com.levelchef.core.model

import kotlinx.serialization.Serializable

@Serializable
data class Ingredient(
    val name: String,
    val isNewToUser: Boolean = false,
)
