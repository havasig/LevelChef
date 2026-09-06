package com.levelchef.feature.trophyroom

import app.cash.turbine.test
import com.levelchef.core.model.Badge
import com.levelchef.core.model.BadgeCategory
import com.levelchef.core.model.UserProfile
import com.levelchef.core.model.WeeklyChallenge
import com.levelchef.domain.repository.BadgeRepository
import com.levelchef.domain.repository.UserProfileRepository
import com.levelchef.domain.repository.WeeklyChallengeRepository
import com.levelchef.domain.usecase.GetChefLevelUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class TrophyRoomViewModelTest {

    private val dispatcher = StandardTestDispatcher()

    @BeforeTest
    fun setUp() = Dispatchers.setMain(dispatcher)

    @AfterTest
    fun tearDown() = Dispatchers.resetMain()

    private fun badge(id: String, category: BadgeCategory, current: Int = 0, target: Int = 1) = Badge(
        id = id,
        name = id,
        emoji = "🏅",
        category = category,
        description = "$id description",
        progressCurrent = current,
        progressTarget = target,
    )

    private fun viewModel(
        profile: UserProfile = UserProfile(totalXp = 800, cookingSessionsCount = 12, newIngredientsCount = 5),
        badges: List<Badge> = emptyList(),
        challenge: WeeklyChallenge = WeeklyChallenge(id = "c", title = "Title", description = "Desc", xpReward = 100),
        badgeRepository: BadgeRepository = FakeBadgeRepository(badges),
    ): TrophyRoomViewModel {
        val userProfileRepository = FakeUserProfileRepository(profile)
        return TrophyRoomViewModel(
            userProfileRepository = userProfileRepository,
            badgeRepository = badgeRepository,
            weeklyChallengeRepository = FakeWeeklyChallengeRepository(challenge),
            getChefLevelUseCase = GetChefLevelUseCase(userProfileRepository),
        )
    }

    @Test
    fun ui_state_loads_and_maps_domain_data_into_the_screen_model() = runTest(dispatcher) {
        val viewModel = viewModel(
            profile = UserProfile(
                totalXp = 800,
                cookingSessionsCount = 12,
                newIngredientsCount = 5,
                kitchenTimeMinutes = 90,
                currentStreakDays = 4,
                avgRatingPercent = 80,
            ),
            challenge = WeeklyChallenge(id = "c", title = "T", description = "D", xpReward = 50, progressCurrent = 2, progressTarget = 3),
        )

        viewModel.uiState.test {
            testScheduler.advanceUntilIdle()
            val loaded = expectMostRecentItem()

            assertEquals("Wok Warrior", loaded.levelName)
            assertEquals(800, loaded.currentXp)
            assertEquals(12, loaded.cookingSessions)
            assertEquals(5, loaded.ingredientsTried)
            assertEquals(4, loaded.streakDays)
            assertEquals(80, loaded.avgRatingPercent)
            assertEquals("1h 30m", loaded.kitchenTimeLabel)
            assertEquals("2/3", loaded.weeklyChallengeProgressText)
            assertEquals(false, loaded.weeklyChallengeCompleted)
        }
    }

    @Test
    fun weekly_challenge_completed_state_is_reflected() = runTest(dispatcher) {
        val viewModel = viewModel(
            challenge = WeeklyChallenge(
                id = "c",
                title = "T",
                description = "D",
                xpReward = 50,
                progressCurrent = 3,
                progressTarget = 3,
                completedAt = kotlinx.datetime.Instant.parse("2026-01-01T00:00:00Z"),
            ),
        )

        viewModel.uiState.test {
            testScheduler.advanceUntilIdle()
            assertTrue(expectMostRecentItem().weeklyChallengeCompleted)
        }
    }

    @Test
    fun badges_are_split_into_streaks_and_badges_sections_by_category() = runTest(dispatcher) {
        val viewModel = viewModel(
            badges = listOf(
                badge("night-owl", BadgeCategory.ACHIEVEMENT),
                badge("marathon-cook", BadgeCategory.ACHIEVEMENT, current = 300, target = 600),
                badge("first-bite", BadgeCategory.QUANTITY, current = 1, target = 1),
                badge("pantry-starter", BadgeCategory.DISCOVERY, current = 2, target = 5),
            ),
        )

        viewModel.uiState.test {
            testScheduler.advanceUntilIdle()
            val loaded = expectMostRecentItem()

            assertEquals(setOf("night-owl", "marathon-cook"), loaded.streakBadges.map { it.id }.toSet())
            assertEquals(setOf("first-bite", "pantry-starter"), loaded.badges.map { it.id }.toSet())
        }
    }

    @Test
    fun refresh_persists_newly_earned_badges_before_reading_them_back() = runTest(dispatcher) {
        val badgeRepository = FakeBadgeRepository(listOf(badge("first-bite", BadgeCategory.QUANTITY, current = 1, target = 1)))
        viewModel(badgeRepository = badgeRepository)

        testScheduler.advanceUntilIdle()

        assertTrue(badgeRepository.refreshEarnedCalled)
    }
}

private class FakeUserProfileRepository(private val profile: UserProfile) : UserProfileRepository {
    override suspend fun getProfile(): UserProfile = profile
}

private class FakeBadgeRepository(private val badges: List<Badge> = emptyList()) : BadgeRepository {
    var refreshEarnedCalled = false
        private set

    override fun observeAll(): Flow<List<Badge>> = flowOf(badges)
    override suspend fun refreshEarned() {
        refreshEarnedCalled = true
    }
}

private class FakeWeeklyChallengeRepository(private val current: WeeklyChallenge) : WeeklyChallengeRepository {
    override fun observeCurrent(): Flow<WeeklyChallenge> = flowOf(current)
    override suspend fun complete(id: String) = Unit
    override suspend fun totalAwardedXp(): Int = 0
}
