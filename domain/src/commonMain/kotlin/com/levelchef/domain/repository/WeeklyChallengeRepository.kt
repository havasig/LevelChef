package com.levelchef.domain.repository

import com.levelchef.core.model.WeeklyChallenge
import kotlinx.coroutines.flow.Flow

/** Source of the current week's [WeeklyChallenge] — one is deterministically picked from the
 * catalog per calendar week and its progress is derived from that week's cooking sessions. */
interface WeeklyChallengeRepository {

    /** Emits the active week's challenge, re-emitting as matching cooking sessions are logged. */
    fun observeCurrent(): Flow<WeeklyChallenge>

    /** Marks [id] complete, awarding its XP — a no-op unless [id] is the active week's challenge,
     * it isn't already completed, and its progress has reached its target. */
    suspend fun complete(id: String)

    /** Sum of XP awarded by every completed weekly challenge to date — folded into the user's total XP. */
    suspend fun totalAwardedXp(): Int
}
