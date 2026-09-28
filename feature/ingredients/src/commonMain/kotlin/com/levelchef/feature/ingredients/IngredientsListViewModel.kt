package com.levelchef.feature.ingredients

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.levelchef.core.model.Ingredient
import com.levelchef.core.model.IngredientCategory
import com.levelchef.domain.repository.IngredientRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class IngredientsListViewModel(
    ingredientRepository: IngredientRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(IngredientsListUiState())
    val uiState: StateFlow<IngredientsListUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            ingredientRepository.observeAll().collect { all ->
                _uiState.value = IngredientsListUiState(loading = false, sections = groupByCategory(all))
            }
        }
    }

    private fun groupByCategory(all: List<Ingredient>): List<IngredientCategorySection> =
        IngredientCategory.entries.mapNotNull { category ->
            all.filter { it.category == category }
                .takeIf { it.isNotEmpty() }
                ?.let { IngredientCategorySection(category, it) }
        }
}
