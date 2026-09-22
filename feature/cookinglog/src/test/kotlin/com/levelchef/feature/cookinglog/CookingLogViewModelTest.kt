@file:OptIn(ExperimentalCoroutinesApi::class, ExperimentalTime::class)

package com.levelchef.feature.cookinglog

import app.cash.turbine.test
import com.levelchef.core.model.CookingSession
import com.levelchef.core.model.Difficulty
import com.levelchef.core.model.Recipe
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

class CookingLogViewModelTest {

    private val dispatcher = StandardTestDispatcher()

    private val lemonChicken = Recipe(
        id = "lemon-chicken",
        name = "Lemon chicken breast",
        emoji = "🍗",
        xpReward = 60,
        timeMinutes = 25,
        difficulty = Difficulty.EASY,
    )
    private val steakBowl = Recipe(
        id = "steak-bowl",
        name = "Steak quinoa bowl",
        emoji = "🥩",
        xpReward = 120,
        timeMinutes = 35,
        difficulty = Difficulty.MEDIUM,
    )

    @BeforeTest
    fun setUp() = Dispatchers.setMain(dispatcher)

    @AfterTest
    fun tearDown() = Dispatchers.resetMain()

    private fun viewModel(
        savedIds: List<String> = emptyList(),
        recipes: List<Recipe> = listOf(lemonChicken, steakBowl),
        sessions: List<CookingSession> = emptyList(),
        savedRepo: FakeSavedRecipeRepository = FakeSavedRecipeRepository(savedIds),
        sessionRepo: FakeCookingSessionRepository = FakeCookingSessionRepository(sessions),
    ) = CookingLogViewModel(
        savedRecipeRepository = savedRepo,
        cookingSessionRepository = sessionRepo,
        recipeRepository = FakeRecipeRepository(recipes),
    )

    private fun session(recipeId: String, rating: Int?, cookedAt: String) = CookingSession(
        id = "$recipeId-$cookedAt",
        recipeId = recipeId,
        recipeName = recipeId,
        cookedAt = Instant.parse(cookedAt),
        xpEarned = 10,
        rating = rating,
    )

    @Test
    fun a_never_cooked_saved_recipe_is_uncooked() = runTest(dispatcher) {
        viewModel(savedIds = listOf("lemon-chicken")).uiState.test {
            advanceUntilIdle()
            val item = expectMostRecentItem().allItems.single()
            assertTrue(item is SavedRecipeItem.Uncooked)
            assertEquals("Lemon chicken breast", item.name)
        }
    }

    @Test
    fun a_recipe_with_a_session_is_cooked_with_its_rating() = runTest(dispatcher) {
        val sessions = listOf(session("lemon-chicken", rating = 4, cookedAt = "2026-03-01T00:00:00Z"))
        viewModel(savedIds = listOf("lemon-chicken"), sessions = sessions).uiState.test {
            advanceUntilIdle()
            val item = expectMostRecentItem().allItems.single()
            assertTrue(item is SavedRecipeItem.Cooked)
            assertEquals(4, item.rating)
        }
    }

    @Test
    fun the_most_recent_session_wins_when_a_recipe_was_cooked_more_than_once() = runTest(dispatcher) {
        val sessions = listOf(
            session("lemon-chicken", rating = 2, cookedAt = "2026-01-01T00:00:00Z"),
            session("lemon-chicken", rating = 5, cookedAt = "2026-03-01T00:00:00Z"),
        )
        viewModel(savedIds = listOf("lemon-chicken"), sessions = sessions).uiState.test {
            advanceUntilIdle()
            val item = expectMostRecentItem().allItems.single() as SavedRecipeItem.Cooked
            assertEquals(5, item.rating)
        }
    }

    @Test
    fun a_saved_id_with_no_matching_recipe_is_dropped() = runTest(dispatcher) {
        viewModel(savedIds = listOf("unknown-recipe")).uiState.test {
            advanceUntilIdle()
            assertEquals(emptyList(), expectMostRecentItem().allItems)
        }
    }

    @Test
    fun selecting_a_tab_updates_state() = runTest(dispatcher) {
        val vm = viewModel()
        advanceUntilIdle()

        vm.selectTab(CookingLogTab.COOKED)
        assertEquals(CookingLogTab.COOKED, vm.uiState.value.selectedTab)
    }

    @Test
    fun changing_the_query_updates_state() = runTest(dispatcher) {
        val vm = viewModel()
        advanceUntilIdle()

        vm.changeQuery("lemon")
        assertEquals("lemon", vm.uiState.value.query)
    }

    @Test
    fun deleting_a_recipe_unsaves_it() = runTest(dispatcher) {
        val savedRepo = FakeSavedRecipeRepository(listOf("lemon-chicken"))
        val vm = viewModel(savedRepo = savedRepo)
        advanceUntilIdle()

        vm.deleteRecipe("lemon-chicken")
        advanceUntilIdle()

        assertEquals("lemon-chicken" to false, savedRepo.calls.last())
        assertEquals(emptyList(), vm.uiState.value.allItems)
    }

    @Test
    fun filtering_by_tab_and_query() {
        val uncooked = SavedRecipeItem.Uncooked("a", "Steak bowl", "🥩", 120, 35, Difficulty.MEDIUM)
        val cooked = SavedRecipeItem.Cooked("b", "Lemon chicken", "🍗", 60, 4, CookedAgo.Today)
        val items = listOf(uncooked, cooked)

        assertEquals(items, items.filtered(CookingLogTab.ALL, ""))
        assertEquals(listOf(cooked), items.filtered(CookingLogTab.COOKED, ""))
        assertEquals(listOf(uncooked), items.filtered(CookingLogTab.NEW, ""))
        assertEquals(listOf(cooked), items.filtered(CookingLogTab.ALL, "lemon"))
        assertEquals(emptyList(), items.filtered(CookingLogTab.NEW, "lemon"))
    }
}
