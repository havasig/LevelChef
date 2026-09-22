package com.levelchef.feature.cookinglog

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.levelchef.domain.repository.CookingSessionRepository
import com.levelchef.domain.repository.RecipeRepository
import com.levelchef.domain.repository.SavedRecipeRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class CookingLogViewModel(
    private val savedRecipeRepository: SavedRecipeRepository,
    private val cookingSessionRepository: CookingSessionRepository,
    recipeRepository: RecipeRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(CookingLogUiState())
    val uiState: StateFlow<CookingLogUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val recipes = recipeRepository.getRecommendations()
            combine(
                savedRecipeRepository.observeSavedRecipeIds(),
                cookingSessionRepository.observeAll(),
            ) { savedIds, sessions -> toSavedRecipeItems(savedIds, recipes, sessions) }
                .collect { items -> _uiState.update { it.copy(loading = false, allItems = items) } }
        }
    }

    fun changeQuery(query: String) = _uiState.update { it.copy(query = query) }

    fun selectTab(tab: CookingLogTab) = _uiState.update { it.copy(selectedTab = tab) }

    fun deleteRecipe(recipeId: String) {
        viewModelScope.launch { savedRecipeRepository.setSaved(recipeId, false) }
    }
}
