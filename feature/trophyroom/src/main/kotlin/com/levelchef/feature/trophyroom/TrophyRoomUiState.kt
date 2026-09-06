package com.levelchef.feature.trophyroom

/** Mirrors the Trophy Room screen: chef-level header (avatar/name/XP bar/streak), weekly-challenge
 * and kitchen-time stat cards, a "Streaks" section ([BadgeCategory.ACHIEVEMENT] badges, shown with
 * live progress) and a "Badges" section (QUANTITY/DISCOVERY badges, shown earned-or-locked). */
data class TrophyRoomUiState(
    val levelEmoji: String = "🥘",
    val levelName: String = "Wok Warrior",
    val levelIndex: Int = 3,
    val levelCount: Int = 12,
    val isMaxLevel: Boolean = false,
    val currentXp: Int = 420,
    val xpForNextLevel: Int = 600,
    val streakDays: Int = 0,
    val avgRatingPercent: Int? = null,
    val weeklyChallengeCompleted: Boolean = false,
    val weeklyChallengeProgressText: String = "0/1",
    val kitchenTimeLabel: String = "0m",
    val cookingSessions: Int = 0,
    val ingredientsTried: Int = 0,
    val streakBadges: List<BadgeUiModel> = sampleStreakBadges,
    val badges: List<BadgeUiModel> = sampleBadges,
)
