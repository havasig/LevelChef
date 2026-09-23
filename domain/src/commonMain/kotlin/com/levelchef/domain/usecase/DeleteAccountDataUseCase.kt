package com.levelchef.domain.usecase

import com.levelchef.domain.repository.BadgeRepository
import com.levelchef.domain.repository.CookingSessionRepository
import com.levelchef.domain.repository.IngredientRepository
import com.levelchef.domain.repository.SavedRecipeRepository
import com.levelchef.domain.repository.WeeklyChallengeRepository

/**
 * Wipes every piece of local data owned by the domain layer — the pantry, the cooking history,
 * saved recipes, earned badges and weekly-challenge progress/XP — as part of account deletion, then
 * re-seeds the starter pantry so the app is left exactly as on a fresh install. The survey response
 * is cleared separately by the caller (see `SettingsViewModel.deleteAccount`), since that clear is
 * what makes the mandatory onboarding survey reappear and must happen only after the deletion is
 * confirmed to the user.
 */
class DeleteAccountDataUseCase(
    private val ingredientRepository: IngredientRepository,
    private val cookingSessionRepository: CookingSessionRepository,
    private val savedRecipeRepository: SavedRecipeRepository,
    private val badgeRepository: BadgeRepository,
    private val weeklyChallengeRepository: WeeklyChallengeRepository,
) {
    suspend operator fun invoke() {
        cookingSessionRepository.deleteAll()
        savedRecipeRepository.deleteAll()
        badgeRepository.deleteAll()
        weeklyChallengeRepository.deleteAll()
        ingredientRepository.deleteAll()
        ingredientRepository.seedDefaults()
    }
}
