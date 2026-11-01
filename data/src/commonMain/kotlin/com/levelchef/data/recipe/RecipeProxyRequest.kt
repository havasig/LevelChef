package com.levelchef.data.recipe

import com.levelchef.core.model.SurveyResponse
import kotlinx.serialization.Serializable

/**
 * What the app sends the recipe proxy (`proxy/src/index.js`): only the onboarding answers, as enum
 * names, plus the content language. The proxy validates every value against its own whitelist and
 * builds the Gemini prompt and response schema itself, so the endpoint can't be used to run
 * arbitrary prompts on the project's Gemini key.
 */
@Serializable
internal data class RecipeProxyRequest(
    val survey: SurveyPayload,
    val language: String,
)

@Serializable
internal data class SurveyPayload(
    val cookingExperience: String,
    val dietaryPreference: String,
    val allergens: List<String>,
    val cuisines: List<String>,
    val spiceTolerance: String,
    val cookingGoal: String,
    val weeknightTime: String,
    val householdSize: String,
)

internal fun recipeProxyRequest(survey: SurveyResponse, languageTag: String?): RecipeProxyRequest =
    RecipeProxyRequest(
        survey = SurveyPayload(
            cookingExperience = survey.cookingExperience.name,
            dietaryPreference = survey.dietaryPreference.name,
            allergens = survey.allergens.map { it.name }.sorted(),
            cuisines = survey.cuisines.map { it.name }.sorted(),
            spiceTolerance = survey.spiceTolerance.name,
            cookingGoal = survey.cookingGoal.name,
            weeknightTime = survey.weeknightTime.name,
            householdSize = survey.householdSize.name,
        ),
        language = contentLanguage(languageTag),
    )

/** The recipe-content language for an app-language tag: the app ships English and Hungarian only,
 * so anything else (or no tag) is English. */
internal fun contentLanguage(languageTag: String?): String = if (languageTag?.lowercase() == "hu") "hu" else "en"
