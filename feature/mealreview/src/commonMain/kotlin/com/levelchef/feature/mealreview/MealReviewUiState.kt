package com.levelchef.feature.mealreview

/** Screen model for [MealReviewScreen] — logging a "Log experience" review after cooking a recipe. */
data class MealReviewUiState(
    val loading: Boolean = true,
    val recipeName: String = "",
    val xpReward: Int = 0,
    /** 0 means unrated; the Save button stays disabled until this is set. */
    val rating: Int = 0,
    val note: String = "",
    val durationMinutes: Int = 0,
    val caloriesKcal: Int = 0,
    val proteinGrams: Int = 0,
    val carbsGrams: Int = 0,
    val fatGrams: Int = 0,
    val ingredientLines: List<String> = emptyList(),
    /** One-shot signal: flips to true once Save has recorded the session, for [MealReviewRoute]
     * to navigate away. */
    val saved: Boolean = false,
) {
    companion object {
        const val MAX_RATING = 5
        const val DURATION_STEP_MINUTES = 5
        const val CALORIES_STEP = 10
        const val GRAMS_STEP = 1
    }
}
