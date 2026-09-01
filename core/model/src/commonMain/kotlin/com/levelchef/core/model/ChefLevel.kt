package com.levelchef.core.model

/**
 * The 7 Chef levels, in order. XP thresholds are cumulative XP required to reach that level.
 */
enum class ChefLevel(val displayName: String, val xpThreshold: Int) {
    KITCHEN_NOVICE("Kitchen Novice", 0),
    RICE_COOKING_MASTER("Rice-Cooking Master", 300),
    WOK_WARRIOR("Wok Warrior", 800),
    SPICE_HUNTER("Spice Hunter", 1600),
    SOUS_CHEF("Sous Chef", 2800),
    HEAD_CHEF("Head Chef", 4500),
    MICHELIN_CONTENDER("Michelin Contender", 7000);

    companion object {
        fun forXp(totalXp: Int): ChefLevel =
            entries.lastOrNull { totalXp >= it.xpThreshold } ?: KITCHEN_NOVICE

        fun next(level: ChefLevel): ChefLevel? {
            val idx = entries.indexOf(level)
            return entries.getOrNull(idx + 1)
        }
    }
}
