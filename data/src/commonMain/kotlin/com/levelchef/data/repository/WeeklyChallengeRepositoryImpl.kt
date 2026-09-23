@file:OptIn(ExperimentalTime::class)

package com.levelchef.data.repository

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToOneOrNull
import com.levelchef.core.database.db.LevelChefDatabase
import com.levelchef.core.model.CookingSession
import com.levelchef.core.model.WeeklyChallenge
import com.levelchef.domain.repository.CookingSessionRepository
import com.levelchef.domain.repository.WeeklyChallengeRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.withContext
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

/** One catalog entry: how a challenge's progress is read off that week's sessions (dates read in the
 * given device time zone), capped at [target]. */
private class ChallengeDefinition(
    val id: String,
    val title: String,
    val description: String,
    val xpReward: Int,
    val target: Int,
    val progress: (List<CookingSession>, TimeZone) -> Int,
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
    ) { sessions, _ -> sessions.size },
    ChallengeDefinition(
        "five-star-plate", "Plate of the Week",
        "Rate one meal a full 5 stars.", xpReward = 120, target = 1,
    ) { sessions, _ -> if (sessions.any { it.rating == 5 }) 1 else 0 },
    ChallengeDefinition(
        "protein-push", "Protein Push",
        "Cook a meal with at least 25g of protein.", xpReward = 100, target = 1,
    ) { sessions, _ -> if (sessions.any { (it.proteinGrams ?: 0) >= 25 }) 1 else 0 },
    ChallengeDefinition(
        "xp-sprint", "XP Sprint",
        "Earn 400 XP from cooking this week.", xpReward = 200, target = 400,
    ) { sessions, _ -> sessions.sumOf(CookingSession::xpEarned) },
    ChallengeDefinition(
        "quick-fire", "Quick-Fire Round",
        "Cook a meal in 20 minutes or less.", xpReward = 90, target = 1,
    ) { sessions, _ -> if (sessions.any { it.durationMinutes in 1..MAX_QUICK_FIRE_MINUTES }) 1 else 0 },
    ChallengeDefinition(
        "light-bite", "Light & Lean",
        "Log a meal under 400 kcal.", xpReward = 90, target = 1,
    ) { sessions, _ -> if (sessions.any { (it.kcal ?: Int.MAX_VALUE) < MAX_LIGHT_BITE_KCAL }) 1 else 0 },
    ChallengeDefinition(
        "rate-three", "Critic's Corner",
        "Rate 3 different meals this week.", xpReward = 140, target = 3,
    ) { sessions, _ -> sessions.count { it.rating != null } },
    ChallengeDefinition(
        "kitchen-journal", "Kitchen Journal",
        "Write an improvement note on a meal you cooked.", xpReward = 80, target = 1,
    ) { sessions, _ -> if (sessions.any { !it.improvementNote.isNullOrBlank() }) 1 else 0 },
    ChallengeDefinition(
        "four-day-streak", "Cook Four Days",
        "Cook on 4 different days this week.", xpReward = 180, target = 4,
    ) { sessions, zone -> sessions.map { it.cookedAt.toLocalDateTime(zone).date }.distinct().size },
)

private const val MAX_QUICK_FIRE_MINUTES = 20
private const val MAX_LIGHT_BITE_KCAL = 400

/** SQLDelight-backed [WeeklyChallengeRepository]. Rotates through [CATALOG] one per calendar week
 * (Monday to Sunday in the device's [timeZone]; see [weekKeyFor]), and derives progress live from
 * that week's cooking sessions. Blocking SQLite calls run on [dispatcher] —
 * `Dispatchers.IO` on Android (see `databaseModule`). */
class WeeklyChallengeRepositoryImpl(
    private val database: LevelChefDatabase,
    private val cookingSessionRepository: CookingSessionRepository,
    private val clock: Clock = Clock.System,
    private val dispatcher: CoroutineDispatcher = Dispatchers.Default,
    private val timeZone: () -> TimeZone = { TimeZone.currentSystemDefault() },
) : WeeklyChallengeRepository {

    override fun observeCurrent(): Flow<WeeklyChallenge> {
        val zone = timeZone()
        val weekKey = weekKeyFor(clock.now(), zone)
        val definition = CATALOG[weekKey.mod(CATALOG.size)]

        return combine(
            database.weeklyChallengeQueries.selectByWeek(weekKey.toString())
                .asFlow()
                .mapToOneOrNull(Dispatchers.Default),
            cookingSessionRepository.observeAll(),
        ) { row, sessions ->
            val thisWeekSessions = sessions.filter { weekKeyFor(it.cookedAt, zone) == weekKey }
            WeeklyChallenge(
                id = definition.id,
                title = definition.title,
                description = definition.description,
                xpReward = definition.xpReward,
                progressCurrent = definition.progress(thisWeekSessions, zone).coerceAtMost(definition.target),
                progressTarget = definition.target,
                completedAt = row?.completedAt?.let(Instant::parse),
            )
        }.onStart {
            withContext(dispatcher) {
                database.weeklyChallengeQueries.insertIfAbsent(weekKey.toString(), definition.id)
            }
        }
    }

    override suspend fun complete(id: String) {
        val zone = timeZone()
        val weekKey = weekKeyFor(clock.now(), zone)
        val definition = CATALOG[weekKey.mod(CATALOG.size)]
        if (definition.id != id) return

        val thisWeekSessions = cookingSessionRepository.observeAll().first()
            .filter { weekKeyFor(it.cookedAt, zone) == weekKey }
        if (definition.progress(thisWeekSessions, zone) < definition.target) return

        withContext(dispatcher) {
            database.weeklyChallengeQueries.markCompleted(
                completedAt = clock.now().toString(),
                xpAwarded = definition.xpReward.toLong(),
                weekKey = weekKey.toString(),
            )
        }
    }

    override suspend fun totalAwardedXp(): Int = withContext(dispatcher) {
        database.weeklyChallengeQueries.totalAwardedXp().executeAsOne().toInt()
    }

    override suspend fun deleteAll() {
        withContext(dispatcher) { database.weeklyChallengeQueries.deleteAll() }
    }

    private companion object {
        /** 1970-01-01 (epoch day 0) was a Thursday; shifting by 3 days makes each bucket start on Monday. */
        const val EPOCH_DAY_TO_MONDAY_SHIFT = 3
        const val DAYS_PER_WEEK = 7

        /** Index of the Monday-to-Sunday week containing [instant] in [zone]. */
        fun weekKeyFor(instant: Instant, zone: TimeZone): Int =
            ((instant.toLocalDateTime(zone).date.toEpochDays() + EPOCH_DAY_TO_MONDAY_SHIFT) / DAYS_PER_WEEK).toInt()
    }
}
