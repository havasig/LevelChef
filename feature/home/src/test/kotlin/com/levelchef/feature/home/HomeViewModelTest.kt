package com.levelchef.feature.home

import app.cash.turbine.test
import com.levelchef.core.model.ChefLevel
import com.levelchef.core.model.CookingSession
import com.levelchef.core.model.Difficulty
import com.levelchef.core.model.Recipe
import com.levelchef.core.model.UserProfile
import com.levelchef.core.model.WeeklyChallenge
import com.levelchef.domain.repository.CookingSessionRepository
import com.levelchef.domain.repository.RecipeRepository
import com.levelchef.domain.repository.UserProfileRepository
import com.levelchef.domain.repository.WeeklyChallengeRepository
import com.levelchef.domain.usecase.GetChefLevelUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.datetime.Instant
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlin.time.Clock
import kotlin.time.Duration.Companion.days
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class, ExperimentalCoroutinesApi::class)
class HomeViewModelTest {

    private val dispatcher = StandardTestDispatcher()

    @BeforeTest
    fun setUp() = Dispatchers.setMain(dispatcher)

    @AfterTest
    fun tearDown() = Dispatchers.resetMain()

    @Test
    fun ui_state_loads_and_maps_domain_data_into_the_screen_model() = runTest(dispatcher) {
        val viewModel = viewModel(
            profile = UserProfile(totalXp = 800, cookingSessionsCount = 12, newIngredientsCount = 5),
            lastCooked = session(rating = 4, cookedAt = Clock.System.now() - 3.days),
            recommendations = listOf(
                Recipe("r1", "Miso soup", "🍜", xpReward = 30, timeMinutes = 10, difficulty = Difficulty.EASY),
            ),
        )

        viewModel.uiState.test {
            testScheduler.advanceUntilIdle()
            val loaded = expectMostRecentItem()

            assertEquals(ChefLevel.WOK_WARRIOR, loaded.level)
            assertEquals(800, loaded.currentXp)
            assertEquals(12, loaded.cookingSessions)
            assertEquals(5, loaded.ingredientsTried)
            assertEquals("Miso soup", loaded.recommendations.single().name)
            assertEquals(Difficulty.EASY, loaded.recommendations.single().difficulty)
            assertEquals(null, loaded.recommendations.single().tag)
            assertEquals(3, loaded.lastCooked?.daysAgo)
            assertEquals(4, loaded.lastCooked?.stars)
        }
    }

    @Test
    fun last_cooked_is_null_when_there_is_no_session() = runTest(dispatcher) {
        viewModel(lastCooked = null).uiState.test {
            testScheduler.advanceUntilIdle()
            assertEquals(null, expectMostRecentItem().lastCooked)
        }
    }

    @Test
    fun days_ago_is_zero_for_a_session_cooked_now() = runTest(dispatcher) {
        viewModel(lastCooked = session(cookedAt = Clock.System.now())).uiState.test {
            testScheduler.advanceUntilIdle()
            assertEquals(0, expectMostRecentItem().lastCooked?.daysAgo)
        }
    }

    @Test
    fun days_ago_is_one_for_a_session_cooked_yesterday() = runTest(dispatcher) {
        viewModel(lastCooked = session(cookedAt = Clock.System.now() - 1.days)).uiState.test {
            testScheduler.advanceUntilIdle()
            assertEquals(1, expectMostRecentItem().lastCooked?.daysAgo)
        }
    }

    @Test
    fun challenge_fields_map_from_the_active_week_challenge() = runTest(dispatcher) {
        val challenge = challenge(id = "xp-sprint", title = "XP Sprint", xpReward = 150)

        viewModel(challenge = challenge).uiState.test {
            testScheduler.advanceUntilIdle()
            val loaded = expectMostRecentItem()

            assertEquals("xp-sprint", loaded.challengeId)
            assertEquals("XP Sprint", loaded.challengeTitle)
            assertEquals(150, loaded.challengeXp)
        }
    }

    @Test
    fun challenge_is_not_completed_when_its_completedAt_is_null() = runTest(dispatcher) {
        viewModel(challenge = challenge(completedAt = null)).uiState.test {
            testScheduler.advanceUntilIdle()
            assertFalse(expectMostRecentItem().challengeCompleted)
        }
    }

    @Test
    fun challenge_is_completed_when_its_completedAt_is_set() = runTest(dispatcher) {
        viewModel(challenge = challenge(completedAt = Instant.fromEpochMilliseconds(0))).uiState.test {
            testScheduler.advanceUntilIdle()
            assertTrue(expectMostRecentItem().challengeCompleted)
        }
    }

    @Test
    fun challenge_is_not_eligible_while_progress_is_below_target() = runTest(dispatcher) {
        viewModel(challenge = challenge(progressCurrent = 2, progressTarget = 3)).uiState.test {
            testScheduler.advanceUntilIdle()
            assertFalse(expectMostRecentItem().challengeEligible)
        }
    }

    @Test
    fun challenge_is_eligible_once_progress_reaches_target() = runTest(dispatcher) {
        viewModel(challenge = challenge(progressCurrent = 3, progressTarget = 3)).uiState.test {
            testScheduler.advanceUntilIdle()
            assertTrue(expectMostRecentItem().challengeEligible)
        }
    }

    @Test
    fun challenge_done_click_completes_the_challenge_and_refreshes() = runTest(dispatcher) {
        val fakeChallenges = FakeWeeklyChallengeRepository(
            challenge(id = "c1", progressCurrent = 3, progressTarget = 3),
        )
        val viewModel = viewModel(weeklyChallengeRepository = fakeChallenges)

        viewModel.uiState.test {
            testScheduler.advanceUntilIdle()
            assertFalse(expectMostRecentItem().challengeCompleted)

            viewModel.onChallengeDoneClick()
            testScheduler.advanceUntilIdle()

            assertEquals(listOf("c1"), fakeChallenges.completedIds)
            assertTrue(expectMostRecentItem().challengeCompleted)
        }
    }

    private fun viewModel(
        profile: UserProfile = UserProfile(0, 0, 0),
        lastCooked: CookingSession? = null,
        recommendations: List<Recipe> = emptyList(),
        challenge: WeeklyChallenge = challenge(),
        weeklyChallengeRepository: WeeklyChallengeRepository = FakeWeeklyChallengeRepository(challenge),
    ): HomeViewModel {
        val userProfileRepository = FakeUserProfileRepository(profile)
        return HomeViewModel(
            userProfileRepository = userProfileRepository,
            cookingSessionRepository = FakeCookingSessionRepository(lastCooked),
            recipeRepository = FakeRecipeRepository(recommendations),
            weeklyChallengeRepository = weeklyChallengeRepository,
            getChefLevelUseCase = GetChefLevelUseCase(userProfileRepository),
        )
    }

    private fun challenge(
        id: String = "c1",
        title: String = "Three Home-Cooked Meals",
        xpReward: Int = 150,
        progressCurrent: Int = 0,
        progressTarget: Int = 3,
        completedAt: Instant? = null,
    ) = WeeklyChallenge(
        id = id,
        title = title,
        description = "",
        xpReward = xpReward,
        progressCurrent = progressCurrent,
        progressTarget = progressTarget,
        completedAt = completedAt,
    )

    private fun session(rating: Int? = null, cookedAt: Instant) = CookingSession(
        id = "s1",
        recipeId = "r1",
        recipeName = "Tofu stir-fry",
        cookedAt = cookedAt,
        xpEarned = 50,
        rating = rating,
    )
}

private class FakeUserProfileRepository(private val profile: UserProfile) : UserProfileRepository {
    override suspend fun getProfile(): UserProfile = profile
}

private class FakeCookingSessionRepository(private val recent: CookingSession?) : CookingSessionRepository {
    override fun observeAll(): Flow<List<CookingSession>> = emptyFlow()
    override suspend fun recordSession(session: CookingSession) = Unit
    override suspend fun mostRecent(): CookingSession? = recent
    override suspend fun totalXp(): Int = 0
    override suspend fun sessionCount(): Int = 0
    override suspend fun totalDurationMinutes(): Int = 0
    override suspend fun deleteAll() = Unit
}

private class FakeRecipeRepository(private val recommendations: List<Recipe>) : RecipeRepository {
    override suspend fun getRecommendations(): List<Recipe> = recommendations
    override suspend fun getById(id: String): Recipe? = recommendations.find { it.id == id }
}

private class FakeWeeklyChallengeRepository(initial: WeeklyChallenge) : WeeklyChallengeRepository {
    private val current = MutableStateFlow(initial)
    val completedIds = mutableListOf<String>()

    override fun observeCurrent(): Flow<WeeklyChallenge> = current

    override suspend fun complete(id: String) {
        completedIds += id
        val challenge = current.value
        if (challenge.id == id && challenge.progressCurrent >= challenge.progressTarget) {
            current.value = challenge.copy(completedAt = Instant.fromEpochMilliseconds(0))
        }
    }

    override suspend fun totalAwardedXp(): Int = 0

    override suspend fun deleteAll() = Unit
}
