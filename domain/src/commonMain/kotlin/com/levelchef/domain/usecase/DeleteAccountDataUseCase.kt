package com.levelchef.domain.usecase

import com.levelchef.domain.repository.CookingSessionRepository
import com.levelchef.domain.repository.IngredientRepository

/**
 * Wipes every piece of local data owned by the domain layer — the pantry and the cooking history —
 * as part of account deletion. The survey response is cleared separately by the caller (see
 * `SettingsViewModel.deleteAccount`), since that clear is what makes the mandatory onboarding
 * survey reappear and must happen only after the deletion is confirmed to the user.
 */
class DeleteAccountDataUseCase(
    private val ingredientRepository: IngredientRepository,
    private val cookingSessionRepository: CookingSessionRepository,
) {
    suspend operator fun invoke() {
        ingredientRepository.deleteAll()
        cookingSessionRepository.deleteAll()
    }
}
