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
import platform.Foundation.preferredLanguages

// kotlinx.coroutines' Dispatchers.IO is unusable from application code on Kotlin/Native: a member
// declaration of the same name shadows the public extension property (still true as of 1.9.0 — see
// https://github.com/Kotlin/kotlinx.coroutines/issues/3205), so `Dispatchers.IO` fails to compile here
// with "it is internal in 'kotlinx.coroutines.Dispatchers'". Dispatchers.Default (CPU-bound, but public
// on every target) is the safe stand-in for iOS until that's resolved upstream.
private val ioDispatcher = Dispatchers.Default

/**
 * iOS-only: wires the SQLDelight driver + database + DB-backed repositories. Mirrors androidMain's
 * `databaseModule` (see there for the shared shape and per-repository notes) — every repository runs
 * its blocking SQLite calls on [ioDispatcher], off the main thread.
 */
val databaseModule = module {
    single { DatabaseDriverFactory().createDriver() }
    single { LevelChefDatabase(get()) }
    single { HttpClient(Darwin) { install(ContentNegotiation) { json() } } }
    single<CookingSessionRepository> { CookingSessionRepositoryImpl(get(), dispatcher = ioDispatcher) }
    single<SurveyRepository> { SurveyResponseRepositoryImpl(get(), dispatcher = ioDispatcher) }
    single<IngredientRepository> { IngredientRepositoryImpl(get(), dispatcher = ioDispatcher) }
    single<BadgeRepository> { BadgeRepositoryImpl(get(), get(), get(), dispatcher = ioDispatcher) }
    single<WeeklyChallengeRepository> { WeeklyChallengeRepositoryImpl(get(), get(), dispatcher = ioDispatcher) }
    single<SavedRecipeRepository> { SavedRecipeRepositoryImpl(get(), dispatcher = ioDispatcher) }
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
            dispatcher = ioDispatcher,
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
