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

    /** Ids seen so far; null until the first emission (whose items are not "new"). */
    private var knownIds: Set<String>? = null

    /** Categories that received an ingredient while this screen was open (e.g. via the add form). */
    private val expandedCategories = mutableSetOf<IngredientCategory>()

    init {
        viewModelScope.launch {
            ingredientRepository.observeAll().collect { all ->
                knownIds?.let { known ->
                    all.filter { it.id !in known }.mapTo(expandedCategories) { it.category }
                }
                knownIds = all.mapTo(mutableSetOf()) { it.id }
                _uiState.value = IngredientsListUiState(loading = false, sections = groupByCategory(all))
            }
        }
    }

    private fun groupByCategory(all: List<Ingredient>): List<IngredientCategorySection> =
        IngredientCategory.entries.mapNotNull { category ->
            all.filter { it.category == category }
                .takeIf { it.isNotEmpty() }
                ?.let { IngredientCategorySection(category, it, expanded = category in expandedCategories) }
        }
}
