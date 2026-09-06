package com.levelchef.feature.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.levelchef.core.model.CookingExperience
import com.levelchef.core.model.DietaryPreference
import com.levelchef.core.model.HouseholdSize
import com.levelchef.core.model.SurveyResponse
import com.levelchef.domain.repository.SurveyRepository
import com.levelchef.domain.usecase.ClearSurveyResponseUseCase
import com.levelchef.domain.usecase.DeleteAccountDataUseCase
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val controller: AppSettingsController,
    private val clearSurveyResponse: ClearSurveyResponseUseCase,
    private val surveyRepository: SurveyRepository,
    private val deleteAccountData: DeleteAccountDataUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        SettingsUiState(themeMode = controller.themeMode(), language = controller.language()),
    )
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    // Settings is only reachable once onboarding is complete, so this is non-null in practice —
    // kept nullable defensively since `updateSurvey` is a no-op until the first emission arrives.
    private var latestSurveyResponse: SurveyResponse? = null

    init {
        viewModelScope.launch {
            surveyRepository.observeResponse().collect { response ->
                latestSurveyResponse = response
                if (response != null) {
                    _uiState.update {
                        it.copy(
                            dietaryPreference = response.dietaryPreference,
                            cookingExperience = response.cookingExperience,
                            householdSize = response.householdSize,
                        )
                    }
                }
            }
        }
    }

    /** Applying either change recreates the activity; the update keeps state right for the retained VM. */
    fun setThemeMode(mode: ThemeMode) {
        _uiState.update { it.copy(themeMode = mode) }
        controller.setThemeMode(mode)
    }

    fun setLanguage(language: AppLanguage) {
        _uiState.update { it.copy(language = language) }
        controller.setLanguage(language)
    }

    /** Changes one cooking-preference field at a time and saves it immediately — no full re-survey. */
    fun setDietaryPreference(preference: DietaryPreference) = updateSurvey { it.copy(dietaryPreference = preference) }

    fun setCookingExperience(experience: CookingExperience) = updateSurvey { it.copy(cookingExperience = experience) }

    fun setHouseholdSize(size: HouseholdSize) = updateSurvey { it.copy(householdSize = size) }

    private fun updateSurvey(update: (SurveyResponse) -> SurveyResponse) {
        val current = latestSurveyResponse ?: return
        viewModelScope.launch { surveyRepository.save(update(current)) }
    }

    /** No feedback backend exists yet — this just confirms locally that the message was captured. */
    fun submitFeedback(text: String) {
        if (text.isBlank()) return
        showSnackbarThenDismiss(SettingsSnackbarMessage.FEEDBACK_SENT)
    }

    /**
     * Wipes every local ingredient, cooking session and the survey response, and resets the theme
     * and language back to their System defaults. The survey response is cleared *last*, after the
     * success message has had time to show: `OnboardingGate` observes it and swaps this whole
     * screen out for the mandatory survey the instant it becomes null.
     */
    fun deleteAccount() {
        viewModelScope.launch {
            deleteAccountData()
            _uiState.update { it.copy(snackbarMessage = SettingsSnackbarMessage.ACCOUNT_DELETION_SUCCESS) }
            delay(SNACKBAR_DISPLAY_MS)
            controller.resetToDefaults()
            clearSurveyResponse()
        }
    }

    private fun showSnackbarThenDismiss(message: SettingsSnackbarMessage) {
        viewModelScope.launch {
            _uiState.update { it.copy(snackbarMessage = message) }
            delay(SNACKBAR_DISPLAY_MS)
            _uiState.update { it.copy(snackbarMessage = null) }
        }
    }

    /**
     * Wipes the stored survey response. `OnboardingGate` observes that row, so the mandatory wizard
     * reappears on its own — this backs the developer "clear onboarding storage" option.
     */
    fun clearOnboarding() {
        viewModelScope.launch { clearSurveyResponse() }
    }

    private companion object {
        const val SNACKBAR_DISPLAY_MS = 2000L
    }
}
