package com.levelchef.feature.mealreview

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.levelchef.core.model.Recipe
import com.levelchef.core.model.RecipeIngredient
import com.levelchef.domain.repository.RecipeRepository
import com.levelchef.domain.usecase.RecordCookingSessionUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.math.roundToLong

class MealReviewViewModel(
    private val recipeId: String,
    private val recipeRepository: RecipeRepository,
    private val recordCookingSession: RecordCookingSessionUseCase,
    /** The serving count cooked on the recipe-detail stepper; `null` means the recipe's own servings. */
    private val servings: Int? = null,
) : ViewModel() {

    private val _uiState = MutableStateFlow(MealReviewUiState())
    val uiState: StateFlow<MealReviewUiState> = _uiState.asStateFlow()

    private var recipe: Recipe? = null

    init {
        viewModelScope.launch {
            val loaded = recipeRepository.getById(recipeId)
            recipe = loaded
            _uiState.update {
                it.copy(
                    loading = false,
                    recipeName = loaded?.name.orEmpty(),
                    durationMinutes = loaded?.timeMinutes ?: 0,
                    xpReward = loaded?.xpReward ?: 0,
                    caloriesKcal = loaded?.caloriesKcal ?: 0,
                    proteinGrams = loaded?.proteinGrams ?: 0,
                    carbsGrams = loaded?.carbsGrams ?: 0,
                    fatGrams = loaded?.fatGrams ?: 0,
                    ingredientLines = loaded?.let { found ->
                        found.ingredients.map { ingredient -> ingredient.toDisplayLine(servings ?: found.servings, found.servings) }
                    }.orEmpty(),
                )
            }
        }
    }

    fun changeRating(rating: Int) = _uiState.update {
        it.copy(rating = rating.coerceIn(0, MealReviewUiState.MAX_RATING))
    }

    fun changeNote(note: String) = _uiState.update { it.copy(note = note) }

    fun changeDuration(delta: Int) = _uiState.update {
        it.copy(durationMinutes = (it.durationMinutes + delta).coerceAtLeast(0))
    }

    fun changeCalories(delta: Int) = _uiState.update {
        it.copy(caloriesKcal = (it.caloriesKcal + delta).coerceAtLeast(0))
    }

    fun changeProtein(delta: Int) = _uiState.update {
        it.copy(proteinGrams = (it.proteinGrams + delta).coerceAtLeast(0))
    }

    fun changeCarbs(delta: Int) = _uiState.update {
        it.copy(carbsGrams = (it.carbsGrams + delta).coerceAtLeast(0))
    }

    fun changeFat(delta: Int) = _uiState.update {
        it.copy(fatGrams = (it.fatGrams + delta).coerceAtLeast(0))
    }

    fun save() {
        val recipe = recipe ?: return
        val state = _uiState.value
        viewModelScope.launch {
            recordCookingSession(
                recipe,
                rating = state.rating,
                improvementNote = state.note.ifBlank { null },
                durationMinutes = state.durationMinutes,
                kcal = state.caloriesKcal,
                proteinGrams = state.proteinGrams,
                carbsGrams = state.carbsGrams,
                fatGrams = state.fatGrams,
            )
            // Only after the write: `saved` makes the route pop this screen, which clears the
            // ViewModel and would cancel a still-running insert.
            _uiState.update { it.copy(saved = true) }
        }
    }
}

/**
 * One ingredient line, scaled from the recipe's [baseServings] to the [servings] actually cooked —
 * mirrors `feature:recipedetail`'s mapper (features can't depend on each other) so both screens agree.
 */
private fun RecipeIngredient.toDisplayLine(servings: Int, baseServings: Int): String {
    val rawQuantity = quantity ?: return name
    val scaled = rawQuantity * servings / baseServings.coerceAtLeast(1)
    val amount = if (scaled % 1.0 == 0.0) {
        scaled.roundToLong().toString()
    } else {
        ((scaled * HUNDREDTHS).roundToLong() / HUNDREDTHS).toString()
    }
    return listOfNotNull(amount, unit, name).joinToString(" ")
}

private const val HUNDREDTHS = 100.0
