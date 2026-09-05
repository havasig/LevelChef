package com.levelchef.feature.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.levelchef.core.model.Allergen
import com.levelchef.core.model.CookingExperience
import com.levelchef.core.model.CookingGoal
import com.levelchef.core.model.Cuisine
import com.levelchef.core.model.DietaryPreference
import com.levelchef.core.model.HouseholdSize
import com.levelchef.core.model.SpiceTolerance
import com.levelchef.core.model.WeeknightTime
import com.levelchef.domain.repository.SurveyRepository
import com.levelchef.domain.usecase.SaveSurveyResponseUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Drives the first-launch survey wizard: tracks whether the survey is already done (from the DB),
 * the in-progress answers, and step navigation. On the final step it persists the response, after
 * which [SurveyRepository.observeResponse] flips [OnboardingUiState.completed].
 */
class OnboardingViewModel(
    surveyRepository: SurveyRepository,
    private val saveSurveyResponse: SaveSurveyResponseUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(OnboardingUiState())
    val uiState: StateFlow<OnboardingUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            surveyRepository.observeResponse().collect { response ->
                _uiState.update { it.copy(loading = false, completed = response != null) }
            }
        }
    }

    fun selectExperience(value: CookingExperience) = _uiState.update { it.copy(cookingExperience = value) }

    fun selectDiet(value: DietaryPreference) = _uiState.update { it.copy(dietaryPreference = value) }

    fun selectSpice(value: SpiceTolerance) = _uiState.update { it.copy(spiceTolerance = value) }

    fun selectGoal(value: CookingGoal) = _uiState.update { it.copy(cookingGoal = value) }

    fun selectTime(value: WeeknightTime) = _uiState.update { it.copy(weeknightTime = value) }

    fun selectHousehold(value: HouseholdSize) = _uiState.update { it.copy(householdSize = value) }

    fun toggleAllergen(value: Allergen) = _uiState.update {
        val next = if (value in it.allergens) it.allergens - value else it.allergens + value
        it.copy(allergens = next, noAllergies = false)
    }

    fun setNoAllergies() = _uiState.update { it.copy(allergens = emptySet(), noAllergies = true) }

    fun toggleCuisine(value: Cuisine) = _uiState.update {
        it.copy(cuisines = if (value in it.cuisines) it.cuisines - value else it.cuisines + value)
    }

    fun back() = _uiState.update { if (it.isFirstStep) it else it.copy(stepIndex = it.stepIndex - 1) }

    fun next() {
        val state = _uiState.value
        if (!state.canAdvance) return
        if (state.isLastStep) {
            submit(state)
        } else {
            _uiState.update { it.copy(stepIndex = it.stepIndex + 1) }
        }
    }

    private fun submit(state: OnboardingUiState) {
        val answers = state.answers ?: return
        viewModelScope.launch { saveSurveyResponse(answers) }
    }
}
