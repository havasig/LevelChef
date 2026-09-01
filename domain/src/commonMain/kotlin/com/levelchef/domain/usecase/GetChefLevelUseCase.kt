package com.levelchef.domain.usecase

import com.levelchef.core.model.ChefLevel
import com.levelchef.domain.repository.UserProfileRepository

/** Derives the user's current [ChefLevel] from their total XP. */
class GetChefLevelUseCase(private val userProfileRepository: UserProfileRepository) {
    suspend operator fun invoke(): ChefLevel = ChefLevel.forXp(userProfileRepository.getProfile().totalXp)
}
