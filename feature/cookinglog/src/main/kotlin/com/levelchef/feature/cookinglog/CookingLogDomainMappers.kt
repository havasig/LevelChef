package com.levelchef.feature.cookinglog

import com.levelchef.core.model.CookingSession
import com.levelchef.core.model.Recipe
import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

/** Maps domain models (from [com.levelchef.domain.repository]) onto this screen's presentation types. */

/** Resolves saved recipe ids (most-recently-saved first) into [SavedRecipeItem]s, cross-referencing
 * [recipes] for details and [sessions] for whether — and how well — each was last cooked. A saved id
 * with no matching [Recipe] (its cache row was never populated) is dropped. */
@OptIn(ExperimentalTime::class)
internal fun toSavedRecipeItems(
    savedRecipeIds: List<String>,
    recipes: List<Recipe>,
    sessions: List<CookingSession>,
): List<SavedRecipeItem> {
    val recipesById = recipes.associateBy { it.id }
    val latestSessionByRecipeId = sessions
        .groupBy { it.recipeId }
        .mapValues { (_, forRecipe) -> forRecipe.maxBy { it.cookedAt } }

    return savedRecipeIds.mapNotNull { recipeId ->
        val recipe = recipesById[recipeId] ?: return@mapNotNull null
        val session = latestSessionByRecipeId[recipeId]
        if (session != null) {
            SavedRecipeItem.Cooked(
                recipeId = recipe.id,
                name = recipe.name,
                emoji = recipe.emoji,
                xpReward = recipe.xpReward,
                rating = session.rating ?: 0,
                cookedAgo = session.cookedAt.toCookedAgo(),
            )
        } else {
            SavedRecipeItem.Uncooked(
                recipeId = recipe.id,
                name = recipe.name,
                emoji = recipe.emoji,
                xpReward = recipe.xpReward,
                timeMinutes = recipe.timeMinutes,
                difficulty = recipe.difficulty,
            )
        }
    }
}

@OptIn(ExperimentalTime::class)
private fun Instant.toCookedAgo(): CookedAgo {
    val daysAgo = (Clock.System.now() - this).inWholeDays
    return when {
        daysAgo <= 0 -> CookedAgo.Today
        daysAgo == 1L -> CookedAgo.Yesterday
        else -> CookedAgo.DaysAgo(daysAgo)
    }
}
