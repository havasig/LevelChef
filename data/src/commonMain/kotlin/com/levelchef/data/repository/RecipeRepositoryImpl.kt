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
 * and cached in [LevelChefDatabase]'s `generatedRecipe` table, keyed by a fingerprint of the survey
 * that produced them — a repeat call with an unchanged survey is served from the cache, and only a
 * changed survey (or an empty cache) triggers a new Gemini call. [getById] resolves against the
 * whole cache history, not just the latest batch, so a saved/cooked recipe stays resolvable even
 * after a newer batch replaces it in [getRecommendations]. A blank [apiKey], or any failure talking
 * to Gemini, falls back to existing cache rows and finally to [fallbackRecipes] — the app never
 * shows an empty "Recommended for you." Blocking SQLite calls run on [dispatcher] — `Dispatchers.IO`
 * on Android (see `databaseModule`).
 */
class RecipeRepositoryImpl(
    private val database: LevelChefDatabase,
    private val surveyRepository: SurveyRepository,
    private val httpClient: HttpClient,
    private val apiKey: String,
    private val clock: Clock = Clock.System,
    private val dispatcher: CoroutineDispatcher = Dispatchers.Default,
) : RecipeRepository {

    private val queries: GeneratedRecipeQueries get() = database.generatedRecipeQueries

    override suspend fun getRecommendations(): List<Recipe> {
        val survey = surveyRepository.observeResponse().first() ?: return fallbackRecipes
        val fingerprint = survey.fingerprint()

        if (latestCachedFingerprint() == fingerprint) {
            val cached = cachedRecipes()
            if (cached.isNotEmpty()) return cached
        }

        return generateRecommendations(survey, fingerprint) ?: cachedRecipes().ifEmpty { fallbackRecipes }
    }

    override suspend fun getById(id: String): Recipe? =
        withContext(dispatcher) { queries.selectById(id).executeAsOneOrNull() }?.decodeRecipeOrNull()
            ?: fallbackRecipes.find { it.id == id }

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
            setBody(geminiRecipeRequest(buildRecipePrompt(survey)))
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

/** A stable-enough hash of the answers that matter for recommendations — not security-sensitive,
 * only used to detect "the user retook the survey with different answers." */
private fun SurveyResponse.fingerprint(): String = hashCode().toString()

/** Small bundled set served when there's no survey yet, no API key configured, or Gemini/the cache
 * are both unavailable — the last-resort source so Home never shows an empty recommendation list. */
private val fallbackRecipes = listOf(
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
