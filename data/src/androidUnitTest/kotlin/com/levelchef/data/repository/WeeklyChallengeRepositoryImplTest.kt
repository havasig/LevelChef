package com.levelchef.data.repository

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import app.cash.turbine.test
import com.levelchef.core.database.db.LevelChefDatabase
import com.levelchef.core.model.CookingSession
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.Instant
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

/** Exercises [WeeklyChallengeRepositoryImpl] against the real SQLDelight schema through an
 * in-memory JDBC database. Uses a fixed [Clock] so the picked challenge/week is deterministic. */
@OptIn(ExperimentalTime::class)
class WeeklyChallengeRepositoryImplTest {

    private val fixedNow = Instant.parse("2026-01-05T12:00:00Z") // a Monday
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
        repository = WeeklyChallengeRepositoryImpl(database, cookingSessionRepository, clock)
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

    @Test
    fun the_same_week_always_picks_the_same_challenge() = runTest {
        val first = repository.observeCurrent().test { awaitItem().also { cancelAndIgnoreRemainingEvents() } }
        val second = repository.observeCurrent().test { awaitItem().also { cancelAndIgnoreRemainingEvents() } }

        assertEquals(first.id, second.id)
        assertEquals(first.progressTarget, second.progressTarget)
    }

    @Test
    fun complete_is_a_no_op_until_progress_reaches_target() = runTest {
        val challenge = repository.observeCurrent().test { awaitItem().also { cancelAndIgnoreRemainingEvents() } }

        repository.complete(challenge.id)

        val stillIncomplete = repository.observeCurrent().test { awaitItem().also { cancelAndIgnoreRemainingEvents() } }
        assertTrue(!stillIncomplete.isCompleted)
    }

    @Test
    fun completing_the_active_challenge_awards_its_xp_once_its_target_is_reached() = runTest {
        // Deterministic for fixedNow (2026-01-05): resolves to catalog entry "rate-three"
        // ("Critic's Corner" - rate 3 different meals this week, target 3).
        val challenge = repository.observeCurrent().test { awaitItem().also { cancelAndIgnoreRemainingEvents() } }
        assertEquals("rate-three", challenge.id)

        repeat(3) { cookingSessionRepository.recordSession(session("s$it", fixedNow, rating = 5)) }
        repository.complete(challenge.id)

        val completed = repository.observeCurrent().test { awaitItem().also { cancelAndIgnoreRemainingEvents() } }
        assertTrue(completed.isCompleted)
        assertEquals(challenge.xpReward, repository.totalAwardedXp())
    }
}
