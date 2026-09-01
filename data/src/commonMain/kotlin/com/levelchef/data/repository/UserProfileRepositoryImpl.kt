package com.levelchef.data.repository

import com.levelchef.core.model.UserProfile
import com.levelchef.domain.repository.CookingSessionRepository
import com.levelchef.domain.repository.UserProfileRepository

/**
 * Derives the aggregate [UserProfile] from cooking session history.
 * TODO: track distinct new ingredients once ingredient logging is wired up; hardcoded to 0 for now.
 */
class UserProfileRepositoryImpl(
    private val cookingSessionRepository: CookingSessionRepository,
) : UserProfileRepository {

    override suspend fun getProfile(): UserProfile = UserProfile(
        totalXp = cookingSessionRepository.totalXp(),
        cookingSessionsCount = cookingSessionRepository.sessionCount(),
        newIngredientsCount = 0,
    )
}
