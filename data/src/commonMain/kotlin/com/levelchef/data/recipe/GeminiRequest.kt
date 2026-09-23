package com.levelchef.data.recipe

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.add
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonArray
import kotlinx.serialization.json.putJsonObject

@Serializable
internal data class GeminiRequest(
    val contents: List<GeminiContent>,
    val generationConfig: GeminiGenerationConfig,
)

@Serializable
internal data class GeminiContent(
    val parts: List<GeminiPart>,
)

@Serializable
internal data class GeminiPart(
    val text: String,
)

@Serializable
internal data class GeminiGenerationConfig(
    val responseMimeType: String = "application/json",
    val responseSchema: JsonElement,
)

internal fun geminiRecipeRequest(prompt: String): GeminiRequest = GeminiRequest(
    contents = listOf(GeminiContent(parts = listOf(GeminiPart(text = prompt)))),
    generationConfig = GeminiGenerationConfig(responseSchema = geminiRecipeListSchema()),
)

private val requiredRecipeFields = listOf(
    "id", "name", "emoji", "xpReward", "timeMinutes", "difficulty", "servings", "tags", "ingredients", "steps",
)

/**
 * Gemini's `responseSchema` uses a restricted OpenAPI-style schema (uppercase `type` names, no
 * `$ref`) rather than standard JSON Schema, so it's built as a raw [JsonElement] tree instead of a
 * fixed data class — it mirrors [com.levelchef.core.model.Recipe]'s shape field-for-field.
 */
private fun geminiRecipeListSchema(): JsonElement = buildJsonObject {
    put("type", "ARRAY")
    putJsonObject("items") {
        put("type", "OBJECT")
        putJsonArray("required") {
            requiredRecipeFields.forEach { add(it) }
        }
        putJsonObject("properties") {
            putJsonObject("id") { put("type", "STRING") }
            putJsonObject("name") { put("type", "STRING") }
            putJsonObject("emoji") { put("type", "STRING") }
            putJsonObject("xpReward") { put("type", "INTEGER") }
            putJsonObject("timeMinutes") { put("type", "INTEGER") }
            putJsonObject("difficulty") {
                put("type", "STRING")
                putJsonArray("enum") { listOf("EASY", "MEDIUM", "HARD").forEach { add(it) } }
            }
            putJsonObject("servings") { put("type", "INTEGER") }
            putJsonObject("caloriesKcal") { put("type", "INTEGER") }
            putJsonObject("proteinGrams") { put("type", "INTEGER") }
            putJsonObject("carbsGrams") { put("type", "INTEGER") }
            putJsonObject("fatGrams") { put("type", "INTEGER") }
            putJsonObject("tags") {
                put("type", "ARRAY")
                putJsonObject("items") { put("type", "STRING") }
            }
            putJsonObject("ingredients") {
                put("type", "ARRAY")
                putJsonObject("items") {
                    put("type", "OBJECT")
                    putJsonArray("required") { add("name") }
                    putJsonObject("properties") {
                        putJsonObject("name") { put("type", "STRING") }
                        putJsonObject("quantity") { put("type", "NUMBER") }
                        putJsonObject("unit") { put("type", "STRING") }
                    }
                }
            }
            putJsonObject("steps") {
                put("type", "ARRAY")
                putJsonObject("items") {
                    put("type", "OBJECT")
                    putJsonArray("required") { add("text") }
                    putJsonObject("properties") {
                        putJsonObject("text") { put("type", "STRING") }
                        putJsonObject("timerMinutes") { put("type", "INTEGER") }
                    }
                }
            }
            putJsonObject("videoUrl") { put("type", "STRING") }
        }
    }
}
