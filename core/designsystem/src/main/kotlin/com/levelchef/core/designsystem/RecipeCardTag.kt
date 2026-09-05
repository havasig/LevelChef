package com.levelchef.core.designsystem

/** The optional tag shown on a [LevelChefRecipeCard] — bundles the [LevelChefTag] inputs. */
data class RecipeCardTag(
    val label: String,
    val emoji: String? = null,
    val color: TagColor = TagColor.PURPLE,
)
