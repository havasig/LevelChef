package com.levelchef.domain.repository

import com.levelchef.core.model.Badge
import kotlinx.coroutines.flow.Flow

/** Source of the user's [Badge] collection — progress is always derived live from cooking-session
 * and pantry data; only the moment a badge is first earned is persisted. */
interface BadgeRepository {

    /** Emits every catalog badge with its current progress and (if reached) earned date. */
    fun observeAll(): Flow<List<Badge>>

    /** Persists the earned date for any badge whose progress has newly reached its target. */
    suspend fun refreshEarned()
}
