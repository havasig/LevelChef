package com.levelchef.data.repository

import com.levelchef.core.model.WeeklyChallenge
import com.levelchef.domain.repository.WeeklyChallengeRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

/** Hand-written fake following the `Fake…Repository` pattern from `GetChefLevelUseCaseTest`. */
internal class FakeWeeklyChallengeRepository(
    private val current: WeeklyChallenge = WeeklyChallenge(id = "c", title = "", description = "", xpReward = 0),
    private val awardedXp: Int = 0,
) : WeeklyChallengeRepository {
    val completed = mutableListOf<String>()

    override fun observeCurrent(): Flow<WeeklyChallenge> = flowOf(current)
    override suspend fun complete(id: String) {
        completed += id
    }
    override suspend fun totalAwardedXp(): Int = awardedXp
}
