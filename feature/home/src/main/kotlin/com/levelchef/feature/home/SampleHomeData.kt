package com.levelchef.feature.home

import com.levelchef.core.designsystem.TagColors
import com.levelchef.core.ui.theme.TagGreenBg
import com.levelchef.core.ui.theme.TagGreenStroke
import com.levelchef.core.ui.theme.TagRedBg
import com.levelchef.core.ui.theme.TagRedStroke
import com.levelchef.core.ui.theme.TagYellowBg
import com.levelchef.core.ui.theme.TagYellowStroke

/** Placeholder sample data for [HomeUiState]'s default preview state. */
internal val sampleRecommendations = listOf(
    RecipeRecommendation("🍲", "Chicken curry with coconut milk", 45, 25, "Easy", "🌿", "New ingredient", TagColors(TagGreenBg, TagGreenStroke)),
    RecipeRecommendation("🥩", "Steak quinoa bowl", 120, 35, "Medium", "💪", "High protein", TagColors(TagYellowBg, TagYellowStroke)),
    RecipeRecommendation("🍝", "Jucy pasta", 80, 14, "Easy", "⚡", "High carb", TagColors(TagRedBg, TagRedStroke)),
)
