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
import kotlin.test.assertNull

/** Exercises the real SQLDelight schema through an in-memory JDBC database. */
class CookingSessionRepositoryImplTest {

    private lateinit var driver: SqlDriver
    private lateinit var repository: CookingSessionRepositoryImpl

    @BeforeTest
    fun setUp() {
        driver = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY)
        LevelChefDatabase.Schema.create(driver)
        repository = CookingSessionRepositoryImpl(LevelChefDatabase(driver))
    }

    @AfterTest
    fun tearDown() = driver.close()

    @Test
    fun records_a_session_and_reads_it_back_as_a_domain_model() = runTest {
        val cookedAt = Instant.parse("2026-01-02T10:00:00Z")
        repository.recordSession(session(id = "a", xp = 40, cookedAt = cookedAt, rating = 4))

        val recent = repository.mostRecent()

        assertEquals("a", recent?.id)
        assertEquals("Recipe a", recent?.recipeName)
        assertEquals(cookedAt, recent?.cookedAt)
        assertEquals(40, recent?.xpEarned)
        assertEquals(4, recent?.rating)
    }

    @Test
    fun most_recent_returns_null_when_there_are_no_sessions() = runTest {
        assertNull(repository.mostRecent())
    }

    @Test
    fun total_xp_and_session_count_aggregate_every_row() = runTest {
        repository.recordSession(session("a", xp = 30, cookedAt = Instant.parse("2026-01-01T00:00:00Z")))
        repository.recordSession(session("b", xp = 70, cookedAt = Instant.parse("2026-01-02T00:00:00Z")))

        assertEquals(100, repository.totalXp())
        assertEquals(2, repository.sessionCount())
    }

    @Test
    fun observe_all_emits_current_sessions_and_re_emits_on_insert() = runTest {
        repository.observeAll().test {
            assertEquals(emptyList(), awaitItem())

            repository.recordSession(session("a", xp = 10, cookedAt = Instant.parse("2026-01-01T00:00:00Z")))

            assertEquals(listOf("a"), awaitItem().map { it.id })
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun delete_all_clears_every_session() = runTest {
        repository.recordSession(session("a", xp = 10, cookedAt = Instant.parse("2026-01-01T00:00:00Z")))
        repository.recordSession(session("b", xp = 20, cookedAt = Instant.parse("2026-01-02T00:00:00Z")))

        repository.deleteAll()

        assertEquals(0, repository.sessionCount())
        assertNull(repository.mostRecent())
    }

    private fun session(
        id: String,
        xp: Int,
        cookedAt: Instant,
        rating: Int? = null,
    ) = CookingSession(
        id = id,
        recipeId = "recipe-$id",
        recipeName = "Recipe $id",
        cookedAt = cookedAt,
        xpEarned = xp,
        rating = rating,
    )
}
