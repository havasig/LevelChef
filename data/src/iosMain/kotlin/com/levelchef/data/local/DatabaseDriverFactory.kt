package com.levelchef.data.local

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.native.NativeSqliteDriver
import com.levelchef.core.database.db.LevelChefDatabase

/**
 * Creates the platform [SqlDriver] backing [LevelChefDatabase]. iOS counterpart of androidMain's
 * `DatabaseDriverFactory` — same name and shape, but not an `expect`/`actual` pair: each platform's own
 * `databaseModule` only ever references the version in its own source set, so there's no shared
 * declaration requiring one. [NativeSqliteDriver] resolves an app-sandboxed default location for the
 * database file on its own.
 */
class DatabaseDriverFactory {
    fun createDriver(): SqlDriver = NativeSqliteDriver(LevelChefDatabase.Schema, "levelchef.db")
}
