package com.levelchef.data.recipe

import com.levelchef.core.model.SurveyResponse

private const val RECOMMENDATION_COUNT = 5

/** Maps an app-language tag to the language name Gemini should write recipe text in — the app
 * only ships English + Hungarian today, so this stays a single entry rather than a general
 * locale-name lookup. `null` means "no instruction", i.e. the default (English). */
private fun languageName(languageTag: String?): String? = when (languageTag?.lowercase()) {
    "hu" -> "Hungarian"
    else -> null
}

/** Builds the recommendation prompt as a direct, complete translation of [SurveyResponse] — every
 * one of its 8 fields becomes one explicit line, so nothing the user told the onboarding survey
 * is silently dropped. Allergens/dietary preference are called out as hard constraints since
 * those are safety-relevant, not just taste. [languageTag] is the current app language (see
 * [languageName]); when it maps to a known non-English language, every generated text field is
 * requested in that language instead of the default English. */
internal fun buildRecipePrompt(survey: SurveyResponse, languageTag: String? = null): String = """
    Recommend $RECOMMENDATION_COUNT distinct recipes for a home cook with this profile:
    - Cooking experience: ${survey.cookingExperience}
    - Dietary preference: ${survey.dietaryPreference}
    - Allergens to strictly avoid: ${survey.allergens.ifEmpty { setOf("none") }}
    - Preferred cuisines: ${survey.cuisines.ifEmpty { setOf("no strong preference") }}
    - Spice tolerance: ${survey.spiceTolerance}
    - Primary cooking goal: ${survey.cookingGoal}
    - Available weeknight cooking time: ${survey.weeknightTime}
    - Household size: ${survey.householdSize}

    Hard constraints — every recipe MUST respect these, no exceptions:
    - Match the dietary preference exactly (e.g. VEGAN means no animal products at all).
    - Contain none of the listed allergens, in any ingredient.

    Strong preferences — follow these unless they conflict with a hard constraint above:
    - Favor the preferred cuisines and match the stated spice tolerance.
    - Keep total time within the stated weeknight time budget.
    - Scale the `servings` field to the household size (SOLO=1, TWO=2, THREE_TO_FOUR=4, FIVE_PLUS=6).
    - Lean into the stated cooking goal (e.g. HIGH_PROTEIN favors protein-forward recipes,
      QUICK_MEALS favors the shortest times, BUDGET_FRIENDLY favors inexpensive ingredients).
    - Match the difficulty to the stated cooking experience (NEVER_COOKED/BEGINNER → EASY recipes).

    For each recipe, provide:
    - id: a lowercase, URL-safe slug unique among the $RECOMMENDATION_COUNT recipes (e.g. "lemon-garlic-chicken")
    - name: a short, appealing title
    - emoji: exactly one emoji that best represents the dish
    - xpReward: an int from 30-150 reflecting how involved the recipe is (simple=lower, complex=higher)
    - timeMinutes: total active + cook time
    - difficulty: one of EASY, MEDIUM, HARD
    - servings: an int, per the household-size rule above
    - caloriesKcal, proteinGrams, carbsGrams, fatGrams: realistic per-serving estimates
    - tags: 1-3 short descriptive tags (e.g. "One pot", "Meal prep")
    - ingredients: each with name, quantity (a number, omit for free-form entries),
      unit (e.g. "g", "tbsp"; omit for free-form)
    - steps: each with text, and timerMinutes only on steps that involve unattended waiting
      (simmering, marinating, resting)
    - videoUrl: a YouTube search URL for the dish,
      e.g. "https://www.youtube.com/results?search_query=<url-encoded dish name>"

    Return ONLY the JSON array of $RECOMMENDATION_COUNT recipes, matching the provided schema exactly.
""".trimIndent() + languageInstruction(languageTag)

private fun languageInstruction(languageTag: String?): String {
    val language = languageName(languageTag) ?: return ""
    return "\n\nWrite the recipe content in $language: name, tags, ingredient names, and step text. " +
        "Keep id as a lowercase ASCII slug regardless of language; videoUrl stays a valid YouTube " +
        "search URL (the search query itself may be in $language)."
}
