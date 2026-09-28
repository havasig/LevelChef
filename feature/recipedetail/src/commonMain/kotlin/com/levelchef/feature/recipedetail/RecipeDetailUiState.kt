package com.levelchef.feature.recipedetail

import com.levelchef.core.model.Recipe

/** Screen model for [RecipeDetailScreen]. */
data class RecipeDetailUiState(
    val loading: Boolean = true,
    val recipe: Recipe? = null,
    /** The user-chosen serving count; starts at the recipe's own [Recipe.servings]. */
    val servings: Int = DEFAULT_SERVINGS,
    /** Indices into [Recipe.ingredients] the user has ticked off. Not persisted. */
    val checkedIngredients: Set<Int> = emptySet(),
    val isSaved: Boolean = false,
    /** Index into [Recipe.steps] with an active countdown, or null when no timer is running. */
    val runningTimerStepIndex: Int? = null,
    /** Seconds left on [runningTimerStepIndex]'s countdown; meaningless while that's null. */
    val timerSecondsRemaining: Int = 0,
    /** A transient confirmation to surface in the snackbar, or null when nothing is pending. */
    val transientMessage: TransientMessage? = null,
) {
    companion object {
        const val MIN_SERVINGS = 1
        const val MAX_SERVINGS = 12
        const val DEFAULT_SERVINGS = 2
    }
}

/** The one-off confirmations the recipe detail screen flashes in its snackbar. */
enum class TransientMessage { SAVED, UNSAVED, TIMER_DONE }
