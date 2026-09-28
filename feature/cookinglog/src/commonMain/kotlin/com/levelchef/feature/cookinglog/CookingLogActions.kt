package com.levelchef.feature.cookinglog

/** Callbacks [CookingLogScreen] needs, bundled so the stateless screen stays easy to preview. */
data class CookingLogActions(
    val onQueryChange: (String) -> Unit = {},
    val onTabSelected: (CookingLogTab) -> Unit = {},
    val onRecipeClick: (recipeId: String) -> Unit = {},
    val onDeleteClick: (recipeId: String) -> Unit = {},
)
