package com.levelchef.feature.home

/** Mirrors the Figma "Home" screen (node 296:1929): level pill, XP bar, stat cards,
 * weekly challenge card, primary CTA, 3 AI recipe recommendations, last-cooked card. */
data class HomeUiState(
    val levelLabel: String = "Wok Warrior · Level 3",
    val currentXp: Int = 420,
    val xpForNextLevel: Int = 600,
    val cookingSessions: Int = 27,
    val ingredientsTried: Int = 14,
    val challengeTitle: String = "Cook one Asian-inspired dish this week",
    val challengeDescription: String = "Explore and master authentic eastern culinary techniques.",
    val challengeXp: Int = 200,
    val challengeInProgress: Boolean = true,
    val recommendations: List<RecipeRecommendation> = sampleRecommendations,
    val lastCooked: LastCooked? = LastCooked("Tofu stir-fry", "3 days ago", 3),
)
