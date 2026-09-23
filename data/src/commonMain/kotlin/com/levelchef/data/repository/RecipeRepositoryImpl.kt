@file:OptIn(ExperimentalTime::class)

package com.levelchef.data.repository

import co.touchlab.kermit.Logger
import com.levelchef.core.database.db.GeneratedRecipe
import com.levelchef.core.database.db.GeneratedRecipeQueries
import com.levelchef.core.database.db.LevelChefDatabase
import com.levelchef.core.model.Difficulty
import com.levelchef.core.model.Recipe
import com.levelchef.core.model.RecipeIngredient
import com.levelchef.core.model.RecipeStep
import com.levelchef.core.model.SurveyResponse
import com.levelchef.data.recipe.GeminiResponse
import com.levelchef.data.recipe.buildRecipePrompt
import com.levelchef.data.recipe.geminiRecipeRequest
import com.levelchef.data.recipe.generatedRecipesJson
import com.levelchef.domain.repository.RecipeRepository
import com.levelchef.domain.repository.SurveyRepository
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

private const val GEMINI_ENDPOINT =
    "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent"
private const val RECOMMENDATION_COUNT = 5L

private val json = Json { ignoreUnknownKeys = true }

/**
 * Gemini-backed [RecipeRepository]. Recommendations are generated from the stored [SurveyResponse]
 * (and the current app [languageTag]) and cached in [LevelChefDatabase]'s `generatedRecipe` table,
 * keyed by a fingerprint of both — a repeat call with an unchanged survey and language is served
 * from the cache, and a changed survey, a changed language, or an empty cache triggers a new Gemini
 * call. [getById] resolves against the whole cache history, not just the latest batch, so a
 * saved/cooked recipe stays resolvable even after a newer batch replaces it in
 * [getRecommendations]. A blank [apiKey], or any failure talking to Gemini, falls back to existing
 * cache rows and finally to [fallbackRecipes] — the app never shows an empty "Recommended for you."
 * Blocking SQLite calls run on [dispatcher] — `Dispatchers.IO` on Android (see `databaseModule`).
 */
class RecipeRepositoryImpl(
    private val database: LevelChefDatabase,
    private val surveyRepository: SurveyRepository,
    private val httpClient: HttpClient,
    private val apiKey: String,
    private val clock: Clock = Clock.System,
    private val languageTag: () -> String? = { null },
    private val dispatcher: CoroutineDispatcher = Dispatchers.Default,
) : RecipeRepository {

    private val queries: GeneratedRecipeQueries get() = database.generatedRecipeQueries

    override suspend fun getRecommendations(): List<Recipe> {
        val survey = surveyRepository.observeResponse().first() ?: return fallbackRecipes(languageTag())
        val fingerprint = fingerprint(survey, languageTag())

        if (latestCachedFingerprint() == fingerprint) {
            val cached = cachedRecipes()
            if (cached.isNotEmpty()) return cached
        }

        return generateRecommendations(survey, fingerprint)
            ?: cachedRecipes().ifEmpty { fallbackRecipes(languageTag()) }
    }

    override suspend fun getById(id: String): Recipe? =
        withContext(dispatcher) { queries.selectById(id).executeAsOneOrNull() }?.decodeRecipeOrNull()
            ?: fallbackRecipes(languageTag()).find { it.id == id }

    private suspend fun latestCachedFingerprint(): String? =
        withContext(dispatcher) { queries.latestFingerprint().executeAsOneOrNull() }

    private suspend fun cachedRecipes(): List<Recipe> =
        withContext(dispatcher) { queries.selectRecent(RECOMMENDATION_COUNT).executeAsList() }
            .mapNotNull { it.decodeRecipeOrNull() }

    @Suppress("TooGenericExceptionCaught")
    private suspend fun generateRecommendations(survey: SurveyResponse, fingerprint: String): List<Recipe>? {
        if (apiKey.isBlank()) return null
        return try {
            fetchGeneratedRecipes(survey)?.also { recipes -> cacheRecipes(recipes, fingerprint) }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Logger.e(e) { "Failed to generate recipe recommendations from Gemini" }
            null
        }
    }

    private suspend fun fetchGeneratedRecipes(survey: SurveyResponse): List<Recipe>? {
        val response = httpClient.post(GEMINI_ENDPOINT) {
            parameter("key", apiKey)
            contentType(ContentType.Application.Json)
            setBody(geminiRecipeRequest(buildRecipePrompt(survey, languageTag())))
        }
        if (!response.status.isSuccess()) {
            Logger.w { "Gemini recommendation request failed: ${response.status}" }
            return null
        }
        val recipesJson = response.body<GeminiResponse>().generatedRecipesJson()
        if (recipesJson == null) {
            Logger.w { "Gemini response had no generated content" }
            return null
        }
        return json.decodeFromString<List<Recipe>>(recipesJson).ifEmpty { null }
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

private fun GeneratedRecipe.decodeRecipeOrNull(): Recipe? = try {
    json.decodeFromString(recipeJson)
} catch (e: SerializationException) {
    Logger.w(e) { "Failed to decode cached recipe $id" }
    null
}

/** A stable-enough hash of the answers (plus the current app language, so a language switch also
 * invalidates the cache) that matter for recommendations — not security-sensitive, only used to
 * detect "the user retook the survey, or changed the app language, since the last generation." */
private fun fingerprint(survey: SurveyResponse, languageTag: String?): String =
    "${survey.hashCode()}-${languageTag ?: "en"}"

/** Small bundled set served when there's no survey yet, no API key configured, or Gemini/the cache
 * are both unavailable — the last-resort source so Home never shows an empty recommendation list.
 * [languageTag] picks [fallbackRecipesHu] for Hungarian, else the English [fallbackRecipesEn]. */
private fun fallbackRecipes(languageTag: String?): List<Recipe> =
    if (languageTag?.lowercase() == "hu") fallbackRecipesHu else fallbackRecipesEn

private val fallbackRecipesEn = listOf(
    Recipe(
        id = "chicken-curry",
        name = "Chicken curry with coconut milk",
        emoji = "🍲",
        xpReward = 45,
        timeMinutes = 25,
        difficulty = Difficulty.EASY,
        servings = 2,
        caloriesKcal = 520,
        proteinGrams = 38,
        carbsGrams = 18,
        fatGrams = 32,
        tags = listOf("Comfort", "One pot"),
        ingredients = listOf(
            RecipeIngredient("chicken thigh", quantity = 300.0, unit = "g"),
            RecipeIngredient("coconut milk", quantity = 400.0, unit = "ml"),
            RecipeIngredient("yellow curry paste", quantity = 2.0, unit = "tbsp"),
            RecipeIngredient("onion", quantity = 1.0),
            RecipeIngredient("Cilantro, lime, salt"),
        ),
        steps = listOf(
            RecipeStep("Dice the chicken and slice the onion."),
            RecipeStep("Fry the curry paste with the onion until fragrant."),
            RecipeStep("Add the chicken and sear on all sides."),
            RecipeStep("Pour in the coconut milk and simmer.", timerMinutes = 15),
            RecipeStep("Finish with lime juice and fresh cilantro."),
        ),
        videoUrl = "https://www.youtube.com/results?search_query=chicken+curry+coconut+milk",
    ),
    Recipe(
        id = "steak-quinoa-bowl",
        name = "Steak quinoa bowl",
        emoji = "🥩",
        xpReward = 120,
        timeMinutes = 35,
        difficulty = Difficulty.MEDIUM,
        servings = 2,
        caloriesKcal = 610,
        proteinGrams = 46,
        carbsGrams = 52,
        fatGrams = 24,
        tags = listOf("High protein", "Meal prep"),
        ingredients = listOf(
            RecipeIngredient("flank steak", quantity = 250.0, unit = "g"),
            RecipeIngredient("quinoa", quantity = 150.0, unit = "g"),
            RecipeIngredient("cherry tomatoes", quantity = 100.0, unit = "g"),
            RecipeIngredient("avocado", quantity = 1.0),
            RecipeIngredient("Olive oil, lemon, salt, pepper"),
        ),
        steps = listOf(
            RecipeStep("Rinse the quinoa, then simmer in salted water.", timerMinutes = 15),
            RecipeStep("Season the steak generously with salt and pepper."),
            RecipeStep("Sear the steak 3-4 minutes per side, then rest.", timerMinutes = 5),
            RecipeStep("Halve the tomatoes and slice the avocado."),
            RecipeStep("Slice the steak against the grain and build the bowl."),
        ),
        videoUrl = "https://www.youtube.com/results?search_query=steak+quinoa+bowl",
    ),
    Recipe(
        id = "juicy-pasta",
        name = "Juicy pasta",
        emoji = "🍝",
        xpReward = 80,
        timeMinutes = 14,
        difficulty = Difficulty.EASY,
        servings = 2,
        caloriesKcal = 480,
        proteinGrams = 17,
        carbsGrams = 72,
        fatGrams = 14,
        tags = listOf("Quick", "Vegetarian"),
        ingredients = listOf(
            RecipeIngredient("spaghetti", quantity = 200.0, unit = "g"),
            RecipeIngredient("canned tomatoes", quantity = 400.0, unit = "g"),
            RecipeIngredient("garlic", quantity = 2.0, unit = "cloves"),
            RecipeIngredient("Basil, olive oil, salt", isNewToUser = true),
        ),
        steps = listOf(
            RecipeStep("Boil the spaghetti in well-salted water.", timerMinutes = 9),
            RecipeStep("Gently fry the sliced garlic in olive oil."),
            RecipeStep("Add the tomatoes and simmer until thickened."),
            RecipeStep("Toss the drained pasta through the sauce with basil."),
        ),
        videoUrl = "https://www.youtube.com/results?search_query=easy+tomato+pasta",
    ),
)

/** Hungarian translation of [fallbackRecipesEn] — same `id`s and numeric fields (macros, time,
 * difficulty, servings, xpReward) so a recipe saved/cooked under one language still resolves by
 * id under the other; only the display text is translated. */
private val fallbackRecipesHu = listOf(
    Recipe(
        id = "chicken-curry",
        name = "Csirke curry kókusztejjel",
        emoji = "🍲",
        xpReward = 45,
        timeMinutes = 25,
        difficulty = Difficulty.EASY,
        servings = 2,
        caloriesKcal = 520,
        proteinGrams = 38,
        carbsGrams = 18,
        fatGrams = 32,
        tags = listOf("Laktató", "Egytálétel"),
        ingredients = listOf(
            RecipeIngredient("csirkecomb", quantity = 300.0, unit = "g"),
            RecipeIngredient("kókusztej", quantity = 400.0, unit = "ml"),
            RecipeIngredient("sárga curry paszta", quantity = 2.0, unit = "evőkanál"),
            RecipeIngredient("vöröshagyma", quantity = 1.0),
            RecipeIngredient("Koriander, lime, só"),
        ),
        steps = listOf(
            RecipeStep("Kockázd fel a csirkét, és szeleteld fel a hagymát."),
            RecipeStep("Pirítsd meg a curry pasztát a hagymával, amíg illatos nem lesz."),
            RecipeStep("Add hozzá a csirkét, és süsd körbe minden oldalról."),
            RecipeStep("Öntsd hozzá a kókusztejet, és forrald fel.", timerMinutes = 15),
            RecipeStep("Fejezd be lime lével és friss korianderrel."),
        ),
        videoUrl = "https://www.youtube.com/results?search_query=csirke+curry+kokusztejjel",
    ),
    Recipe(
        id = "steak-quinoa-bowl",
        name = "Steak quinoa tál",
        emoji = "🥩",
        xpReward = 120,
        timeMinutes = 35,
        difficulty = Difficulty.MEDIUM,
        servings = 2,
        caloriesKcal = 610,
        proteinGrams = 46,
        carbsGrams = 52,
        fatGrams = 24,
        tags = listOf("Fehérjedús", "Előre elkészíthető"),
        ingredients = listOf(
            RecipeIngredient("marha steak", quantity = 250.0, unit = "g"),
            RecipeIngredient("quinoa", quantity = 150.0, unit = "g"),
            RecipeIngredient("koktélparadicsom", quantity = 100.0, unit = "g"),
            RecipeIngredient("avokádó", quantity = 1.0),
            RecipeIngredient("Olívaolaj, citrom, só, bors"),
        ),
        steps = listOf(
            RecipeStep("Öblítsd le a quinoát, majd főzd meg sós vízben.", timerMinutes = 15),
            RecipeStep("Fűszerezd bőségesen sóval és borssal a steaket."),
            RecipeStep("Süsd a steaket 3-4 percig oldalanként, majd pihentesd.", timerMinutes = 5),
            RecipeStep("Vágd félbe a paradicsomokat, és szeleteld fel az avokádót."),
            RecipeStep("Szeleteld fel a steaket a rostokkal szemben, és állítsd össze a tálat."),
        ),
        videoUrl = "https://www.youtube.com/results?search_query=steak+quinoa+tal",
    ),
    Recipe(
        id = "juicy-pasta",
        name = "Szaftos tészta",
        emoji = "🍝",
        xpReward = 80,
        timeMinutes = 14,
        difficulty = Difficulty.EASY,
        servings = 2,
        caloriesKcal = 480,
        proteinGrams = 17,
        carbsGrams = 72,
        fatGrams = 14,
        tags = listOf("Gyors", "Vegetáriánus"),
        ingredients = listOf(
            RecipeIngredient("spagetti", quantity = 200.0, unit = "g"),
            RecipeIngredient("konzerv paradicsom", quantity = 400.0, unit = "g"),
            RecipeIngredient("fokhagyma", quantity = 2.0, unit = "gerezd"),
            RecipeIngredient("Bazsalikom, olívaolaj, só", isNewToUser = true),
        ),
        steps = listOf(
            RecipeStep("Főzd meg a spagettit bőséges sós vízben.", timerMinutes = 9),
            RecipeStep("Pirítsd meg enyhén a szeletelt fokhagymát olívaolajon."),
            RecipeStep("Add hozzá a paradicsomot, és főzd, amíg besűrűsödik."),
            RecipeStep("Forgasd össze a leszűrt tésztát a szósszal és a bazsalikommal."),
        ),
        videoUrl = "https://www.youtube.com/results?search_query=szaftos+teszta",
    ),
)
