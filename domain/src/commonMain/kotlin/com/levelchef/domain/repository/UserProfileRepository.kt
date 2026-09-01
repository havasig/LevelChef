package com.levelchef.domain.repository

import com.levelchef.core.model.UserProfile

/** Source of the current user's aggregate profile stats. */
interface UserProfileRepository {
    suspend fun getProfile(): UserProfile
}
