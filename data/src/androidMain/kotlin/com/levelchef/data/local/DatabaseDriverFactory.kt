package com.levelchef.data.local

import android.content.Context
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import com.levelchef.core.database.db.LevelChefDatabase

/** Creates the platform [SqlDriver] backing [LevelChefDatabase]. Android-only for now (KMP targets androidTarget only). */
class DatabaseDriverFactory(private val context: Context) {
    fun createDriver(): SqlDriver = AndroidSqliteDriver(
        schema = LevelChefDatabase.Schema,
        context = context,
        name = "levelchef.db",
        callback = AndroidSqliteDriver.Callback(LevelChefDatabase.Schema, *databaseMigrationCallbacks),
    )
}
