package com.levelchef.feature.home

import com.levelchef.core.model.ChefLevel

/** Mirrors the Figma "Home" screen (node 296:1929): level pill, XP bar, stat cards,
 * weekly challenge card, primary CTA, 3 AI recipe recommendations, last-cooked card. */
data class HomeUiState(
    val level: ChefLevel = ChefLevel.WOK_WARRIOR,
    val currentXp: Int = 420,
    val xpForNextLevel: Int = 600,
    val cookingSessions: Int = 27,
    val ingredientsTried: Int = 14,
    val challengeId: String = "",
    val challengeTitle: String = "Cook one Asian-inspired dish this week",
    val challengeXp: Int = 200,
    val challengeCompleted: Boolean = false,
    val challengeEligible: Boolean = false,
    val recommendations: List<RecipeRecommendation> = sampleRecommendations,
    val lastCooked: LastCooked? = LastCooked("Tofu stir-fry", daysAgo = 3, stars = 3),
)
