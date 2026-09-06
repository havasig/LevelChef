@file:OptIn(ExperimentalCoroutinesApi::class)

package com.levelchef.feature.recipedetail

import app.cash.turbine.test
import com.levelchef.core.model.Difficulty
import com.levelchef.core.model.Recipe
import com.levelchef.domain.usecase.RecordCookingSessionUseCase
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
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class RecipeDetailViewModelTest {

    private val dispatcher = StandardTestDispatcher()

    private val recipe = Recipe(
        id = "lemon-chicken",
        name = "Lemon chicken breast",
        emoji = "🍗",
        xpReward = 60,
        timeMinutes = 25,
        difficulty = Difficulty.EASY,
        servings = 3,
    )

    @BeforeTest
    fun setUp() = Dispatchers.setMain(dispatcher)

    @AfterTest
    fun tearDown() = Dispatchers.resetMain()

    private fun viewModel(
        recipes: List<Recipe> = listOf(recipe),
        saved: Set<String> = emptySet(),
        sessions: RecordingCookingSessionRepository = RecordingCookingSessionRepository(),
        savedRepo: FakeSavedRecipeRepository = FakeSavedRecipeRepository(saved),
    ) = RecipeDetailViewModel(
        recipeId = "lemon-chicken",
        recipeRepository = FakeRecipeRepository(recipes),
        savedRecipeRepository = savedRepo,
        recordCookingSession = RecordCookingSessionUseCase(sessions) { "s" },
    )

    @Test
    fun loads_the_recipe_and_adopts_its_serving_count() = runTest(dispatcher) {
        viewModel().uiState.test {
            advanceUntilIdle()
            val state = expectMostRecentItem()
            assertEquals("Lemon chicken breast", state.recipe?.name)
            assertEquals(3, state.servings)
            assertFalse(state.loading)
        }
    }

    @Test
    fun an_unknown_recipe_leaves_a_null_recipe_and_stops_loading() = runTest(dispatcher) {
        viewModel(recipes = emptyList()).uiState.test {
            advanceUntilIdle()
            val state = expectMostRecentItem()
            assertEquals(null, state.recipe)
            assertFalse(state.loading)
        }
    }

    @Test
    fun changing_servings_clamps_between_the_min_and_max() = runTest(dispatcher) {
        val vm = viewModel()
        advanceUntilIdle()

        repeat(3) { vm.changeServings(-1) }
        assertEquals(RecipeDetailUiState.MIN_SERVINGS, vm.uiState.value.servings)

        repeat(20) { vm.changeServings(1) }
        assertEquals(RecipeDetailUiState.MAX_SERVINGS, vm.uiState.value.servings)
    }

    @Test
    fun toggling_an_ingredient_adds_then_removes_its_index() = runTest(dispatcher) {
        val vm = viewModel()
        advanceUntilIdle()

        vm.toggleIngredient(1)
        assertEquals(setOf(1), vm.uiState.value.checkedIngredients)

        vm.toggleIngredient(1)
        assertEquals(emptySet(), vm.uiState.value.checkedIngredients)
    }

    @Test
    fun toggling_save_persists_the_new_state_and_flashes_a_message() = runTest(dispatcher) {
        val savedRepo = FakeSavedRecipeRepository()
        val vm = viewModel(savedRepo = savedRepo)
        advanceUntilIdle()

        vm.toggleSaved()
        advanceUntilIdle()
        assertTrue(vm.uiState.value.isSaved)
        assertEquals("lemon-chicken" to true, savedRepo.calls.last())
        assertEquals(TransientMessage.SAVED, vm.uiState.value.transientMessage)

        vm.dismissMessage()
        vm.toggleSaved()
        advanceUntilIdle()
        assertFalse(vm.uiState.value.isSaved)
        assertEquals(TransientMessage.UNSAVED, vm.uiState.value.transientMessage)
    }

    @Test
    fun marking_cooked_records_a_session_and_flashes_a_message() = runTest(dispatcher) {
        val sessions = RecordingCookingSessionRepository()
        val vm = viewModel(sessions = sessions)
        advanceUntilIdle()

        vm.markCooked()
        advanceUntilIdle()

        assertEquals("lemon-chicken", sessions.recorded.single().recipeId)
        assertEquals(60, sessions.recorded.single().xpEarned)
        assertEquals(TransientMessage.COOKED, vm.uiState.value.transientMessage)
    }

    @Test
    fun marking_cooked_before_the_recipe_loads_is_a_no_op() = runTest(dispatcher) {
        val sessions = RecordingCookingSessionRepository()
        val vm = viewModel(recipes = emptyList(), sessions = sessions)

        vm.markCooked()
        advanceUntilIdle()

        assertTrue(sessions.recorded.isEmpty())
        assertEquals(null, vm.uiState.value.transientMessage)
    }

    @Test
    fun timer_stub_and_dismiss_drive_the_transient_message() = runTest(dispatcher) {
        val vm = viewModel()
        advanceUntilIdle()

        vm.showTimerStub()
        assertEquals(TransientMessage.TIMER_STUB, vm.uiState.value.transientMessage)

        vm.dismissMessage()
        assertEquals(null, vm.uiState.value.transientMessage)
    }

    @Test
    fun an_already_saved_recipe_reports_saved_on_load() = runTest(dispatcher) {
        viewModel(saved = setOf("lemon-chicken")).uiState.test {
            advanceUntilIdle()
            assertTrue(expectMostRecentItem().isSaved)
        }
    }
}
