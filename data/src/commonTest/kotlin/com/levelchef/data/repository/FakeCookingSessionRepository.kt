package com.levelchef.data.repository

import com.levelchef.core.model.CookingSession
import com.levelchef.domain.repository.CookingSessionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

/** Hand-written fake following the `Fake…Repository` pattern from `GetChefLevelUseCaseTest`. */
internal class FakeCookingSessionRepository(
    private val xp: Int = 0,
    private val count: Int = 0,
    private val recent: CookingSession? = null,
    private val all: List<CookingSession> = emptyList(),
    private val durationMinutes: Int = 0,
) : CookingSessionRepository {
    val recorded = mutableListOf<CookingSession>()

    override fun observeAll(): Flow<List<CookingSession>> = flowOf(all)
    override suspend fun recordSession(session: CookingSession) {
        recorded += session
    }
    override suspend fun mostRecent(): CookingSession? = recent
    override suspend fun totalXp(): Int = xp
    override suspend fun sessionCount(): Int = count
    override suspend fun totalDurationMinutes(): Int = durationMinutes
}
