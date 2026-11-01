@file:OptIn(ExperimentalTime::class)

package com.levelchef.data.repository

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import com.levelchef.core.database.db.LevelChefDatabase
import com.levelchef.core.model.Allergen
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
import io.ktor.http.Headers
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.content.OutgoingContent
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
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.time.Clock
import kotlin.time.Duration.Companion.minutes
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

/** Exercises the real SQLDelight schema through an in-memory JDBC database, with the proxy itself
 * replaced by Ktor's [MockEngine] — this is what actually proves the caching/fallback logic, since
 * the tests have no network access to the real proxy. */
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

    private class MutableClock(var instant: Instant) : Clock {
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
    private lateinit var clock: MutableClock

    // Written from MockEngine's own threads, read from the test's.
    @Volatile private var requestCount = 0

    @Volatile private var lastRequest: HttpRequestData? = null

    private var idCounter = 0

    /** Read by the repositories built below through a `languageTag = { currentLanguageTag }`
     * lambda, so a test can switch it mid-test the same way [surveyRepository] is mutated. */
    private var currentLanguageTag: String? = null

    @BeforeTest
    fun setUp() {
        driver = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY)
        LevelChefDatabase.Schema.create(driver)
        database = LevelChefDatabase(driver)
        surveyRepository = FakeSurveyRepository(sampleSurvey)
        clock = MutableClock(Instant.parse("2026-03-04T12:00:00Z"))
        requestCount = 0
        idCounter = 0
        currentLanguageTag = null
    }

    @AfterTest
    fun tearDown() = driver.close()

    private fun repository(
        proxyUrl: String = PROXY_URL,
        recipesJson: String = SINGLE_RECIPE_JSON,
        status: HttpStatusCode = HttpStatusCode.OK,
        responseHeaders: Headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString()),
        envelope: (String) -> String = ::geminiEnvelope,
        responseGate: CompletableDeferred<Unit>? = null,
    ): RecipeRepositoryImpl {
        val mockEngine = MockEngine { request ->
            requestCount++
            lastRequest = request
            responseGate?.await()
            respond(content = envelope(recipesJson), status = status, headers = responseHeaders)
        }
        val httpClient = HttpClient(mockEngine) {
            install(ContentNegotiation) { json() }
        }
        return RecipeRepositoryImpl(
            database = database,
            surveyRepository = surveyRepository,
            httpClient = httpClient,
            proxyUrl = proxyUrl,
            clock = clock,
            languageTag = { currentLanguageTag },
            deviceId = { "device-123" },
            newId = { "gen-${++idCounter}" },
        )
    }

    private fun geminiEnvelope(recipesJson: String): String {
        val escapedText = JsonPrimitive(recipesJson).toString()
        return """{"candidates":[{"content":{"parts":[{"text":$escapedText}]}}]}"""
    }

    private fun lastRequestBody(): JsonObject {
        val body = lastRequest!!.body as OutgoingContent.ByteArrayContent
        return Json.parseToJsonElement(body.bytes().decodeToString()).jsonObject
    }

    @Test
    fun a_cache_miss_calls_the_proxy_and_persists_the_result_under_an_app_generated_id() = runTest {
        val recommendations = repository().getRecommendations()

        assertEquals(1, requestCount)
        assertEquals(listOf("gen-1"), recommendations.map { it.id })
        assertEquals("Lemon garlic chicken", recommendations.single().name)
    }

    @Test
    fun the_request_carries_only_survey_answers_and_the_language_never_a_prompt() = runTest {
        currentLanguageTag = "hu"
        surveyRepository.setResponse(sampleSurvey.copy(allergens = setOf(Allergen.SOY, Allergen.DAIRY)))

        repository().getRecommendations()

        val body = lastRequestBody()
        assertEquals(setOf("survey", "language"), body.keys)
        assertEquals("hu", body.getValue("language").jsonPrimitive.content)
        val survey = body.getValue("survey").jsonObject
        assertEquals("OMNIVORE", survey.getValue("dietaryPreference").jsonPrimitive.content)
        assertEquals(listOf("DAIRY", "SOY"), survey.getValue("allergens").jsonArray.map { it.jsonPrimitive.content })
        assertFalse("contents" in body)
    }

    @Test
    fun an_unsupported_language_is_sent_as_english() = runTest {
        currentLanguageTag = "de"

        repository().getRecommendations()

        assertEquals("en", lastRequestBody().getValue("language").jsonPrimitive.content)
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

        assertEquals(listOf("Lemon garlic chicken"), recommendations.map { it.name })
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
    fun switching_the_language_back_and_forth_reuses_each_languages_cached_batch() = runTest {
        val repository = repository()
        repository.getRecommendations()
        currentLanguageTag = "hu"
        repository.getRecommendations()
        assertEquals(2, requestCount)

        currentLanguageTag = null
        val english = repository.getRecommendations()
        currentLanguageTag = "hu"
        repository.getRecommendations()

        assertEquals(2, requestCount)
        assertEquals(listOf("gen-1"), english.map { it.id })
    }

    @Test
    fun a_model_id_repeated_in_a_later_batch_does_not_overwrite_an_earlier_recipe() = runTest {
        val repository = repository()
        repository.getRecommendations()
        surveyRepository.setResponse(sampleSurvey.copy(cookingGoal = CookingGoal.HIGH_PROTEIN))

        // Same model-chosen id ("lemon-garlic-chicken") again, but a different dish.
        repository(recipesJson = SINGLE_RECIPE_JSON.replace("Lemon garlic chicken", "Something else"))
            .getRecommendations()

        assertEquals("Lemon garlic chicken", repository.getById("gen-1")?.name)
        assertEquals("Something else", repository.getById("gen-2")?.name)
    }

    @Test
    fun generated_values_are_sanitized_before_they_reach_the_app() = runTest {
        val wild = """[{
            "id": "../../etc?x=1#frag", "name": "  Hacked stew  ", "emoji": "", "xpReward": 999999,
            "timeMinutes": -5, "difficulty": "HARD", "servings": 0, "caloriesKcal": -10,
            "tags": ["a", "b", "c", "d"],
            "ingredients": [{"name": "salt", "quantity": -1.0, "unit": "g"}, {"name": "  "}],
            "steps": [{"text": "Wait.", "timerMinutes": 100000}, {"text": "   "}],
            "videoUrl": "intent://evil#Intent;end"
        }]"""

        val recipe = repository(recipesJson = wild).getRecommendations().single()

        assertEquals("gen-1", recipe.id)
        assertEquals("Hacked stew", recipe.name)
        assertEquals(200, recipe.xpReward)
        assertEquals(1, recipe.timeMinutes)
        assertEquals(1, recipe.servings)
        assertNull(recipe.caloriesKcal)
        assertEquals(3, recipe.tags.size)
        assertEquals(1, recipe.ingredients.size)
        assertNull(recipe.ingredients.single().quantity)
        assertNull(recipe.ingredients.single().unit)
        assertEquals(1, recipe.steps.size)
        assertNull(recipe.steps.single().timerMinutes)
        assertEquals("https://www.youtube.com/results?search_query=Hacked+stew", recipe.videoUrl)
        assertTrue(recipe.emoji.isNotBlank())
    }

    @Test
    fun the_video_link_is_built_from_the_name_and_percent_encoded() = runTest {
        currentLanguageTag = "hu"
        val hungarian = SINGLE_RECIPE_JSON.replace("Lemon garlic chicken", "Csirke & rizs?")

        val recipe = repository(recipesJson = hungarian).getRecommendations().single()

        assertEquals("https://www.youtube.com/results?search_query=Csirke+%26+rizs%3F", recipe.videoUrl)
    }

    @Test
    fun a_batch_with_no_usable_recipes_falls_back_to_the_bundled_set() = runTest {
        val unusable = """[{"id": "x", "name": " ", "emoji": "x", "xpReward": 1, "timeMinutes": 1,
            "difficulty": "EASY", "servings": 1, "tags": [], "ingredients": [], "steps": []}]"""

        val recommendations = repository(recipesJson = unusable).getRecommendations()

        assertTrue(recommendations.any { it.id == "chicken-curry" })
    }

    @Test
    fun a_non_2xx_response_falls_back_to_the_bundled_sample_set() = runTest {
        val recommendations = repository(status = HttpStatusCode.InternalServerError).getRecommendations()

        assertTrue(recommendations.any { it.id == "chicken-curry" })
    }

    @Test
    fun the_fallback_respects_a_vegan_diet_and_allergens() = runTest {
        surveyRepository.setResponse(
            sampleSurvey.copy(dietaryPreference = DietaryPreference.VEGAN, allergens = setOf(Allergen.GLUTEN)),
        )

        val recommendations = repository(status = HttpStatusCode.InternalServerError).getRecommendations()

        assertEquals(setOf("chickpea-spinach-stew", "roasted-vegetable-tray-bake"), recommendations.map { it.id }.toSet())
    }

    @Test
    fun the_fallback_is_never_empty_whatever_the_diet_and_allergens() = runTest {
        val repository = repository(proxyUrl = "")
        for (diet in DietaryPreference.entries) {
            surveyRepository.setResponse(sampleSurvey.copy(dietaryPreference = diet, allergens = Allergen.entries.toSet()))
            assertTrue(repository.getRecommendations().isNotEmpty(), "empty for $diet")
        }
    }

    @Test
    fun a_shellfish_allergy_removes_the_curry_with_shrimp_paste_risk() = runTest {
        surveyRepository.setResponse(sampleSurvey.copy(allergens = setOf(Allergen.SHELLFISH)))

        val ids = repository(proxyUrl = "").getRecommendations().map { it.id }

        assertFalse("chicken-curry" in ids)
        assertTrue("steak-quinoa-bowl" in ids)
    }

    @Test
    fun after_a_failure_the_proxy_is_not_asked_again_until_the_backoff_passes() = runTest {
        val repository = repository(status = HttpStatusCode.BadGateway)
        repository.getRecommendations()
        repository.getRecommendations()
        assertEquals(1, requestCount)

        clock.instant += 6.minutes
        repository.getRecommendations()

        assertEquals(2, requestCount)
    }

    @Test
    fun a_quota_exceeded_response_backs_off_for_its_retry_after() = runTest {
        val headers = headersOf(
            HttpHeaders.ContentType to listOf(ContentType.Application.Json.toString()),
            HttpHeaders.RetryAfter to listOf("3600"),
        )
        val repository = repository(status = HttpStatusCode.TooManyRequests, responseHeaders = headers)

        val recommendations = repository.getRecommendations()
        clock.instant += 30.minutes
        repository.getRecommendations()
        assertEquals(1, requestCount)
        clock.instant += 31.minutes
        repository.getRecommendations()

        assertEquals(2, requestCount)
        assertTrue(recommendations.any { it.id == "chicken-curry" })
    }

    @Test
    fun a_network_exception_falls_back_and_backs_off() = runTest {
        val failing = RecipeRepositoryImpl(
            database = database,
            surveyRepository = surveyRepository,
            httpClient = HttpClient(MockEngine { requestCount++; error("connection reset") }) {
                install(ContentNegotiation) { json() }
            },
            proxyUrl = PROXY_URL,
            clock = clock,
        )

        assertTrue(failing.getRecommendations().any { it.id == "chicken-curry" })
        failing.getRecommendations()

        assertEquals(1, requestCount)
    }

    @Test
    fun delete_all_forgets_generated_recipes_and_the_backoff() = runTest {
        val repository = repository()
        repository.getRecommendations()

        repository.deleteAll()

        assertNull(repository.getById("gen-1"))
        repository.getRecommendations()
        assertEquals(2, requestCount)
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

        assertEquals("Lemon garlic chicken", repository.getById("gen-1")?.name)
    }

    @Test
    fun get_by_id_resolves_any_bundled_recipe_even_one_the_survey_now_filters_out() = runTest {
        surveyRepository.setResponse(sampleSurvey.copy(dietaryPreference = DietaryPreference.VEGAN))

        assertNotNull(repository().getById("steak-quinoa-bowl"))
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
    "steps": [{"text": "Sear the chicken with lemon and garlic."}]
}]"""
