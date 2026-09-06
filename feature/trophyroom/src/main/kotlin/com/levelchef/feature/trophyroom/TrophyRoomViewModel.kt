package com.levelchef.feature.trophyroom

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.levelchef.core.model.BadgeCategory
import com.levelchef.core.model.ChefLevel
import com.levelchef.domain.repository.BadgeRepository
import com.levelchef.domain.repository.UserProfileRepository
import com.levelchef.domain.repository.WeeklyChallengeRepository
import com.levelchef.domain.usecase.GetChefLevelUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/** Loads [TrophyRoomUiState] from the domain layer: chef level + XP/streak/kitchen-time from
 * [UserProfileRepository], badges from [BadgeRepository] (split into the "Streaks"/ACHIEVEMENT
 * and "Badges"/other sections), and the active week's challenge from [WeeklyChallengeRepository]. */
class TrophyRoomViewModel(
    private val userProfileRepository: UserProfileRepository,
    private val badgeRepository: BadgeRepository,
    private val weeklyChallengeRepository: WeeklyChallengeRepository,
    private val getChefLevelUseCase: GetChefLevelUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(TrophyRoomUiState())
    val uiState: StateFlow<TrophyRoomUiState> = _uiState.asStateFlow()

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            badgeRepository.refreshEarned()

            val profile = userProfileRepository.getProfile()
            val level = getChefLevelUseCase()
            val badges = badgeRepository.observeAll().first()
            val challenge = weeklyChallengeRepository.observeCurrent().first()
            val (streakBadges, otherBadges) = badges.partition { it.category == BadgeCategory.ACHIEVEMENT }

            _uiState.value = TrophyRoomUiState(
                levelEmoji = level.emoji,
                levelName = level.displayName,
                levelIndex = ChefLevel.entries.indexOf(level) + 1,
                levelCount = ChefLevel.entries.size,
                isMaxLevel = ChefLevel.next(level) == null,
                currentXp = profile.totalXp,
                xpForNextLevel = (ChefLevel.next(level) ?: level).xpThreshold,
                streakDays = profile.currentStreakDays,
                avgRatingPercent = profile.avgRatingPercent,
                weeklyChallengeCompleted = challenge.isCompleted,
                weeklyChallengeProgressText = "${challenge.progressCurrent}/${challenge.progressTarget}",
                kitchenTimeLabel = formatKitchenTime(profile.kitchenTimeMinutes),
                cookingSessions = profile.cookingSessionsCount,
                ingredientsTried = profile.newIngredientsCount,
                streakBadges = streakBadges.map { it.toBadgeUiModel() },
                badges = otherBadges.map { it.toBadgeUiModel() },
            )
        }
    }
}
