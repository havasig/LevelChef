package com.levelchef.core.model

/**
 * The Chef levels, in order. XP thresholds are cumulative XP required to reach that level.
 * [emoji] is this level's visual (trophy-room avatar, levels list) — same convention as
 * [Ingredient.emoji]/[Recipe.emoji].
 */
enum class ChefLevel(val displayName: String, val xpThreshold: Int, val emoji: String) {
    KITCHEN_NOVICE("Kitchen Novice", 0, "🌱"),
    RICE_COOKING_MASTER("Rice-Cooking Master", 300, "🍚"),
    WOK_WARRIOR("Wok Warrior", 800, "🥘"),
    SPICE_HUNTER("Spice Hunter", 1600, "🌶️"),
    SOUS_CHEF("Sous Chef", 2800, "👨‍🍳"),
    HEAD_CHEF("Head Chef", 4500, "🎖️"),
    MICHELIN_CONTENDER("Michelin Contender", 7000, "💫"),
    GRILL_MASTER("Grill Master", 10000, "🔥"),
    PASTRY_PRODIGY("Pastry Prodigy", 14000, "🥐"),
    CULINARY_SAGE("Culinary Sage", 19000, "🧠"),
    MICHELIN_STAR_CHEF("Michelin Star Chef", 25000, "🌟"),
    LEGENDARY_TASTEMAKER("Legendary Tastemaker", 32000, "👑");

    companion object {
        fun forXp(totalXp: Int): ChefLevel =
            entries.lastOrNull { totalXp >= it.xpThreshold } ?: KITCHEN_NOVICE

        fun next(level: ChefLevel): ChefLevel? {
            val idx = entries.indexOf(level)
            return entries.getOrNull(idx + 1)
        }
    }
}
