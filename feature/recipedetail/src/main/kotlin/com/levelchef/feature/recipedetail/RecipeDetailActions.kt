package com.levelchef.feature.recipedetail

/** Callbacks [RecipeDetailScreen] needs, bundled so the stateless screen stays easy to preview. */
data class RecipeDetailActions(
    val onBackClick: () -> Unit = {},
    val onSettingsClick: () -> Unit = {},
    val onServingsChange: (delta: Int) -> Unit = {},
    val onIngredientToggle: (index: Int) -> Unit = {},
    val onToggleSaved: () -> Unit = {},
    val onMadeIt: () -> Unit = {},
    val onStartTimer: () -> Unit = {},
    val onOpenVideo: (url: String) -> Unit = {},
    val onDismissMessage: () -> Unit = {},
)
