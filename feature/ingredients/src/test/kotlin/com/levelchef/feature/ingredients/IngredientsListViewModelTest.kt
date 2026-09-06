@file:OptIn(ExperimentalCoroutinesApi::class)

package com.levelchef.feature.ingredients

import app.cash.turbine.test
import com.levelchef.core.model.Ingredient
import com.levelchef.core.model.IngredientCategory
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

private fun ingredient(id: String, category: IngredientCategory) =
    Ingredient(id, id, category, "🥕")

class IngredientsListViewModelTest {

    private val dispatcher = StandardTestDispatcher()

    @BeforeTest
    fun setUp() = Dispatchers.setMain(dispatcher)

    @AfterTest
    fun tearDown() = Dispatchers.resetMain()

    @Test
    fun groups_ingredients_by_category_in_enum_order_skipping_empty_categories() = runTest(dispatcher) {
        val repository = FakeIngredientRepository(
            listOf(
                ingredient("apple", IngredientCategory.FRUIT),
                ingredient("beef", IngredientCategory.MEAT),
                ingredient("pear", IngredientCategory.FRUIT),
            ),
        )
        val vm = IngredientsListViewModel(repository)

        vm.uiState.test {
            advanceUntilIdle()
            val state = expectMostRecentItem()
            assertFalse(state.loading)
            assertEquals(
                listOf(IngredientCategory.MEAT, IngredientCategory.FRUIT),
                state.sections.map { it.category },
            )
            assertEquals(listOf("apple", "pear"), state.sections.last().ingredients.map { it.id })
        }
    }

    @Test
    fun is_empty_when_nothing_is_stored() = runTest(dispatcher) {
        val vm = IngredientsListViewModel(FakeIngredientRepository())

        vm.uiState.test {
            advanceUntilIdle()
            assertTrue(expectMostRecentItem().isEmpty)
        }
    }
}
