@file:OptIn(ExperimentalCoroutinesApi::class)

package com.levelchef.feature.mealreview

import app.cash.turbine.test
import com.levelchef.core.model.Difficulty
import com.levelchef.core.model.Recipe
import com.levelchef.core.model.RecipeIngredient
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

class MealReviewViewModelTest {

    private val dispatcher = StandardTestDispatcher()

    private val recipe = Recipe(
        id = "lemon-chicken",
        name = "Lemon chicken breast",
        emoji = "🍗",
        xpReward = 60,
        timeMinutes = 25,
        difficulty = Difficulty.EASY,
        caloriesKcal = 320,
        proteinGrams = 38,
        carbsGrams = 8,
        fatGrams = 12,
        ingredients = listOf(
            RecipeIngredient(name = "chicken breast", quantity = 500.0, unit = "g"),
            RecipeIngredient(name = "lemons", quantity = 2.0),
        ),
    )

    @BeforeTest
    fun setUp() = Dispatchers.setMain(dispatcher)

    @AfterTest
    fun tearDown() = Dispatchers.resetMain()

    private fun viewModel(
        recipes: List<Recipe> = listOf(recipe),
        sessions: RecordingCookingSessionRepository = RecordingCookingSessionRepository(),
    ) = MealReviewViewModel(
        recipeId = "lemon-chicken",
        recipeRepository = FakeRecipeRepository(recipes),
        recordCookingSession = RecordCookingSessionUseCase(sessions) { "s" },
    )

    @Test
    fun loads_the_recipe_and_prefills_macros_name_and_xp() = runTest(dispatcher) {
        viewModel().uiState.test {
            advanceUntilIdle()
            val state = expectMostRecentItem()
            assertEquals("Lemon chicken breast", state.recipeName)
            assertEquals(60, state.xpReward)
            assertEquals(320, state.caloriesKcal)
            assertEquals(38, state.proteinGrams)
            assertEquals(8, state.carbsGrams)
            assertEquals(12, state.fatGrams)
            assertEquals(listOf("500 g chicken breast", "2 lemons"), state.ingredientLines)
            assertFalse(state.loading)
        }
    }

    @Test
    fun an_unknown_recipe_leaves_blank_defaults_and_stops_loading() = runTest(dispatcher) {
        viewModel(recipes = emptyList()).uiState.test {
            advanceUntilIdle()
            val state = expectMostRecentItem()
            assertEquals("", state.recipeName)
            assertEquals(0, state.xpReward)
            assertFalse(state.loading)
        }
    }

    @Test
    fun changing_rating_clamps_between_zero_and_five() = runTest(dispatcher) {
        val vm = viewModel()
        advanceUntilIdle()

        vm.changeRating(-2)
        assertEquals(0, vm.uiState.value.rating)

        vm.changeRating(9)
        assertEquals(MealReviewUiState.MAX_RATING, vm.uiState.value.rating)
    }

    @Test
    fun steppers_clamp_at_zero() = runTest(dispatcher) {
        val vm = viewModel()
        advanceUntilIdle()

        vm.changeDuration(-100)
        assertEquals(0, vm.uiState.value.durationMinutes)

        vm.changeCalories(-1000)
        assertEquals(0, vm.uiState.value.caloriesKcal)

        vm.changeProtein(-1000)
        assertEquals(0, vm.uiState.value.proteinGrams)

        vm.changeCarbs(-1000)
        assertEquals(0, vm.uiState.value.carbsGrams)

        vm.changeFat(-1000)
        assertEquals(0, vm.uiState.value.fatGrams)
    }

    @Test
    fun saving_records_the_full_session_and_flips_saved() = runTest(dispatcher) {
        val sessions = RecordingCookingSessionRepository()
        val vm = viewModel(sessions = sessions)
        advanceUntilIdle()

        vm.changeRating(4)
        vm.changeNote("Needed more lemon")
        vm.changeDuration(5)
        vm.save()
        advanceUntilIdle()

        val session = sessions.recorded.single()
        assertEquals("lemon-chicken", session.recipeId)
        assertEquals(4, session.rating)
        assertEquals("Needed more lemon", session.improvementNote)
        assertEquals(5, session.durationMinutes)
        assertEquals(320, session.kcal)
        assertTrue(vm.uiState.value.saved)
    }

    @Test
    fun saving_before_the_recipe_loads_is_a_no_op() = runTest(dispatcher) {
        val sessions = RecordingCookingSessionRepository()
        val vm = viewModel(recipes = emptyList(), sessions = sessions)

        vm.save()
        advanceUntilIdle()

        assertTrue(sessions.recorded.isEmpty())
        assertFalse(vm.uiState.value.saved)
    }
}
