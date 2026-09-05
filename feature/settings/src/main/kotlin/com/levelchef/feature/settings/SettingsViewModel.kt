package com.levelchef.feature.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.levelchef.domain.usecase.ClearSurveyResponseUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val controller: AppSettingsController,
    private val clearSurveyResponse: ClearSurveyResponseUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        SettingsUiState(themeMode = controller.themeMode(), language = controller.language()),
    )
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    /** Applying either change recreates the activity; the update keeps state right for the retained VM. */
    fun setThemeMode(mode: ThemeMode) {
        _uiState.update { it.copy(themeMode = mode) }
        controller.setThemeMode(mode)
    }

    fun setLanguage(language: AppLanguage) {
        _uiState.update { it.copy(language = language) }
        controller.setLanguage(language)
    }

    /**
     * Wipes the stored survey response. `OnboardingGate` observes that row, so the mandatory wizard
     * reappears on its own — this backs both the "retake the survey" action and the developer
     * "clear onboarding storage" option.
     */
    fun clearOnboarding() {
        viewModelScope.launch { clearSurveyResponse() }
    }
}
