package com.levelchef.feature.recipedetail

/** Callbacks [RecipeDetailScreen] needs, bundled so the stateless screen stays easy to preview. */
data class RecipeDetailActions(
    val onBackClick: () -> Unit = {},
    val onSettingsClick: () -> Unit = {},
    val onServingsChange: (delta: Int) -> Unit = {},
    val onIngredientToggle: (index: Int) -> Unit = {},
    val onToggleSaved: () -> Unit = {},
    /** Opens "Log experience" for the serving count the user cooked (the stepper value). */
    val onMadeIt: (servings: Int) -> Unit = {},
    val onStartTimer: (stepIndex: Int, minutes: Int) -> Unit = { _, _ -> },
    val onCancelTimer: () -> Unit = {},
    val onOpenVideo: (url: String) -> Unit = {},
    val onDismissMessage: () -> Unit = {},
)
