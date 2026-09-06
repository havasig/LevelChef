@file:OptIn(ExperimentalTime::class)

package com.levelchef.data.repository

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import app.cash.turbine.test
import com.levelchef.core.database.db.LevelChefDatabase
import kotlinx.coroutines.test.runTest
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

/** Exercises the real SQLDelight schema through an in-memory JDBC database. */
class SavedRecipeRepositoryImplTest {

    private class StoppedClock(private val instant: Instant) : Clock {
        override fun now(): Instant = instant
    }

    private lateinit var driver: SqlDriver
    private lateinit var repository: SavedRecipeRepositoryImpl

    @BeforeTest
    fun setUp() {
        driver = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY)
        LevelChefDatabase.Schema.create(driver)
        repository = SavedRecipeRepositoryImpl(
            LevelChefDatabase(driver),
            StoppedClock(Instant.parse("2026-03-04T12:00:00Z")),
        )
    }

    @AfterTest
    fun tearDown() = driver.close()

    @Test
    fun a_recipe_starts_unsaved() = runTest {
        repository.observeIsSaved("lemon-chicken").test {
            assertFalse(awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun setting_saved_true_then_false_toggles_the_flag() = runTest {
        repository.observeIsSaved("lemon-chicken").test {
            assertFalse(awaitItem())

            repository.setSaved("lemon-chicken", true)
            assertTrue(awaitItem())

            repository.setSaved("lemon-chicken", false)
            assertFalse(awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun saving_the_same_recipe_twice_keeps_a_single_row() = runTest {
        repository.setSaved("lemon-chicken", true)
        repository.setSaved("lemon-chicken", true)

        val count = LevelChefDatabase(driver).savedRecipeQueries.selectAll().executeAsList().size
        assertEquals(1, count)
    }

    @Test
    fun saves_are_scoped_per_recipe_id() = runTest {
        repository.setSaved("lemon-chicken", true)

        repository.observeIsSaved("steak-bowl").test {
            assertFalse(awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }
}
