package com.levelchef.data.local

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import com.levelchef.core.database.db.LevelChefDatabase
import com.levelchef.core.model.CookingSession
import com.levelchef.data.repository.CookingSessionRepositoryImpl
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.Instant
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Migrates hand-built v4 databases to the current schema. Two v4 shapes exist in the wild (the
 * trophy-room tables and `durationMinutes` column shipped once without a migration), and both must
 * end up readable through the generated queries.
 */
class DatabaseMigrationsTest {

    private lateinit var driver: SqlDriver

    @BeforeTest
    fun setUp() {
        driver = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY)
    }

    @AfterTest
    fun tearDown() = driver.close()

    @Test
    fun schema_version_counts_the_v4_to_v5_migration() {
        assertEquals(5L, LevelChefDatabase.Schema.version)
    }

    @Test
    fun migrates_a_v4_database_missing_the_trophy_tables_and_duration_column() = runTest {
        createV4Tables(withDurationColumn = false)
        driver.execute(
            null,
            "INSERT INTO cookingSession(id, recipeId, recipeName, cookedAt, xpEarned, rating) " +
                "VALUES ('old', 'pasta', 'Pasta', '2026-01-01T10:00:00Z', 80, 4)",
            0,
        )

        migrateFromV4()

        val repository = CookingSessionRepositoryImpl(LevelChefDatabase(driver))
        val migrated = repository.mostRecent()
        assertEquals("old", migrated?.id)
        assertEquals(0, migrated?.durationMinutes)
        assertEquals(4, migrated?.rating)
        assertTrophyTablesUsable()

        repository.recordSession(session(id = "new", durationMinutes = 25))
        assertEquals(25, repository.totalDurationMinutes())
    }

    @Test
    fun migrates_a_v4_database_that_already_has_the_trophy_tables_and_duration_column() = runTest {
        createV4Tables(withDurationColumn = true)
        driver.execute(null, "CREATE TABLE badgeEarned (badgeId TEXT NOT NULL PRIMARY KEY, earnedAt TEXT NOT NULL)", 0)
        driver.execute(
            null,
            "INSERT INTO cookingSession(id, recipeId, recipeName, cookedAt, xpEarned, durationMinutes) " +
                "VALUES ('kept', 'pasta', 'Pasta', '2026-01-01T10:00:00Z', 80, 30)",
            0,
        )

        migrateFromV4()

        val repository = CookingSessionRepositoryImpl(LevelChefDatabase(driver))
        assertEquals(30, repository.mostRecent()?.durationMinutes)
        assertTrophyTablesUsable()
    }

    private fun migrateFromV4() {
        LevelChefDatabase.Schema.migrate(driver, 4, LevelChefDatabase.Schema.version, *databaseMigrationCallbacks)
    }

    private fun assertTrophyTablesUsable() {
        val database = LevelChefDatabase(driver)
        database.badgeQueries.markEarned("first-bite", "2026-01-02T10:00:00Z")
        assertEquals(1, database.badgeQueries.selectAll().executeAsList().size)
        database.weeklyChallengeQueries.insertIfAbsent("2900", "three-meals")
        assertEquals(0L, database.weeklyChallengeQueries.totalAwardedXp().executeAsOne())
    }

    /** The v4 schema as shipped: the original tables plus migrations 1–3. */
    private fun createV4Tables(withDurationColumn: Boolean) {
        val duration = if (withDurationColumn) "durationMinutes INTEGER NOT NULL DEFAULT 0," else ""
        listOf(
            """
            CREATE TABLE cookingSession (
                id TEXT NOT NULL PRIMARY KEY, recipeId TEXT NOT NULL, recipeName TEXT NOT NULL,
                cookedAt TEXT NOT NULL, xpEarned INTEGER NOT NULL, $duration rating INTEGER,
                improvementNote TEXT, kcal INTEGER, proteinGrams INTEGER, carbsGrams INTEGER, fatGrams INTEGER
            )
            """,
            """
            CREATE TABLE surveyResponse (
                id INTEGER NOT NULL PRIMARY KEY, completedAt TEXT NOT NULL, cookingExperience TEXT NOT NULL,
                dietaryPreference TEXT NOT NULL, allergens TEXT NOT NULL, cuisines TEXT NOT NULL,
                spiceTolerance TEXT NOT NULL, cookingGoal TEXT NOT NULL, weeknightTime TEXT NOT NULL,
                householdSize TEXT NOT NULL
            )
            """,
            """
            CREATE TABLE ingredient (
                id TEXT NOT NULL PRIMARY KEY, name TEXT NOT NULL, category TEXT NOT NULL, emoji TEXT NOT NULL,
                defaultUnit TEXT, calories INTEGER, proteinGrams REAL, carbsGrams REAL, fatGrams REAL, imageUrl TEXT
            )
            """,
            "CREATE TABLE savedRecipe (recipeId TEXT NOT NULL PRIMARY KEY, savedAt TEXT NOT NULL)",
        ).forEach { driver.execute(null, it.trimIndent(), 0) }
    }

    private fun session(id: String, durationMinutes: Int) = CookingSession(
        id = id,
        recipeId = "pasta",
        recipeName = "Pasta",
        cookedAt = Instant.parse("2026-02-01T10:00:00Z"),
        xpEarned = 80,
        durationMinutes = durationMinutes,
    )
}
