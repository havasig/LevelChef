package com.levelchef.feature.recipedetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.levelchef.domain.repository.RecipeRepository
import com.levelchef.domain.repository.SavedRecipeRepository
import com.levelchef.feature.recipedetail.RecipeDetailUiState.Companion.MAX_SERVINGS
import com.levelchef.feature.recipedetail.RecipeDetailUiState.Companion.MIN_SERVINGS
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

private const val SECONDS_PER_MINUTE = 60
private const val TICK_MS = 1000L

class RecipeDetailViewModel(
    private val recipeId: String,
    recipeRepository: RecipeRepository,
    private val savedRecipeRepository: SavedRecipeRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(RecipeDetailUiState())
    val uiState: StateFlow<RecipeDetailUiState> = _uiState.asStateFlow()

    private var timerJob: Job? = null

    init {
        viewModelScope.launch {
            val recipe = recipeRepository.getById(recipeId)
            _uiState.update {
                it.copy(loading = false, recipe = recipe, servings = recipe?.servings ?: it.servings)
            }
        }
        viewModelScope.launch {
            savedRecipeRepository.observeIsSaved(recipeId).collect { saved ->
                _uiState.update { it.copy(isSaved = saved) }
            }
        }
    }

    fun changeServings(delta: Int) = _uiState.update {
        it.copy(servings = (it.servings + delta).coerceIn(MIN_SERVINGS, MAX_SERVINGS))
    }

    fun toggleIngredient(index: Int) = _uiState.update {
        val checked = it.checkedIngredients
        it.copy(checkedIngredients = if (index in checked) checked - index else checked + index)
    }

    fun toggleSaved() {
        val nowSaved = !_uiState.value.isSaved
        viewModelScope.launch { savedRecipeRepository.setSaved(recipeId, nowSaved) }
        _uiState.update {
            it.copy(transientMessage = if (nowSaved) TransientMessage.SAVED else TransientMessage.UNSAVED)
        }
    }

    /** Starts a countdown for [stepIndex]; a timer already running on another step is cancelled. */
    fun startTimer(stepIndex: Int, minutes: Int) {
        timerJob?.cancel()
        _uiState.update { it.copy(runningTimerStepIndex = stepIndex, timerSecondsRemaining = minutes * SECONDS_PER_MINUTE) }
        timerJob = viewModelScope.launch {
            while (_uiState.value.timerSecondsRemaining > 0) {
                delay(TICK_MS)
                _uiState.update { it.copy(timerSecondsRemaining = it.timerSecondsRemaining - 1) }
            }
            _uiState.update { it.copy(runningTimerStepIndex = null, transientMessage = TransientMessage.TIMER_DONE) }
        }
    }

    fun cancelTimer() {
        timerJob?.cancel()
        timerJob = null
        _uiState.update { it.copy(runningTimerStepIndex = null, timerSecondsRemaining = 0) }
    }

    fun dismissMessage() = _uiState.update { it.copy(transientMessage = null) }
}
