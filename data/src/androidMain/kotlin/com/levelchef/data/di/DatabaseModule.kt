package com.levelchef.data.di

import com.levelchef.core.database.db.LevelChefDatabase
import com.levelchef.data.local.DatabaseDriverFactory
import com.levelchef.data.repository.BadgeRepositoryImpl
import com.levelchef.data.repository.CookingSessionRepositoryImpl
import com.levelchef.data.repository.IngredientRepositoryImpl
import com.levelchef.data.repository.SurveyResponseRepositoryImpl
import com.levelchef.data.repository.WeeklyChallengeRepositoryImpl
import com.levelchef.domain.repository.BadgeRepository
import com.levelchef.domain.repository.CookingSessionRepository
import com.levelchef.domain.repository.IngredientRepository
import com.levelchef.domain.repository.SurveyRepository
import com.levelchef.domain.repository.WeeklyChallengeRepository
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

/** Android-only: wires the SQLDelight driver + database + DB-backed repositories. */
val databaseModule = module {
    single { DatabaseDriverFactory(androidContext()).createDriver() }
    single { LevelChefDatabase(get()) }
    single<CookingSessionRepository> { CookingSessionRepositoryImpl(get()) }
    single<SurveyRepository> { SurveyResponseRepositoryImpl(get()) }
    single<IngredientRepository> { IngredientRepositoryImpl(get()) }
    single<BadgeRepository> { BadgeRepositoryImpl(get(), get(), get()) }
    single<WeeklyChallengeRepository> { WeeklyChallengeRepositoryImpl(get(), get()) }
}
