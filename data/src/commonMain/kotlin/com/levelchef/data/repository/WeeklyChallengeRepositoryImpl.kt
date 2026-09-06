@file:OptIn(ExperimentalTime::class)

package com.levelchef.data.repository

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToOneOrNull
import com.levelchef.core.database.db.LevelChefDatabase
import com.levelchef.core.model.CookingSession
import com.levelchef.core.model.WeeklyChallenge
import com.levelchef.domain.repository.CookingSessionRepository
import com.levelchef.domain.repository.WeeklyChallengeRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.onStart
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

/** One catalog entry: how a challenge's progress is read off that week's sessions, capped at [target]. */
private class ChallengeDefinition(
    val id: String,
    val title: String,
    val description: String,
    val xpReward: Int,
    val target: Int,
    val progress: (List<CookingSession>) -> Int,
)

/**
 * Static catalog of 9 weekly challenges — a mix of one-time (target 1) and multi-count tasks,
 * evaluated against that week's cooking sessions. One is deterministically picked per calendar
 * week (see [WeeklyChallengeRepositoryImpl.weekKeyFor]).
 */
private val CATALOG = listOf(
    ChallengeDefinition(
        "three-meals", "Three Home-Cooked Meals",
        "Log 3 cooking sessions before the week is out.", xpReward = 150, target = 3,
    ) { it.size },
    ChallengeDefinition(
        "five-star-plate", "Plate of the Week",
        "Rate one meal a full 5 stars.", xpReward = 120, target = 1,
    ) { sessions -> if (sessions.any { it.rating == 5 }) 1 else 0 },
    ChallengeDefinition(
        "protein-push", "Protein Push",
        "Cook a meal with at least 25g of protein.", xpReward = 100, target = 1,
    ) { sessions -> if (sessions.any { (it.proteinGrams ?: 0) >= 25 }) 1 else 0 },
    ChallengeDefinition(
        "xp-sprint", "XP Sprint",
        "Earn 400 XP from cooking this week.", xpReward = 200, target = 400,
    ) { it.sumOf(CookingSession::xpEarned) },
    ChallengeDefinition(
        "quick-fire", "Quick-Fire Round",
        "Cook a meal in 20 minutes or less.", xpReward = 90, target = 1,
    ) { sessions -> if (sessions.any { it.durationMinutes in 1..MAX_QUICK_FIRE_MINUTES }) 1 else 0 },
    ChallengeDefinition(
        "light-bite", "Light & Lean",
        "Log a meal under 400 kcal.", xpReward = 90, target = 1,
    ) { sessions -> if (sessions.any { (it.kcal ?: Int.MAX_VALUE) < MAX_LIGHT_BITE_KCAL }) 1 else 0 },
    ChallengeDefinition(
        "rate-three", "Critic's Corner",
        "Rate 3 different meals this week.", xpReward = 140, target = 3,
    ) { sessions -> sessions.count { it.rating != null } },
    ChallengeDefinition(
        "kitchen-journal", "Kitchen Journal",
        "Write an improvement note on a meal you cooked.", xpReward = 80, target = 1,
    ) { sessions -> if (sessions.any { !it.improvementNote.isNullOrBlank() }) 1 else 0 },
    ChallengeDefinition(
        "four-day-streak", "Cook Four Days",
        "Cook on 4 different days this week.", xpReward = 180, target = 4,
    ) { sessions -> sessions.map { it.cookedAt.toLocalDateTime(TimeZone.UTC).date }.distinct().size },
)

private const val MAX_QUICK_FIRE_MINUTES = 20
private const val MAX_LIGHT_BITE_KCAL = 400

/** SQLDelight-backed [WeeklyChallengeRepository]. Rotates through [CATALOG] one per calendar week,
 * bucketed by UTC epoch-day / 7 (deterministic, no ISO-week arithmetic needed), and derives
 * progress live from that week's cooking sessions. */
class WeeklyChallengeRepositoryImpl(
    private val database: LevelChefDatabase,
    private val cookingSessionRepository: CookingSessionRepository,
    private val clock: Clock = Clock.System,
) : WeeklyChallengeRepository {

    override fun observeCurrent(): Flow<WeeklyChallenge> {
        val weekKey = weekKeyFor(clock.now())
        val definition = CATALOG[weekKey.mod(CATALOG.size)]

        return combine(
            database.weeklyChallengeQueries.selectByWeek(weekKey.toString())
                .asFlow()
                .mapToOneOrNull(Dispatchers.Default),
            cookingSessionRepository.observeAll(),
        ) { row, sessions ->
            val thisWeekSessions = sessions.filter { weekKeyFor(it.cookedAt) == weekKey }
            WeeklyChallenge(
                id = definition.id,
                title = definition.title,
                description = definition.description,
                xpReward = definition.xpReward,
                progressCurrent = definition.progress(thisWeekSessions).coerceAtMost(definition.target),
                progressTarget = definition.target,
                completedAt = row?.completedAt?.let(Instant::parse),
            )
        }.onStart { database.weeklyChallengeQueries.insertIfAbsent(weekKey.toString(), definition.id) }
    }

    override suspend fun complete(id: String) {
        val weekKey = weekKeyFor(clock.now())
        val definition = CATALOG[weekKey.mod(CATALOG.size)]
        if (definition.id != id) return

        val thisWeekSessions = cookingSessionRepository.observeAll().first().filter { weekKeyFor(it.cookedAt) == weekKey }
        if (definition.progress(thisWeekSessions) < definition.target) return

        database.weeklyChallengeQueries.markCompleted(
            completedAt = clock.now().toString(),
            xpAwarded = definition.xpReward.toLong(),
            weekKey = weekKey.toString(),
        )
    }

    override suspend fun totalAwardedXp(): Int =
        database.weeklyChallengeQueries.totalAwardedXp().executeAsOne().toInt()

    private companion object {
        fun weekKeyFor(instant: Instant): Int = (instant.toLocalDateTime(TimeZone.UTC).date.toEpochDays() / 7)
    }
}
