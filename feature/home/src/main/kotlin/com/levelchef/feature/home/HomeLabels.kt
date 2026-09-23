package com.levelchef.feature.home

import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.levelchef.core.model.ChefLevel
import com.levelchef.core.model.Difficulty

/** Localized chef-level name. The domain's `displayName` stays English for logs/tests. */
@Composable
internal fun ChefLevel.label(): String = stringResource(
    when (this) {
        ChefLevel.KITCHEN_NOVICE -> R.string.home_level_kitchen_novice
        ChefLevel.RICE_COOKING_MASTER -> R.string.home_level_rice_cooking_master
        ChefLevel.WOK_WARRIOR -> R.string.home_level_wok_warrior
        ChefLevel.SPICE_HUNTER -> R.string.home_level_spice_hunter
        ChefLevel.SOUS_CHEF -> R.string.home_level_sous_chef
        ChefLevel.HEAD_CHEF -> R.string.home_level_head_chef
        ChefLevel.MICHELIN_CONTENDER -> R.string.home_level_michelin_contender
        ChefLevel.GRILL_MASTER -> R.string.home_level_grill_master
        ChefLevel.PASTRY_PRODIGY -> R.string.home_level_pastry_prodigy
        ChefLevel.CULINARY_SAGE -> R.string.home_level_culinary_sage
        ChefLevel.MICHELIN_STAR_CHEF -> R.string.home_level_michelin_star_chef
        ChefLevel.LEGENDARY_TASTEMAKER -> R.string.home_level_legendary_tastemaker
    },
)

@Composable
internal fun Difficulty.label(): String = stringResource(
    when (this) {
        Difficulty.EASY -> R.string.home_difficulty_easy
        Difficulty.MEDIUM -> R.string.home_difficulty_medium
        Difficulty.HARD -> R.string.home_difficulty_hard
    },
)

/** Localized title of the weekly challenge [id] (see the data layer's catalog), or null for an
 * unknown id so the caller can fall back to the domain's English title. */
@StringRes
private fun weeklyChallengeTitleRes(id: String): Int? = when (id) {
    "three-meals" -> R.string.home_challenge_three_meals
    "five-star-plate" -> R.string.home_challenge_five_star_plate
    "protein-push" -> R.string.home_challenge_protein_push
    "xp-sprint" -> R.string.home_challenge_xp_sprint
    "quick-fire" -> R.string.home_challenge_quick_fire
    "light-bite" -> R.string.home_challenge_light_bite
    "rate-three" -> R.string.home_challenge_rate_three
    "kitchen-journal" -> R.string.home_challenge_kitchen_journal
    "four-day-streak" -> R.string.home_challenge_four_day_streak
    else -> null
}

@Composable
internal fun weeklyChallengeTitle(id: String, fallback: String): String =
    weeklyChallengeTitleRes(id)?.let { stringResource(it) } ?: fallback

/** "today", "1 day ago" or "N days ago". */
@Composable
internal fun daysAgoLabel(daysAgo: Int): String = when {
    daysAgo <= 0 -> stringResource(R.string.home_last_cooked_today)
    daysAgo == 1 -> stringResource(R.string.home_last_cooked_one_day_ago)
    else -> stringResource(R.string.home_last_cooked_days_ago, daysAgo)
}
