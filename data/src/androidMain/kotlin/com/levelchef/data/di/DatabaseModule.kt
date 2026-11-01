package com.levelchef.data.di

import android.content.Context
import androidx.appcompat.app.AppCompatDelegate
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
import io.ktor.client.engine.android.Android
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.Dispatchers
import org.koin.android.ext.koin.androidContext
import org.koin.core.qualifier.named
import org.koin.dsl.module
import java.util.Locale
import java.util.UUID

/** Android-only: wires the SQLDelight driver + database + DB-backed repositories. Every repository
 * runs its blocking SQLite calls on [Dispatchers.IO], off the main thread. */
val databaseModule = module {
    single { DatabaseDriverFactory(androidContext()).createDriver() }
    single { LevelChefDatabase(get()) }
    single {
        HttpClient(Android) {
            install(ContentNegotiation) { json() }
            install(HttpTimeout) {
                connectTimeoutMillis = CONNECT_TIMEOUT_MS
                requestTimeoutMillis = REQUEST_TIMEOUT_MS
            }
        }
    }
    single<CookingSessionRepository> { CookingSessionRepositoryImpl(get(), dispatcher = Dispatchers.IO) }
    single<SurveyRepository> { SurveyResponseRepositoryImpl(get(), dispatcher = Dispatchers.IO) }
    single<IngredientRepository> {
        IngredientRepositoryImpl(get(), dispatcher = Dispatchers.IO, languageTag = { currentAppLanguageTag() })
    }
    single<BadgeRepository> { BadgeRepositoryImpl(get(), get(), get(), dispatcher = Dispatchers.IO) }
    single<WeeklyChallengeRepository> { WeeklyChallengeRepositoryImpl(get(), get(), dispatcher = Dispatchers.IO) }
    single<SavedRecipeRepository> { SavedRecipeRepositoryImpl(get(), dispatcher = Dispatchers.IO) }
    // named("recipeProxyUrl") is registered by androidApp's LevelChefApplication (BuildConfig.RECIPE_PROXY_URL) —
    // `data` never references BuildConfig directly to stay platform-agnostic. The Gemini key itself lives
    // only in the proxy Worker (see /proxy), never in the app.
    single<RecipeRepository> {
        RecipeRepositoryImpl(
            get(),
            get(),
            get(),
            get(named("recipeProxyUrl")),
            languageTag = { currentAppLanguageTag() },
            deviceId = { installId(androidContext()) },
            dispatcher = Dispatchers.IO,
        )
    }
}

/** The language the UI is actually shown in: the app's per-app language override (see
 * `feature:settings`' `AndroidAppSettingsController.language()`, which resolves the same
 * [AppCompatDelegate.getApplicationLocales] into its `AppLanguage` enum — `data` can't depend on
 * `feature:settings`, so this inlines the same tag parsing), or, with no override ("System"), the
 * device language — so a Hungarian phone on "System" gets Hungarian recipes and starter pantry,
 * matching the Hungarian UI. [RecipeRepositoryImpl] and [IngredientRepositoryImpl] treat anything
 * but "hu" as English. */
private fun currentAppLanguageTag(): String? =
    AppCompatDelegate.getApplicationLocales()
        .toLanguageTags()
        .substringBefore(',')
        .substringBefore('-')
        .ifBlank { Locale.getDefault().language }
        .ifBlank { null }

// The proxy waits on Gemini, which can take tens of seconds; Ktor's own default is no request timeout.
private const val CONNECT_TIMEOUT_MS = 10_000L
private const val REQUEST_TIMEOUT_MS = 60_000L

private const val INSTALL_PREFS = "levelchef_install"
private const val INSTALL_ID_KEY = "installId"

/** A random per-install id the recipe proxy meters its daily quota by. Generated on first use and kept
 * in SharedPreferences; it carries no personal data and changes if the app is reinstalled. */
private fun installId(context: Context): String {
    val prefs = context.getSharedPreferences(INSTALL_PREFS, Context.MODE_PRIVATE)
    return prefs.getString(INSTALL_ID_KEY, null) ?: UUID.randomUUID().toString().also {
        prefs.edit().putString(INSTALL_ID_KEY, it).apply()
    }
}
