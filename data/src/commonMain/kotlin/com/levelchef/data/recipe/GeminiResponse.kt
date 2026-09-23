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

/**
 * The structured-output JSON (a `List<Recipe>`) arrives as a **string** inside this envelope's
 * first part, per the Generative Language API's `generateContent` response shape — it still needs
 * a second `Json.decodeFromString` pass, this only unwraps the envelope.
 */
internal fun GeminiResponse.generatedRecipesJson(): String? =
    candidates.firstOrNull()?.content?.parts?.firstOrNull()?.text
