package com.levelchef.feature.home

import androidx.compose.runtime.Composable
import com.levelchef.core.model.ChefLevel
import com.levelchef.core.model.Difficulty
import com.levelchef.feature.home.generated.resources.Res
import com.levelchef.feature.home.generated.resources.home_challenge_five_star_plate
import com.levelchef.feature.home.generated.resources.home_challenge_four_day_streak
import com.levelchef.feature.home.generated.resources.home_challenge_kitchen_journal
import com.levelchef.feature.home.generated.resources.home_challenge_light_bite
import com.levelchef.feature.home.generated.resources.home_challenge_protein_push
import com.levelchef.feature.home.generated.resources.home_challenge_quick_fire
import com.levelchef.feature.home.generated.resources.home_challenge_rate_three
import com.levelchef.feature.home.generated.resources.home_challenge_three_meals
import com.levelchef.feature.home.generated.resources.home_challenge_xp_sprint
import com.levelchef.feature.home.generated.resources.home_difficulty_easy
import com.levelchef.feature.home.generated.resources.home_difficulty_hard
import com.levelchef.feature.home.generated.resources.home_difficulty_medium
import com.levelchef.feature.home.generated.resources.home_last_cooked_days_ago
import com.levelchef.feature.home.generated.resources.home_last_cooked_one_day_ago
import com.levelchef.feature.home.generated.resources.home_last_cooked_today
import com.levelchef.feature.home.generated.resources.home_level_culinary_sage
import com.levelchef.feature.home.generated.resources.home_level_grill_master
import com.levelchef.feature.home.generated.resources.home_level_head_chef
import com.levelchef.feature.home.generated.resources.home_level_kitchen_novice
import com.levelchef.feature.home.generated.resources.home_level_legendary_tastemaker
import com.levelchef.feature.home.generated.resources.home_level_michelin_contender
import com.levelchef.feature.home.generated.resources.home_level_michelin_star_chef
import com.levelchef.feature.home.generated.resources.home_level_pastry_prodigy
import com.levelchef.feature.home.generated.resources.home_level_rice_cooking_master
import com.levelchef.feature.home.generated.resources.home_level_sous_chef
import com.levelchef.feature.home.generated.resources.home_level_spice_hunter
import com.levelchef.feature.home.generated.resources.home_level_wok_warrior
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

/** Localized chef-level name. The domain's `displayName` stays English for logs/tests. */
@Composable
internal fun ChefLevel.label(): String = stringResource(
    when (this) {
        ChefLevel.KITCHEN_NOVICE -> Res.string.home_level_kitchen_novice
        ChefLevel.RICE_COOKING_MASTER -> Res.string.home_level_rice_cooking_master
        ChefLevel.WOK_WARRIOR -> Res.string.home_level_wok_warrior
        ChefLevel.SPICE_HUNTER -> Res.string.home_level_spice_hunter
        ChefLevel.SOUS_CHEF -> Res.string.home_level_sous_chef
        ChefLevel.HEAD_CHEF -> Res.string.home_level_head_chef
        ChefLevel.MICHELIN_CONTENDER -> Res.string.home_level_michelin_contender
        ChefLevel.GRILL_MASTER -> Res.string.home_level_grill_master
        ChefLevel.PASTRY_PRODIGY -> Res.string.home_level_pastry_prodigy
        ChefLevel.CULINARY_SAGE -> Res.string.home_level_culinary_sage
        ChefLevel.MICHELIN_STAR_CHEF -> Res.string.home_level_michelin_star_chef
        ChefLevel.LEGENDARY_TASTEMAKER -> Res.string.home_level_legendary_tastemaker
    },
)

@Composable
internal fun Difficulty.label(): String = stringResource(
    when (this) {
        Difficulty.EASY -> Res.string.home_difficulty_easy
        Difficulty.MEDIUM -> Res.string.home_difficulty_medium
        Difficulty.HARD -> Res.string.home_difficulty_hard
    },
)

/** Localized title of the weekly challenge [id] (see the data layer's catalog), or null for an
 * unknown id so the caller can fall back to the domain's English title. */
private fun weeklyChallengeTitleRes(id: String): StringResource? = when (id) {
    "three-meals" -> Res.string.home_challenge_three_meals
    "five-star-plate" -> Res.string.home_challenge_five_star_plate
    "protein-push" -> Res.string.home_challenge_protein_push
    "xp-sprint" -> Res.string.home_challenge_xp_sprint
    "quick-fire" -> Res.string.home_challenge_quick_fire
    "light-bite" -> Res.string.home_challenge_light_bite
    "rate-three" -> Res.string.home_challenge_rate_three
    "kitchen-journal" -> Res.string.home_challenge_kitchen_journal
    "four-day-streak" -> Res.string.home_challenge_four_day_streak
    else -> null
}

@Composable
internal fun weeklyChallengeTitle(id: String, fallback: String): String =
    weeklyChallengeTitleRes(id)?.let { stringResource(it) } ?: fallback

/** "today", "1 day ago" or "N days ago". */
@Composable
internal fun daysAgoLabel(daysAgo: Int): String = when {
    daysAgo <= 0 -> stringResource(Res.string.home_last_cooked_today)
    daysAgo == 1 -> stringResource(Res.string.home_last_cooked_one_day_ago)
    else -> stringResource(Res.string.home_last_cooked_days_ago, daysAgo)
}
