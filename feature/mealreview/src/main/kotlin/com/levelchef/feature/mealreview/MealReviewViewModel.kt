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

class MealReviewViewModel(
    private val recipeId: String,
    private val recipeRepository: RecipeRepository,
    private val recordCookingSession: RecordCookingSessionUseCase,
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
                    xpReward = loaded?.xpReward ?: 0,
                    caloriesKcal = loaded?.caloriesKcal ?: 0,
                    proteinGrams = loaded?.proteinGrams ?: 0,
                    carbsGrams = loaded?.carbsGrams ?: 0,
                    fatGrams = loaded?.fatGrams ?: 0,
                    ingredientLines = loaded?.ingredients.orEmpty().map { ingredient -> ingredient.toDisplayLine() },
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

private fun RecipeIngredient.toDisplayLine(): String {
    val amount = quantity?.let { raw ->
        val formatted = if (raw == raw.toLong().toDouble()) raw.toLong().toString() else raw.toString()
        unit?.let { "$formatted $it" } ?: formatted
    }
    return if (amount != null) "$amount $name" else name
}
