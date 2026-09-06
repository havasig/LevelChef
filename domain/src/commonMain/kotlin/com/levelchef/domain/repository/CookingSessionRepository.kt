package com.levelchef.domain.repository

import com.levelchef.core.model.CookingSession
import kotlinx.coroutines.flow.Flow

/** Persistence boundary for logged cooking sessions. */
interface CookingSessionRepository {
    fun observeAll(): Flow<List<CookingSession>>
    suspend fun recordSession(session: CookingSession)
    suspend fun mostRecent(): CookingSession?
    suspend fun totalXp(): Int
    suspend fun sessionCount(): Int

    /** Wipes the entire cooking history — backs account deletion. */
    suspend fun deleteAll()
}
