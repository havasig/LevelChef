package com.levelchef.data.local

import app.cash.sqldelight.db.AfterVersion
import app.cash.sqldelight.db.QueryResult
import app.cash.sqldelight.db.SqlDriver

private const val BEFORE_DURATION_COLUMN_VERSION = 4L

/**
 * Code-side migration steps that `.sqm` files can't express. Pass them to every
 * `LevelChefDatabase.Schema` migrate/driver callback.
 *
 * After v4 → v5 (`4.sqm`): `cookingSession.durationMinutes` was added to the schema without a
 * migration, so some v4 databases have it and some don't. SQLite has no `ADD COLUMN IF NOT EXISTS`,
 * and SQLDelight reads `SELECT *` rows by column position — so a plain `ALTER TABLE … ADD COLUMN`
 * (which appends at the end) would misalign every read. Instead the table is rebuilt in the
 * declared column order, only when the column is missing.
 */
val databaseMigrationCallbacks: Array<AfterVersion> = arrayOf(
    AfterVersion(BEFORE_DURATION_COLUMN_VERSION) { driver ->
        if ("durationMinutes" !in driver.columnNames("cookingSession")) driver.rebuildCookingSessionWithDuration()
    },
)

private fun SqlDriver.columnNames(table: String): Set<String> =
    executeQuery(
        identifier = null,
        sql = "PRAGMA table_info($table)",
        mapper = { cursor ->
            val names = mutableSetOf<String>()
            // PRAGMA table_info columns: cid, name, type, notnull, dflt_value, pk.
            while (cursor.next().value) cursor.getString(1)?.let(names::add)
            QueryResult.Value(names)
        },
        parameters = 0,
    ).value

private fun SqlDriver.rebuildCookingSessionWithDuration() {
    listOf(
        """
        CREATE TABLE cookingSession_new (
            id TEXT NOT NULL PRIMARY KEY,
            recipeId TEXT NOT NULL,
            recipeName TEXT NOT NULL,
            cookedAt TEXT NOT NULL,
            xpEarned INTEGER NOT NULL,
            durationMinutes INTEGER NOT NULL DEFAULT 0,
            rating INTEGER,
            improvementNote TEXT,
            kcal INTEGER,
            proteinGrams INTEGER,
            carbsGrams INTEGER,
            fatGrams INTEGER
        )
        """,
        """
        INSERT INTO cookingSession_new(id, recipeId, recipeName, cookedAt, xpEarned, rating, improvementNote, kcal, proteinGrams, carbsGrams, fatGrams)
        SELECT id, recipeId, recipeName, cookedAt, xpEarned, rating, improvementNote, kcal, proteinGrams, carbsGrams, fatGrams
        FROM cookingSession
        """,
        "DROP TABLE cookingSession",
        "ALTER TABLE cookingSession_new RENAME TO cookingSession",
    ).forEach { sql -> execute(identifier = null, sql = sql.trimIndent(), parameters = 0) }
}
