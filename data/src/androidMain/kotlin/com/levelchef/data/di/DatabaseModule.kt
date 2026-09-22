package com.levelchef.data.di

import com.levelchef.core.database.db.LevelChefDatabase
import com.levelchef.data.local.DatabaseDriverFactory
import com.levelchef.data.repository.BadgeRepositoryImpl
import com.levelchef.data.repository.CookingSessionRepositoryImpl
import com.levelchef.data.repository.IngredientRepositoryImpl
import com.levelchef.data.repository.SavedRecipeRepositoryImpl
import com.levelchef.data.repository.SurveyResponseRepositoryImpl
import com.levelchef.data.repository.WeeklyChallengeRepositoryImpl
import com.levelchef.domain.repository.BadgeRepository
import com.levelchef.domain.repository.CookingSessionRepository
import com.levelchef.domain.repository.IngredientRepository
import com.levelchef.domain.repository.SavedRecipeRepository
import com.levelchef.domain.repository.SurveyRepository
import com.levelchef.domain.repository.WeeklyChallengeRepository
import kotlinx.coroutines.Dispatchers
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

/** Android-only: wires the SQLDelight driver + database + DB-backed repositories. Every repository
 * runs its blocking SQLite calls on [Dispatchers.IO], off the main thread. */
val databaseModule = module {
    single { DatabaseDriverFactory(androidContext()).createDriver() }
    single { LevelChefDatabase(get()) }
    single<CookingSessionRepository> { CookingSessionRepositoryImpl(get(), dispatcher = Dispatchers.IO) }
    single<SurveyRepository> { SurveyResponseRepositoryImpl(get(), dispatcher = Dispatchers.IO) }
    single<IngredientRepository> { IngredientRepositoryImpl(get(), dispatcher = Dispatchers.IO) }
    single<BadgeRepository> { BadgeRepositoryImpl(get(), get(), get(), dispatcher = Dispatchers.IO) }
    single<WeeklyChallengeRepository> { WeeklyChallengeRepositoryImpl(get(), get(), dispatcher = Dispatchers.IO) }
    single<SavedRecipeRepository> { SavedRecipeRepositoryImpl(get(), dispatcher = Dispatchers.IO) }
}
