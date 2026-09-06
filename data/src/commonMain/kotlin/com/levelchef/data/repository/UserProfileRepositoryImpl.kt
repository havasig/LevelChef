package com.levelchef.data.repository

import com.levelchef.core.model.CookingSession
import com.levelchef.core.model.UserProfile
import com.levelchef.domain.repository.CookingSessionRepository
import com.levelchef.domain.repository.IngredientRepository
import com.levelchef.domain.repository.UserProfileRepository
import com.levelchef.domain.repository.WeeklyChallengeRepository
import kotlinx.coroutines.flow.first
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.toLocalDateTime
import kotlin.math.roundToInt

/** Derives the aggregate [UserProfile] from cooking session history, the pantry and weekly-
 * challenge bonus XP. */
class UserProfileRepositoryImpl(
    private val cookingSessionRepository: CookingSessionRepository,
    private val ingredientRepository: IngredientRepository,
    private val weeklyChallengeRepository: WeeklyChallengeRepository,
) : UserProfileRepository {

    override suspend fun getProfile(): UserProfile {
        val sessions = cookingSessionRepository.observeAll().first()
        return UserProfile(
            totalXp = cookingSessionRepository.totalXp() + weeklyChallengeRepository.totalAwardedXp(),
            cookingSessionsCount = cookingSessionRepository.sessionCount(),
            newIngredientsCount = ingredientRepository.count(),
            kitchenTimeMinutes = cookingSessionRepository.totalDurationMinutes(),
            currentStreakDays = streakDays(sessions),
            avgRatingPercent = avgRatingPercent(sessions),
        )
    }
}

/** Consecutive calendar days (UTC) of cooking sessions, counted back from the most recently
 * cooked day — not necessarily "today", so this doesn't reset just because the profile is read
 * on a day nothing has been cooked yet. */
private fun streakDays(sessions: List<CookingSession>): Int {
    val dates = sessions.map { it.cookedAt.toLocalDateTime(TimeZone.UTC).date }.distinct().sortedDescending()
    if (dates.isEmpty()) return 0
    var streak = 1
    for (i in 1 until dates.size) {
        if (dates[i] == dates[i - 1].minus(1, DateTimeUnit.DAY)) streak++ else break
    }
    return streak
}

/** Average of every rated session's star rating, scaled from a 1-5 scale to a 0-100 percentage. */
private fun avgRatingPercent(sessions: List<CookingSession>): Int? {
    val ratings = sessions.mapNotNull { it.rating }
    if (ratings.isEmpty()) return null
    return (ratings.average() / MAX_RATING * PERCENT).roundToInt()
}

private const val MAX_RATING = 5.0
private const val PERCENT = 100.0
