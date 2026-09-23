package com.levelchef.data.repository

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import app.cash.turbine.test
import com.levelchef.core.database.db.LevelChefDatabase
import com.levelchef.core.model.CookingSession
import com.levelchef.core.model.WeeklyChallenge
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.time.Clock
import kotlin.time.Duration.Companion.days
import kotlin.time.ExperimentalTime

/** Exercises [WeeklyChallengeRepositoryImpl] against the real SQLDelight schema through an
 * in-memory JDBC database. Uses a fixed [Clock] so the picked challenge/week is deterministic. */
@OptIn(ExperimentalTime::class)
class WeeklyChallengeRepositoryImplTest {

    private val fixedNow = Instant.parse("2025-12-29T12:00:00Z") // a Monday
    private val clock = object : Clock {
        override fun now(): Instant = fixedNow
    }

    private lateinit var driver: SqlDriver
    private lateinit var database: LevelChefDatabase
    private lateinit var cookingSessionRepository: CookingSessionRepositoryImpl
    private lateinit var repository: WeeklyChallengeRepositoryImpl

    @BeforeTest
    fun setUp() {
        driver = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY)
        LevelChefDatabase.Schema.create(driver)
        database = LevelChefDatabase(driver)
        cookingSessionRepository = CookingSessionRepositoryImpl(database)
        repository = WeeklyChallengeRepositoryImpl(
            database,
            cookingSessionRepository,
            clock,
            timeZone = { TimeZone.UTC },
        )
    }

    @AfterTest
    fun tearDown() = driver.close()

    private fun session(id: String, cookedAt: Instant, rating: Int? = null) = CookingSession(
        id = id,
        recipeId = "recipe-$id",
        recipeName = "Recipe $id",
        cookedAt = cookedAt,
        xpEarned = 10,
        rating = rating,
    )

    /** Turbine's `test { ... }` always returns Unit, so the current challenge is captured into a
     * local var from inside the block rather than returned out of it. */
    private suspend fun currentChallenge(): WeeklyChallenge {
        lateinit var result: WeeklyChallenge
        repository.observeCurrent().test {
            result = awaitItem()
            cancelAndIgnoreRemainingEvents()
        }
        return result
    }

    @Test
    fun the_same_week_always_picks_the_same_challenge() = runTest {
        val first = currentChallenge()
        val second = currentChallenge()

        assertEquals(first.id, second.id)
        assertEquals(first.progressTarget, second.progressTarget)
    }

    @Test
    fun complete_is_a_no_op_until_progress_reaches_target() = runTest {
        val challenge = currentChallenge()

        repository.complete(challenge.id)

        assertTrue(!currentChallenge().isCompleted)
    }

    @Test
    fun completing_the_active_challenge_awards_its_xp_once_its_target_is_reached() = runTest {
        // Deterministic for fixedNow (2025-12-29, UTC): resolves to catalog entry "rate-three"
        // ("Critic's Corner" - rate 3 different meals this week, target 3).
        val challenge = currentChallenge()
        assertEquals("rate-three", challenge.id)

        repeat(3) { cookingSessionRepository.recordSession(session("s$it", fixedNow, rating = 5)) }
        repository.complete(challenge.id)

        assertTrue(currentChallenge().isCompleted)
        assertEquals(challenge.xpReward, repository.totalAwardedXp())
    }

    @Test
    fun delete_all_resets_completion_and_awarded_xp() = runTest {
        val challenge = currentChallenge()
        repeat(3) { cookingSessionRepository.recordSession(session("s$it", fixedNow, rating = 5)) }
        repository.complete(challenge.id)

        repository.deleteAll()

        assertEquals(0, repository.totalAwardedXp())
        assertTrue(!currentChallenge().isCompleted)
    }

    @Test
    fun weeks_run_monday_to_sunday_so_last_sunday_does_not_count() = runTest {
        // fixedNow's week is Mon 2025-12-29 .. Sun 2026-01-04; "rate-three" needs 3 rated meals.
        val challenge = currentChallenge()
        cookingSessionRepository.recordSession(session("sun", Instant.parse("2025-12-28T20:00:00Z"), rating = 5))
        cookingSessionRepository.recordSession(session("mon", Instant.parse("2025-12-29T08:00:00Z"), rating = 5))
        cookingSessionRepository.recordSession(session("next-sun", Instant.parse("2026-01-04T20:00:00Z"), rating = 5))

        assertEquals(2, currentChallenge().progressCurrent)
        assertEquals("rate-three", challenge.id)
    }

    @Test
    fun every_catalog_challenge_rotates_in_and_counts_a_matching_session() = runTest {
        // One session that satisfies every catalog rule at least partially: rated 5, a note, 15 min,
        // 300 kcal, 30 g protein, 10 XP.
        val allRounder = session("template", fixedNow, rating = 5).copy(
            durationMinutes = 15,
            kcal = 300,
            proteinGrams = 30,
            improvementNote = "More lemon",
        )
        val seenIds = mutableSetOf<String>()

        repeat(CATALOG_SIZE) { week ->
            val monday = fixedNow + (week * DAYS_PER_WEEK).days
            val weekRepository = WeeklyChallengeRepositoryImpl(
                database,
                cookingSessionRepository,
                clock = object : Clock {
                    override fun now(): Instant = monday
                },
                timeZone = { TimeZone.UTC },
            )
            cookingSessionRepository.recordSession(allRounder.copy(id = "s$week", cookedAt = monday))

            lateinit var challenge: WeeklyChallenge
            weekRepository.observeCurrent().test {
                challenge = awaitItem()
                cancelAndIgnoreRemainingEvents()
            }
            seenIds += challenge.id
            assertTrue(challenge.progressCurrent >= 1, "${challenge.id} should count this week's session")
        }

        assertEquals(CATALOG_SIZE, seenIds.size)
    }

    private companion object {
        const val CATALOG_SIZE = 9
        const val DAYS_PER_WEEK = 7
    }
}
