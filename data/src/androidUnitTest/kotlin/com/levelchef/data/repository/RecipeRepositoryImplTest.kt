@file:OptIn(ExperimentalTime::class)

package com.levelchef.data.repository

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import com.levelchef.core.database.db.LevelChefDatabase
import com.levelchef.core.model.CookingExperience
import com.levelchef.core.model.CookingGoal
import com.levelchef.core.model.Cuisine
import com.levelchef.core.model.DietaryPreference
import com.levelchef.core.model.HouseholdSize
import com.levelchef.core.model.SpiceTolerance
import com.levelchef.core.model.SurveyResponse
import com.levelchef.core.model.WeeknightTime
import com.levelchef.domain.repository.SurveyRepository
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.HttpRequestData
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.JsonPrimitive
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

/** Exercises the real SQLDelight schema through an in-memory JDBC database, with Gemini itself
 * replaced by Ktor's [MockEngine] — this is what actually proves the caching/fallback logic, since
 * this sandbox has no real Gemini API key or network access to it. */
class RecipeRepositoryImplTest {

    private class FakeSurveyRepository(initial: SurveyResponse?) : SurveyRepository {
        private val state = MutableStateFlow(initial)
        override fun observeResponse(): Flow<SurveyResponse?> = state
        override suspend fun save(response: SurveyResponse) {
            state.value = response
        }
        override suspend fun clear() {
            state.value = null
        }
        fun setResponse(response: SurveyResponse?) {
            state.value = response
        }
    }

    private class StoppedClock(private val instant: Instant) : Clock {
        override fun now(): Instant = instant
    }

    private val sampleSurvey = SurveyResponse(
        completedAt = Instant.parse("2026-01-01T00:00:00Z"),
        cookingExperience = CookingExperience.BEGINNER,
        dietaryPreference = DietaryPreference.OMNIVORE,
        allergens = emptySet(),
        cuisines = setOf(Cuisine.ITALIAN),
        spiceTolerance = SpiceTolerance.MILD,
        cookingGoal = CookingGoal.QUICK_MEALS,
        weeknightTime = WeeknightTime.UNDER_15,
        householdSize = HouseholdSize.SOLO,
    )

    private lateinit var driver: SqlDriver
    private lateinit var database: LevelChefDatabase
    private lateinit var surveyRepository: FakeSurveyRepository
    // Written from MockEngine's own threads, read from the test's.
    @Volatile private var requestCount = 0

    @Volatile private var lastRequest: HttpRequestData? = null

    /** Read by the repositories built below through a `languageTag = { currentLanguageTag }`
     * lambda, so a test can switch it mid-test the same way [surveyRepository] is mutated. */
    private var currentLanguageTag: String? = null

    @BeforeTest
    fun setUp() {
        driver = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY)
        LevelChefDatabase.Schema.create(driver)
        database = LevelChefDatabase(driver)
        surveyRepository = FakeSurveyRepository(sampleSurvey)
        requestCount = 0
        currentLanguageTag = null
    }

    @AfterTest
    fun tearDown() = driver.close()

    private fun repository(
        proxyUrl: String = PROXY_URL,
        recipesJson: String = SINGLE_RECIPE_JSON,
        status: HttpStatusCode = HttpStatusCode.OK,
        envelope: (String) -> String = ::geminiEnvelope,
        responseGate: CompletableDeferred<Unit>? = null,
    ): RecipeRepositoryImpl {
        val mockEngine = MockEngine { request ->
            requestCount++
            lastRequest = request
            responseGate?.await()
            respond(
                content = envelope(recipesJson),
                status = status,
                headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString()),
            )
        }
        val httpClient = HttpClient(mockEngine) {
            install(ContentNegotiation) { json() }
        }
        return RecipeRepositoryImpl(
            database = database,
            surveyRepository = surveyRepository,
            httpClient = httpClient,
            proxyUrl = proxyUrl,
            clock = StoppedClock(Instant.parse("2026-03-04T12:00:00Z")),
            languageTag = { currentLanguageTag },
            deviceId = { "device-123" },
        )
    }

    private fun geminiEnvelope(recipesJson: String): String {
        val escapedText = JsonPrimitive(recipesJson).toString()
        return """{"candidates":[{"content":{"parts":[{"text":$escapedText}]}}]}"""
    }

    @Test
    fun a_cache_miss_calls_gemini_and_persists_the_result() = runTest {
        val recommendations = repository().getRecommendations()

        assertEquals(1, requestCount)
        assertEquals(listOf("lemon-garlic-chicken"), recommendations.map { it.id })
    }

    @Test
    fun unknown_fields_in_the_gemini_envelope_are_ignored() = runTest {
        // Gemini 3 adds a `thoughtSignature` next to each part's text (and other fields over time).
        val gemini3Envelope = { recipesJson: String ->
            val escapedText = JsonPrimitive(recipesJson).toString()
            """{"candidates":[{"content":{"parts":[{"text":$escapedText,"thoughtSignature":"abc"}]}}],""" +
                """"modelVersion":"gemini-3.8-flash"}"""
        }

        val recommendations = repository(envelope = gemini3Envelope).getRecommendations()

        assertEquals(listOf("lemon-garlic-chicken"), recommendations.map { it.id })
    }

    @Test
    fun the_survey_fingerprint_is_built_from_answer_names_so_it_survives_a_restart() {
        // A literal, not a computed value: the fingerprint is persisted, so it must not depend on
        // anything per-process like an enum's identity hashCode().
        assertEquals(
            "BEGINNER|OMNIVORE||ITALIAN|MILD|QUICK_MEALS|UNDER_15|SOLO|en",
            fingerprint(sampleSurvey, languageTag = null),
        )
    }

    @Test
    fun the_survey_fingerprint_ignores_completion_time_and_answer_order() {
        val retaken = sampleSurvey.copy(
            completedAt = Instant.parse("2026-06-01T00:00:00Z"),
            cuisines = setOf(Cuisine.MEXICAN, Cuisine.ITALIAN),
        )
        val reordered = retaken.copy(cuisines = setOf(Cuisine.ITALIAN, Cuisine.MEXICAN))

        assertEquals(fingerprint(retaken, "hu"), fingerprint(reordered, "hu"))
        assertEquals(
            fingerprint(sampleSurvey, "hu"),
            fingerprint(sampleSurvey.copy(completedAt = retaken.completedAt), "hu"),
        )
    }

    @Test
    fun concurrent_calls_share_a_single_generation() = runTest {
        val gate = CompletableDeferred<Unit>()
        val repository = repository(responseGate = gate)

        val first = async(Dispatchers.Default) { repository.getRecommendations() }
        val second = async(Dispatchers.Default) { repository.getRecommendations() }
        // Real time, not runTest's virtual clock: MockEngine answers on its own threads. Hold the first
        // response back long enough that an unserialized second call would also reach the network.
        withContext(Dispatchers.Default) {
            while (requestCount == 0) delay(10)
            delay(300)
        }
        gate.complete(Unit)

        assertEquals(first.await(), second.await())
        assertEquals(1, requestCount)
    }

    @Test
    fun a_repeat_call_with_an_unchanged_survey_is_served_from_the_cache() = runTest {
        val repository = repository()
        repository.getRecommendations()
        assertEquals(1, requestCount)

        repository.getRecommendations()

        assertEquals(1, requestCount)
    }

    @Test
    fun a_changed_survey_triggers_a_new_generation() = runTest {
        val repository = repository()
        repository.getRecommendations()
        assertEquals(1, requestCount)

        surveyRepository.setResponse(sampleSurvey.copy(dietaryPreference = DietaryPreference.VEGAN))
        repository.getRecommendations()

        assertEquals(2, requestCount)
    }

    @Test
    fun a_changed_language_alone_triggers_a_new_generation() = runTest {
        val repository = repository()
        repository.getRecommendations()
        assertEquals(1, requestCount)

        currentLanguageTag = "hu"
        repository.getRecommendations()

        assertEquals(2, requestCount)
    }

    @Test
    fun a_non_2xx_response_falls_back_to_the_bundled_sample_set() = runTest {
        val recommendations = repository(status = HttpStatusCode.InternalServerError).getRecommendations()

        assertTrue(recommendations.any { it.id == "chicken-curry" })
    }

    @Test
    fun a_quota_exceeded_response_falls_back_to_the_bundled_sample_set() = runTest {
        val recommendations = repository(status = HttpStatusCode.TooManyRequests).getRecommendations()

        assertEquals(1, requestCount)
        assertTrue(recommendations.any { it.id == "chicken-curry" })
    }

    @Test
    fun the_request_goes_to_the_proxy_with_the_device_id_and_without_any_api_key() = runTest {
        repository().getRecommendations()

        val request = lastRequest!!
        assertEquals(PROXY_URL, request.url.toString())
        assertEquals("device-123", request.headers["X-Device-Id"])
        assertNull(request.url.parameters["key"])
        assertNull(request.headers["x-goog-api-key"])
    }

    @Test
    fun malformed_generated_json_falls_back_to_the_bundled_sample_set() = runTest {
        val recommendations = repository(recipesJson = "not valid recipe json").getRecommendations()

        assertTrue(recommendations.any { it.id == "chicken-curry" })
    }

    @Test
    fun a_blank_proxy_url_skips_the_network_call_and_serves_the_bundled_set() = runTest {
        val recommendations = repository(proxyUrl = "").getRecommendations()

        assertEquals(0, requestCount)
        assertTrue(recommendations.any { it.id == "chicken-curry" })
    }

    @Test
    fun a_blank_proxy_url_with_hungarian_serves_the_hungarian_bundled_set() = runTest {
        currentLanguageTag = "hu"

        val recommendations = repository(proxyUrl = "").getRecommendations()

        assertEquals(0, requestCount)
        assertTrue(recommendations.any { it.id == "chicken-curry" && it.name == "Csirke curry kókusztejjel" })
    }

    @Test
    fun no_survey_yet_serves_the_bundled_set_without_a_network_call() = runTest {
        surveyRepository.setResponse(null)

        val recommendations = repository().getRecommendations()

        assertEquals(0, requestCount)
        assertTrue(recommendations.any { it.id == "chicken-curry" })
    }

    @Test
    fun get_by_id_resolves_a_cached_generated_recipe() = runTest {
        val repository = repository()
        repository.getRecommendations()

        assertEquals("Lemon garlic chicken", repository.getById("lemon-garlic-chicken")?.name)
    }

    @Test
    fun get_by_id_falls_back_to_the_bundled_set_for_an_id_never_generated() = runTest {
        assertEquals("Steak quinoa bowl", repository().getById("steak-quinoa-bowl")?.name)
    }

    @Test
    fun get_by_id_returns_null_when_nothing_matches() = runTest {
        assertNull(repository().getById("does-not-exist"))
    }
}

private const val PROXY_URL = "https://recipes.example.workers.dev/"

private const val SINGLE_RECIPE_JSON = """[{
    "id": "lemon-garlic-chicken",
    "name": "Lemon garlic chicken",
    "emoji": "🍋",
    "xpReward": 40,
    "timeMinutes": 20,
    "difficulty": "EASY",
    "servings": 1,
    "caloriesKcal": 420,
    "proteinGrams": 35,
    "carbsGrams": 10,
    "fatGrams": 22,
    "tags": ["Quick"],
    "ingredients": [{"name": "chicken breast", "quantity": 300.0, "unit": "g"}],
    "steps": [{"text": "Sear the chicken with lemon and garlic."}],
    "videoUrl": "https://www.youtube.com/results?search_query=lemon+garlic+chicken"
}]"""
