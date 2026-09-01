package com.levelchef.feature.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.levelchef.core.model.ChefLevel
import com.levelchef.domain.repository.CookingSessionRepository
import com.levelchef.domain.repository.RecipeRepository
import com.levelchef.domain.repository.UserProfileRepository
import com.levelchef.domain.usecase.GetChefLevelUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/** Loads [HomeUiState] from the domain layer. Weekly-challenge fields stay at their [HomeUiState] defaults
 * until a WeeklyChallenge repository exists. */
class HomeViewModel(
    private val userProfileRepository: UserProfileRepository,
    private val cookingSessionRepository: CookingSessionRepository,
    private val recipeRepository: RecipeRepository,
    private val getChefLevelUseCase: GetChefLevelUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            val profile = userProfileRepository.getProfile()
            val level = getChefLevelUseCase()
            val lastCookedSession = cookingSessionRepository.mostRecent()
            val recommendations = recipeRepository.getRecommendations()

            _uiState.value = HomeUiState(
                levelLabel = level.displayName,
                currentXp = profile.totalXp,
                xpForNextLevel = (ChefLevel.next(level) ?: level).xpThreshold,
                cookingSessions = profile.cookingSessionsCount,
                ingredientsTried = profile.newIngredientsCount,
                recommendations = recommendations.map { it.toRecommendation() },
                lastCooked = lastCookedSession?.toLastCooked(),
            )
        }
    }
}
