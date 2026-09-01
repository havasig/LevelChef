package com.levelchef.data.di

import com.levelchef.core.database.db.LevelChefDatabase
import com.levelchef.data.local.DatabaseDriverFactory
import com.levelchef.data.repository.CookingSessionRepositoryImpl
import com.levelchef.domain.repository.CookingSessionRepository
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

/** Android-only: wires the SQLDelight driver + database + [CookingSessionRepository]. */
val databaseModule = module {
    single { DatabaseDriverFactory(androidContext()).createDriver() }
    single { LevelChefDatabase(get()) }
    single<CookingSessionRepository> { CookingSessionRepositoryImpl(get()) }
}
