package com.levelchef.feature.home

import app.cash.turbine.test
import com.levelchef.core.model.CookingSession
import com.levelchef.core.model.Difficulty
import com.levelchef.core.model.Recipe
import com.levelchef.core.model.UserProfile
import com.levelchef.domain.repository.CookingSessionRepository
import com.levelchef.domain.repository.RecipeRepository
import com.levelchef.domain.repository.UserProfileRepository
import com.levelchef.domain.usecase.GetChefLevelUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
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

            assertEquals("Wok Warrior", loaded.levelLabel)
            assertEquals(800, loaded.currentXp)
            assertEquals(12, loaded.cookingSessions)
            assertEquals(5, loaded.ingredientsTried)
            assertEquals("Miso soup", loaded.recommendations.single().name)
            assertEquals("Easy", loaded.recommendations.single().difficulty)
            assertEquals("3 days ago", loaded.lastCooked?.whenText)
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
    fun relative_day_text_reads_today_for_a_session_cooked_now() = runTest(dispatcher) {
        viewModel(lastCooked = session(cookedAt = Clock.System.now())).uiState.test {
            testScheduler.advanceUntilIdle()
            assertEquals("today", expectMostRecentItem().lastCooked?.whenText)
        }
    }

    @Test
    fun relative_day_text_reads_one_day_ago() = runTest(dispatcher) {
        viewModel(lastCooked = session(cookedAt = Clock.System.now() - 1.days)).uiState.test {
            testScheduler.advanceUntilIdle()
            assertEquals("1 day ago", expectMostRecentItem().lastCooked?.whenText)
        }
    }

    private fun viewModel(
        profile: UserProfile = UserProfile(0, 0, 0),
        lastCooked: CookingSession? = null,
        recommendations: List<Recipe> = emptyList(),
    ): HomeViewModel {
        val userProfileRepository = FakeUserProfileRepository(profile)
        return HomeViewModel(
            userProfileRepository = userProfileRepository,
            cookingSessionRepository = FakeCookingSessionRepository(lastCooked),
            recipeRepository = FakeRecipeRepository(recommendations),
            getChefLevelUseCase = GetChefLevelUseCase(userProfileRepository),
        )
    }

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
}

private class FakeRecipeRepository(private val recommendations: List<Recipe>) : RecipeRepository {
    override suspend fun getRecommendations(): List<Recipe> = recommendations
    override suspend fun getById(id: String): Recipe? = recommendations.find { it.id == id }
}
