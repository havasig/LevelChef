package com.levelchef.data.di

import com.levelchef.core.database.db.LevelChefDatabase
import com.levelchef.data.local.DatabaseDriverFactory
import com.levelchef.data.repository.BadgeRepositoryImpl
import com.levelchef.data.repository.CookingSessionRepositoryImpl
import com.levelchef.data.repository.IngredientRepositoryImpl
import com.levelchef.data.repository.RecipeRepositoryImpl
import com.levelchef.data.repository.SavedRecipeRepositoryImpl
import com.levelchef.data.repository.SurveyResponseRepositoryImpl
import com.levelchef.data.repository.WeeklyChallengeRepositoryImpl
import com.levelchef.domain.repository.BadgeRepository
import com.levelchef.domain.repository.CookingSessionRepository
import com.levelchef.domain.repository.IngredientRepository
import com.levelchef.domain.repository.RecipeRepository
import com.levelchef.domain.repository.SavedRecipeRepository
import com.levelchef.domain.repository.SurveyRepository
import com.levelchef.domain.repository.WeeklyChallengeRepository
import io.ktor.client.HttpClient
import io.ktor.client.engine.darwin.Darwin
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.Dispatchers
import org.koin.core.qualifier.named
import org.koin.dsl.module
import platform.Foundation.NSLocale

/**
 * iOS-only: wires the SQLDelight driver + database + DB-backed repositories. Mirrors androidMain's
 * `databaseModule` (see there for the shared shape and per-repository notes) — every repository runs
 * its blocking SQLite calls on [Dispatchers.IO] (Kotlin/Native's native-thread-pool IO dispatcher), off
 * the main thread.
 */
val databaseModule = module {
    single { DatabaseDriverFactory().createDriver() }
    single { LevelChefDatabase(get()) }
    single { HttpClient(Darwin) { install(ContentNegotiation) { json() } } }
    single<CookingSessionRepository> { CookingSessionRepositoryImpl(get(), dispatcher = Dispatchers.IO) }
    single<SurveyRepository> { SurveyResponseRepositoryImpl(get(), dispatcher = Dispatchers.IO) }
    single<IngredientRepository> { IngredientRepositoryImpl(get(), dispatcher = Dispatchers.IO) }
    single<BadgeRepository> { BadgeRepositoryImpl(get(), get(), get(), dispatcher = Dispatchers.IO) }
    single<WeeklyChallengeRepository> { WeeklyChallengeRepositoryImpl(get(), get(), dispatcher = Dispatchers.IO) }
    single<SavedRecipeRepository> { SavedRecipeRepositoryImpl(get(), dispatcher = Dispatchers.IO) }
    // named("geminiApiKey") is registered by iosApp's shared Koin entry point, the same way
    // androidApp's LevelChefApplication registers it from BuildConfig.GEMINI_API_KEY — `data` never
    // references a platform config mechanism directly to stay platform-agnostic.
    single<RecipeRepository> {
        RecipeRepositoryImpl(
            get(),
            get(),
            get(),
            get(named("geminiApiKey")),
            languageTag = { currentAppLanguageTag() },
            dispatcher = Dispatchers.IO,
        )
    }
}

/**
 * The app's current language (see androidMain's `currentAppLanguageTag` for the Android counterpart —
 * `data` can't depend on `feature:settings`, so this reads the system locale directly). No in-app
 * language override exists on iOS yet (see AGENTS.md's iOS "Not yet done" item — that's `feature:settings`'
 * job in a later stage); until then this always reflects the device's own language setting. `null` means
 * "no locale info", which [RecipeRepositoryImpl] treats as English.
 */
private fun currentAppLanguageTag(): String? =
    (NSLocale.preferredLanguages.firstOrNull() as? String)
        ?.substringBefore('-')
        ?.ifBlank { null }
