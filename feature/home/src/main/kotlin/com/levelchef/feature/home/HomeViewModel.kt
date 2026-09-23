package com.levelchef.feature.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.levelchef.core.model.ChefLevel
import com.levelchef.domain.repository.CookingSessionRepository
import com.levelchef.domain.repository.RecipeRepository
import com.levelchef.domain.repository.UserProfileRepository
import com.levelchef.domain.repository.WeeklyChallengeRepository
import com.levelchef.domain.usecase.GetChefLevelUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/** Loads [HomeUiState] from the domain layer, including the active week's challenge from
 * [WeeklyChallengeRepository]. */
class HomeViewModel(
    private val userProfileRepository: UserProfileRepository,
    private val cookingSessionRepository: CookingSessionRepository,
    private val recipeRepository: RecipeRepository,
    private val weeklyChallengeRepository: WeeklyChallengeRepository,
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
            val challenge = weeklyChallengeRepository.observeCurrent().first()

            _uiState.value = HomeUiState(
                level = level,
                currentXp = profile.totalXp,
                xpForNextLevel = (ChefLevel.next(level) ?: level).xpThreshold,
                cookingSessions = profile.cookingSessionsCount,
                ingredientsTried = profile.newIngredientsCount,
                challengeId = challenge.id,
                challengeTitle = challenge.title,
                challengeXp = challenge.xpReward,
                challengeCompleted = challenge.isCompleted,
                challengeEligible = challenge.progressCurrent >= challenge.progressTarget,
                recommendations = recommendations.map { it.toRecommendation() },
                lastCooked = lastCookedSession?.toLastCooked(),
            )
        }
    }

    /** Marks the active week's challenge complete (a no-op unless it's actually eligible — see
     * [WeeklyChallengeRepository.complete]) and refreshes to reflect the awarded XP. */
    fun onChallengeDoneClick() {
        viewModelScope.launch {
            weeklyChallengeRepository.complete(_uiState.value.challengeId)
            refresh()
        }
    }
}
