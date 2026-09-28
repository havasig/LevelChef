@file:OptIn(ExperimentalCoroutinesApi::class)

package com.levelchef.feature.ingredients

import app.cash.turbine.test
import com.levelchef.core.model.Ingredient
import com.levelchef.core.model.IngredientCategory
import com.levelchef.domain.usecase.DeleteIngredientUseCase
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

class IngredientDetailViewModelTest {

    private val dispatcher = StandardTestDispatcher()
    private val apple = Ingredient("apple", "Apple", IngredientCategory.FRUIT, "🍎")

    @BeforeTest
    fun setUp() = Dispatchers.setMain(dispatcher)

    @AfterTest
    fun tearDown() = Dispatchers.resetMain()

    private fun viewModel(repository: FakeIngredientRepository) =
        IngredientDetailViewModel("apple", repository, DeleteIngredientUseCase(repository))

    @Test
    fun exposes_the_matching_ingredient() = runTest(dispatcher) {
        viewModel(FakeIngredientRepository(listOf(apple))).uiState.test {
            advanceUntilIdle()
            val state = expectMostRecentItem()
            assertEquals(apple, state.ingredient)
        }
    }

    @Test
    fun delete_removes_the_ingredient_and_flags_deleted() = runTest(dispatcher) {
        val repository = FakeIngredientRepository(listOf(apple))
        val vm = viewModel(repository)
        advanceUntilIdle()

        vm.delete()
        advanceUntilIdle()

        assertEquals(listOf("apple"), repository.deleted)
        assertTrue(vm.uiState.value.deleted)
    }
}
