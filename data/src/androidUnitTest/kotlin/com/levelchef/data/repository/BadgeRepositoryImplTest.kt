package com.levelchef.data.repository

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import app.cash.turbine.test
import com.levelchef.core.database.db.LevelChefDatabase
import com.levelchef.core.model.CookingSession
import com.levelchef.core.model.Ingredient
import com.levelchef.core.model.IngredientCategory
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.Instant
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

/** Exercises [BadgeRepositoryImpl] against the real SQLDelight schema through an in-memory JDBC database. */
class BadgeRepositoryImplTest {

    private lateinit var driver: SqlDriver
    private lateinit var database: LevelChefDatabase
    private lateinit var cookingSessionRepository: CookingSessionRepositoryImpl
    private lateinit var ingredientRepository: IngredientRepositoryImpl
    private lateinit var repository: BadgeRepositoryImpl

    @BeforeTest
    fun setUp() {
        driver = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY)
        LevelChefDatabase.Schema.create(driver)
        database = LevelChefDatabase(driver)
        cookingSessionRepository = CookingSessionRepositoryImpl(database)
        ingredientRepository = IngredientRepositoryImpl(database)
        repository = BadgeRepositoryImpl(cookingSessionRepository, ingredientRepository, database)
    }

    @AfterTest
    fun tearDown() = driver.close()

    private fun session(id: String) = CookingSession(
        id = id,
        recipeId = "recipe-$id",
        recipeName = "Recipe $id",
        cookedAt = Instant.parse("2026-01-01T10:00:00Z"),
        xpEarned = 10,
    )

    @Test
    fun first_bite_is_locked_with_no_sessions_and_earned_after_one() = runTest {
        repository.observeAll().test {
            val locked = awaitItem().single { it.id == "first-bite" }
            assertEquals(0, locked.progressCurrent)
            assertNull(locked.earnedAt)

            cookingSessionRepository.recordSession(session("a"))

            val earned = awaitItem().single { it.id == "first-bite" }
            assertEquals(1, earned.progressCurrent)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun full_shelf_progress_counts_distinct_categories_excluding_other() = runTest {
        ingredientRepository.save(Ingredient("a", "Chicken", IngredientCategory.MEAT, "🍗"))
        ingredientRepository.save(Ingredient("b", "Milk", IngredientCategory.DAIRY, "🥛"))
        ingredientRepository.save(Ingredient("c", "Junk", IngredientCategory.OTHER, "❓"))

        val fullShelf = repository.observeAll().test {
            val badges = awaitItem()
            cancelAndIgnoreRemainingEvents()
            badges.single { it.id == "full-shelf" }
        }

        assertEquals(2, fullShelf.progressCurrent)
        assertEquals(6, fullShelf.progressTarget)
    }

    @Test
    fun refresh_earned_persists_a_stable_earned_date_once_a_badge_reaches_target() = runTest {
        repeat(10) { cookingSessionRepository.recordSession(session("s$it")) }

        repository.refreshEarned()
        val firstEarnedAt = repository.observeAll().test {
            val at = awaitItem().single { it.id == "ten-meals-deep" }.earnedAt
            cancelAndIgnoreRemainingEvents()
            at
        }
        assertNotNull(firstEarnedAt)

        repository.refreshEarned() // idempotent: earned date doesn't move on a second refresh
        val secondEarnedAt = repository.observeAll().test {
            val at = awaitItem().single { it.id == "ten-meals-deep" }.earnedAt
            cancelAndIgnoreRemainingEvents()
            at
        }
        assertEquals(firstEarnedAt, secondEarnedAt)
    }
}
