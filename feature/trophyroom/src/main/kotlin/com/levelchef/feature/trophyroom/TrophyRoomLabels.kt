package com.levelchef.feature.trophyroom

import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.levelchef.core.model.ChefLevel

/** Localized chef-level name. The domain's `displayName` stays English for logs/tests. */
@Composable
internal fun ChefLevel.label(): String = stringResource(
    when (this) {
        ChefLevel.KITCHEN_NOVICE -> R.string.trophy_room_level_kitchen_novice
        ChefLevel.RICE_COOKING_MASTER -> R.string.trophy_room_level_rice_cooking_master
        ChefLevel.WOK_WARRIOR -> R.string.trophy_room_level_wok_warrior
        ChefLevel.SPICE_HUNTER -> R.string.trophy_room_level_spice_hunter
        ChefLevel.SOUS_CHEF -> R.string.trophy_room_level_sous_chef
        ChefLevel.HEAD_CHEF -> R.string.trophy_room_level_head_chef
        ChefLevel.MICHELIN_CONTENDER -> R.string.trophy_room_level_michelin_contender
        ChefLevel.GRILL_MASTER -> R.string.trophy_room_level_grill_master
        ChefLevel.PASTRY_PRODIGY -> R.string.trophy_room_level_pastry_prodigy
        ChefLevel.CULINARY_SAGE -> R.string.trophy_room_level_culinary_sage
        ChefLevel.MICHELIN_STAR_CHEF -> R.string.trophy_room_level_michelin_star_chef
        ChefLevel.LEGENDARY_TASTEMAKER -> R.string.trophy_room_level_legendary_tastemaker
    },
)

/** Name/description string pair for a badge id from the data layer's catalog; null if unknown. */
private fun badgeTextRes(id: String): Pair<Int, Int>? = when (id) {
    "first-bite" -> R.string.trophy_room_badge_first_bite to R.string.trophy_room_badge_first_bite_desc
    "ten-meals-deep" -> R.string.trophy_room_badge_ten_meals_deep to R.string.trophy_room_badge_ten_meals_deep_desc
    "century-chef" -> R.string.trophy_room_badge_century_chef to R.string.trophy_room_badge_century_chef_desc
    "xp-overachiever" -> R.string.trophy_room_badge_xp_overachiever to R.string.trophy_room_badge_xp_overachiever_desc
    "pantry-starter" -> R.string.trophy_room_badge_pantry_starter to R.string.trophy_room_badge_pantry_starter_desc
    "ingredient-explorer" ->
        R.string.trophy_room_badge_ingredient_explorer to R.string.trophy_room_badge_ingredient_explorer_desc
    "full-shelf" -> R.string.trophy_room_badge_full_shelf to R.string.trophy_room_badge_full_shelf_desc
    "protein-pro" -> R.string.trophy_room_badge_protein_pro to R.string.trophy_room_badge_protein_pro_desc
    "night-owl" -> R.string.trophy_room_badge_night_owl to R.string.trophy_room_badge_night_owl_desc
    "early-bird" -> R.string.trophy_room_badge_early_bird to R.string.trophy_room_badge_early_bird_desc
    "perfect-plate" -> R.string.trophy_room_badge_perfect_plate to R.string.trophy_room_badge_perfect_plate_desc
    "marathon-cook" -> R.string.trophy_room_badge_marathon_cook to R.string.trophy_room_badge_marathon_cook_desc
    else -> null
}

/** Localized badge name, falling back to the domain's English name for an unknown id. */
@Composable
internal fun BadgeUiModel.localizedName(): String = localized(badgeTextRes(id)?.first, name)

/** Localized badge description, falling back to the domain's English text for an unknown id. */
@Composable
internal fun BadgeUiModel.localizedDescription(): String = localized(badgeTextRes(id)?.second, description)

@Composable
private fun localized(@StringRes res: Int?, fallback: String): String = res?.let { stringResource(it) } ?: fallback

private const val MINUTES_PER_HOUR = 60

/** "0m" below an hour, else whole hours plus any leftover minutes, e.g. "14h 30m" (localized units). */
@Composable
internal fun kitchenTimeLabel(totalMinutes: Int): String {
    val hours = totalMinutes / MINUTES_PER_HOUR
    val minutes = totalMinutes % MINUTES_PER_HOUR
    return when {
        hours <= 0 -> stringResource(R.string.trophy_room_kitchen_time_minutes, minutes)
        minutes == 0 -> stringResource(R.string.trophy_room_kitchen_time_hours, hours)
        else -> stringResource(R.string.trophy_room_kitchen_time_hours_minutes, hours, minutes)
    }
}
