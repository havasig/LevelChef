package com.levelchef.data.repository

import com.levelchef.core.model.UserProfile
import com.levelchef.domain.repository.CookingSessionRepository
import com.levelchef.domain.repository.IngredientRepository
import com.levelchef.domain.repository.UserProfileRepository

/** Derives the aggregate [UserProfile] from cooking session history and the pantry. */
class UserProfileRepositoryImpl(
    private val cookingSessionRepository: CookingSessionRepository,
    private val ingredientRepository: IngredientRepository,
) : UserProfileRepository {

    override suspend fun getProfile(): UserProfile = UserProfile(
        totalXp = cookingSessionRepository.totalXp(),
        cookingSessionsCount = cookingSessionRepository.sessionCount(),
        newIngredientsCount = ingredientRepository.count(),
    )
}
