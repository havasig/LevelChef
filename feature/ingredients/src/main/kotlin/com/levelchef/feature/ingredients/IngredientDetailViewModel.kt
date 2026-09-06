package com.levelchef.feature.ingredients

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.levelchef.domain.repository.IngredientRepository
import com.levelchef.domain.usecase.DeleteIngredientUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class IngredientDetailViewModel(
    private val ingredientId: String,
    ingredientRepository: IngredientRepository,
    private val deleteIngredient: DeleteIngredientUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(IngredientDetailUiState())
    val uiState: StateFlow<IngredientDetailUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            ingredientRepository.observeAll().collect { all ->
                _uiState.update { it.copy(loading = false, ingredient = all.firstOrNull { row -> row.id == ingredientId }) }
            }
        }
    }

    fun delete() {
        viewModelScope.launch {
            deleteIngredient(ingredientId)
            _uiState.update { it.copy(deleted = true) }
        }
    }
}
