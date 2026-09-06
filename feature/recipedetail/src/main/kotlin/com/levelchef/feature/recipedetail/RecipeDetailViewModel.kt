package com.levelchef.feature.recipedetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.levelchef.domain.repository.RecipeRepository
import com.levelchef.domain.repository.SavedRecipeRepository
import com.levelchef.domain.usecase.RecordCookingSessionUseCase
import com.levelchef.feature.recipedetail.RecipeDetailUiState.Companion.MAX_SERVINGS
import com.levelchef.feature.recipedetail.RecipeDetailUiState.Companion.MIN_SERVINGS
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class RecipeDetailViewModel(
    private val recipeId: String,
    recipeRepository: RecipeRepository,
    private val savedRecipeRepository: SavedRecipeRepository,
    private val recordCookingSession: RecordCookingSessionUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(RecipeDetailUiState())
    val uiState: StateFlow<RecipeDetailUiState> = _uiState.asStateFlow()

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

    fun markCooked() {
        val recipe = _uiState.value.recipe ?: return
        viewModelScope.launch { recordCookingSession(recipe) }
        _uiState.update { it.copy(transientMessage = TransientMessage.COOKED) }
    }

    fun showTimerStub() = _uiState.update { it.copy(transientMessage = TransientMessage.TIMER_STUB) }

    fun dismissMessage() = _uiState.update { it.copy(transientMessage = null) }
}
