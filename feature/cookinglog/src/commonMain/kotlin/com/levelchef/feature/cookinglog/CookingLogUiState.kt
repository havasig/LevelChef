package com.levelchef.feature.cookinglog

import com.levelchef.core.model.Difficulty

/** Screen model for [CookingLogScreen] — the "My saved recipes" screen (Figma node 489:1362),
 * which is this app's real content for the Recipes bottom-nav tab. */
data class CookingLogUiState(
    val loading: Boolean = true,
    val query: String = "",
    val selectedTab: CookingLogTab = CookingLogTab.ALL,
    /** Every saved recipe, already resolved from the domain layer. Filtering by [selectedTab] and
     * [query] happens in [CookingLogScreen] so switching tabs doesn't need a reload. */
    val allItems: List<SavedRecipeItem> = emptyList(),
)

enum class CookingLogTab { ALL, COOKED, NEW }

/** One row in the saved-recipes list — cooked ones render as a [com.levelchef.core.designsystem
 * .LevelChefLastCookedCard], uncooked ones as a [com.levelchef.core.designsystem.LevelChefRecipeCard]. */
sealed interface SavedRecipeItem {
    val recipeId: String
    val name: String
    val emoji: String
    val xpReward: Int

    data class Uncooked(
        override val recipeId: String,
        override val name: String,
        override val emoji: String,
        override val xpReward: Int,
        val timeMinutes: Int,
        val difficulty: Difficulty,
    ) : SavedRecipeItem

    data class Cooked(
        override val recipeId: String,
        override val name: String,
        override val emoji: String,
        override val xpReward: Int,
        val rating: Int,
        val cookedAgo: CookedAgo,
    ) : SavedRecipeItem
}

/** How long ago a recipe was last cooked, pre-bucketed so the screen doesn't need [kotlinx.datetime]
 * to render it — see [com.levelchef.feature.cookinglog.CookedAgo.label]. */
sealed interface CookedAgo {
    data object Today : CookedAgo
    data object Yesterday : CookedAgo
    data class DaysAgo(val days: Long) : CookedAgo
}
