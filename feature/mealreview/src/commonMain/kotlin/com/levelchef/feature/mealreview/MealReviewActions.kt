package com.levelchef.feature.mealreview

/** Callbacks [MealReviewScreen] needs, bundled so the stateless screen stays easy to preview. */
data class MealReviewActions(
    val onBackClick: () -> Unit = {},
    val onRatingChange: (rating: Int) -> Unit = {},
    val onNoteChange: (note: String) -> Unit = {},
    val onDurationChange: (delta: Int) -> Unit = {},
    val onCaloriesChange: (delta: Int) -> Unit = {},
    val onProteinChange: (delta: Int) -> Unit = {},
    val onCarbsChange: (delta: Int) -> Unit = {},
    val onFatChange: (delta: Int) -> Unit = {},
    val onSaveClick: () -> Unit = {},
)
