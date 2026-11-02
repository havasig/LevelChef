@file:OptIn(ExperimentalTime::class, ExperimentalUuidApi::class)

package com.levelchef.data.repository

import co.touchlab.kermit.Logger
import com.levelchef.core.database.db.GeneratedRecipe
import com.levelchef.core.database.db.GeneratedRecipeQueries
import com.levelchef.core.database.db.LevelChefDatabase
import com.levelchef.core.model.Recipe
import com.levelchef.core.model.SurveyResponse
import com.levelchef.data.recipe.GeminiResponse
import com.levelchef.data.recipe.bundledRecipeById
import com.levelchef.data.recipe.bundledRecipes
import com.levelchef.data.recipe.contentLanguage
import com.levelchef.data.recipe.generatedRecipesJson
import com.levelchef.data.recipe.recipeProxyRequest
import com.levelchef.data.recipe.sanitized
import com.levelchef.domain.repository.RecipeRepository
import com.levelchef.domain.repository.SurveyRepository
import io.ktor.client.HttpClient
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import kotlin.time.Clock
import kotlin.time.Duration
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds
import kotlin.time.ExperimentalTime
import kotlin.time.Instant
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

private const val DEVICE_ID_HEADER = "X-Device-Id"
private const val RECOMMENDATION_COUNT = 5L

/** After a failed generation, don't ask the proxy again for this long (a 429's Retry-After wins). */
private val FAILURE_BACKOFF = 5.minutes
private val MAX_RETRY_AFTER = 1.hours

private val json = Json { ignoreUnknownKeys = true }

/**
 * Gemini-backed [RecipeRepository], reached through the LevelChef recipe proxy ([proxyUrl], see the
 * repo's `proxy/` Cloudflare Worker) — the app never holds a Gemini API key, and never sends a
 * prompt: only the survey answers and the content language (see `RecipeProxyRequest`). The proxy
 * enforces the per-device/IP/global daily quota. [deviceId] is a stable per-install id the proxy
 * meters the quota by.
 *
 * Recommendations are cached in [LevelChefDatabase]'s `generatedRecipe` table, keyed by a
 * fingerprint of the survey answers and the language — any earlier batch for the same fingerprint
 * is reused (switching the language back and forth doesn't spend new generations). Every generated
 * recipe gets an app-generated id ([newId]) and is sanitized before it's stored, so [getById]
 * resolves against the whole cache history: a saved/cooked recipe stays resolvable after newer
 * batches arrive. A blank [proxyUrl], no survey, or any proxy/Gemini failure falls back to the
 * bundled recipes that fit the survey's diet and allergens — "Recommended for you" is never empty.
 * After a failure the proxy isn't asked again for [FAILURE_BACKOFF] (or a 429's Retry-After), so
 * Home's refresh-on-resume doesn't hammer a proxy that's down or out of quota.
 * Blocking SQLite calls run on [dispatcher] — `Dispatchers.IO` on Android (see `databaseModule`).
 */
class RecipeRepositoryImpl(
    private val database: LevelChefDatabase,
    private val surveyRepository: SurveyRepository,
    private val httpClient: HttpClient,
    private val proxyUrl: String,
    private val clock: Clock = Clock.System,
    private val languageTag: () -> String? = { null },
    private val deviceId: () -> String = { "" },
    private val dispatcher: CoroutineDispatcher = Dispatchers.Default,
    private val newId: () -> String = { Uuid.random().toString() },
) : RecipeRepository {

    private val queries: GeneratedRecipeQueries get() = database.generatedRecipeQueries

    /** Serializes [getRecommendations]: Home asks again on resume while a slow first generation is
     * still in flight, and without this both calls would miss the cache and each spend a generation of
     * the proxy's daily quota. The second caller now waits and is served the first one's cached batch. */
    private val generationLock = Mutex()

    /** Guarded by [generationLock]. */
    private var retryNotBefore: Instant? = null

    override suspend fun getRecommendations(): List<Recipe> = generationLock.withLock { cachedOrGenerated() }

    private suspend fun cachedOrGenerated(): List<Recipe> {
        val language = languageTag()
        val survey = surveyRepository.observeResponse().first() ?: return bundledRecipes(language)
        val fingerprint = fingerprint(survey, language)

        val cached = cachedRecipes(fingerprint)
        if (cached.isNotEmpty()) return cached

        return generateRecommendations(survey, language, fingerprint) ?: bundledRecipes(language, survey)
    }

    override suspend fun getById(id: String): Recipe? =
        withContext(dispatcher) { queries.selectById(id).executeAsOneOrNull() }?.decodeRecipeOrNull()
            ?: bundledRecipeById(id, languageTag())

    override suspend fun deleteAll() {
        generationLock.withLock {
            withContext(dispatcher) { queries.deleteAll() }
            retryNotBefore = null
        }
    }

    private suspend fun cachedRecipes(fingerprint: String): List<Recipe> =
        withContext(dispatcher) {
            queries.selectRecentByFingerprint(fingerprint, RECOMMENDATION_COUNT).executeAsList()
        }.mapNotNull { it.decodeRecipeOrNull() }

    @Suppress("TooGenericExceptionCaught")
    private suspend fun generateRecommendations(
        survey: SurveyResponse,
        language: String?,
        fingerprint: String,
    ): List<Recipe>? {
        if (proxyUrl.isBlank()) return null
        retryNotBefore?.let { if (clock.now() < it) return null }
        return try {
            fetchGeneratedRecipes(survey, language)?.also { recipes -> cacheRecipes(recipes, fingerprint) }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Logger.e(e) { "Failed to generate recipe recommendations" }
            backOff(FAILURE_BACKOFF)
            null
        }
    }

    private suspend fun fetchGeneratedRecipes(survey: SurveyResponse, language: String?): List<Recipe>? {
        val response = httpClient.post(proxyUrl) {
            header(DEVICE_ID_HEADER, deviceId())
            contentType(ContentType.Application.Json)
            setBody(recipeProxyRequest(survey, language))
        }
        if (!response.status.isSuccess()) {
            Logger.w { "Recipe proxy request failed: ${response.status}" }
            backOff(response.retryAfter() ?: FAILURE_BACKOFF)
            return null
        }
        // Decoded with the lenient [json], not the HttpClient's own — Gemini adds fields (e.g. Gemini 3's
        // `thoughtSignature`) that a strict decoder rejects.
        val recipesJson = json.decodeFromString<GeminiResponse>(response.bodyAsText()).generatedRecipesJson()
        val recipes = recipesJson?.let { text ->
            json.decodeFromString<List<Recipe>>(text)
                .mapNotNull { it.sanitized(newId()) }
                .take(RECOMMENDATION_COUNT.toInt())
        }
        if (recipes.isNullOrEmpty()) {
            Logger.w { "Gemini response had no usable recipes" }
            backOff(FAILURE_BACKOFF)
            return null
        }
        return recipes
    }

    private fun backOff(duration: Duration) {
        retryNotBefore = clock.now() + duration
    }

    private suspend fun cacheRecipes(recipes: List<Recipe>, fingerprint: String) {
        val generatedAt = clock.now().toString()
        withContext(dispatcher) {
            queries.transaction {
                recipes.forEach { recipe ->
                    queries.insert(
                        id = recipe.id,
                        recipeJson = json.encodeToString(recipe),
                        generatedAt = generatedAt,
                        surveyFingerprint = fingerprint,
                    )
                }
            }
        }
    }
}

/** A 429's `Retry-After` (seconds), clamped to [FAILURE_BACKOFF]..[MAX_RETRY_AFTER]; null otherwise. */
private fun HttpResponse.retryAfter(): Duration? {
    if (status != HttpStatusCode.TooManyRequests) return null
    val seconds = headers[HttpHeaders.RetryAfter]?.toLongOrNull() ?: return FAILURE_BACKOFF
    return seconds.seconds.coerceIn(FAILURE_BACKOFF, MAX_RETRY_AFTER)
}

private fun GeneratedRecipe.decodeRecipeOrNull(): Recipe? = try {
    json.decodeFromString(recipeJson)
} catch (e: SerializationException) {
    Logger.w(e) { "Failed to decode cached recipe $id" }
    null
}

/** The answers that matter for recommendations, plus the content language (so a language switch
 * also selects a different cache batch) — used to find "a batch generated for exactly these answers
 * and this language." Built from enum *names*: it is persisted, so it must be identical across
 * processes, which `hashCode()` is not (an enum's hash is its identity hash on the JVM — every cold
 * start missed the cache and spent a proxy generation). `completedAt` is left out on purpose:
 * retaking the survey with the same answers keeps the cached batch. */
internal fun fingerprint(survey: SurveyResponse, languageTag: String?): String = listOf(
    survey.cookingExperience.name,
    survey.dietaryPreference.name,
    survey.allergens.map { it.name }.sorted().joinToString("+"),
    survey.cuisines.map { it.name }.sorted().joinToString("+"),
    survey.spiceTolerance.name,
    survey.cookingGoal.name,
    survey.weeknightTime.name,
    survey.householdSize.name,
    contentLanguage(languageTag),
).joinToString("|")
