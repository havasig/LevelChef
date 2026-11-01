package com.levelchef.data.recipe

import kotlinx.serialization.Serializable

@Serializable
internal data class GeminiResponse(
    val candidates: List<GeminiCandidate> = emptyList(),
)

@Serializable
internal data class GeminiCandidate(
    val content: GeminiContent? = null,
)

@Serializable
internal data class GeminiContent(
    val parts: List<GeminiPart> = emptyList(),
)

@Serializable
internal data class GeminiPart(
    val text: String? = null,
)

/**
 * The structured-output JSON (a `List<Recipe>`) arrives as a **string** inside this envelope's
 * first part, per the Generative Language API's `generateContent` response shape — it still needs
 * a second `Json.decodeFromString` pass, this only unwraps the envelope. The proxy passes Gemini's
 * response through unchanged.
 */
internal fun GeminiResponse.generatedRecipesJson(): String? =
    candidates.firstOrNull()?.content?.parts?.firstOrNull()?.text
