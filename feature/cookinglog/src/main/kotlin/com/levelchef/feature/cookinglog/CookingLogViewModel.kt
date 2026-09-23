package com.levelchef.feature.cookinglog

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.levelchef.domain.repository.CookingSessionRepository
import com.levelchef.domain.repository.RecipeRepository
import com.levelchef.domain.repository.SavedRecipeRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class CookingLogViewModel(
    private val savedRecipeRepository: SavedRecipeRepository,
    private val cookingSessionRepository: CookingSessionRepository,
    private val recipeRepository: RecipeRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(CookingLogUiState())
    val uiState: StateFlow<CookingLogUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            combine(
                savedRecipeRepository.observeSavedRecipeIds(),
                cookingSessionRepository.observeAll(),
            ) { savedIds, sessions -> savedIds to sessions }
                .map { (savedIds, sessions) -> toSavedRecipeItems(savedIds, resolveRecipes(savedIds), sessions) }
                .collect { items -> _uiState.update { it.copy(loading = false, allItems = items) } }
        }
    }

    // Saved/cooked recipes may no longer be in a fresh getRecommendations() batch (recommendations
    // rotate with the survey), so each saved id is resolved individually against the full cache.
    private suspend fun resolveRecipes(recipeIds: List<String>) = coroutineScope {
        recipeIds.map { id -> async { recipeRepository.getById(id) } }.awaitAll().filterNotNull()
    }

    fun changeQuery(query: String) = _uiState.update { it.copy(query = query) }

    fun selectTab(tab: CookingLogTab) = _uiState.update { it.copy(selectedTab = tab) }

    fun deleteRecipe(recipeId: String) {
        viewModelScope.launch { savedRecipeRepository.setSaved(recipeId, false) }
    }
}
