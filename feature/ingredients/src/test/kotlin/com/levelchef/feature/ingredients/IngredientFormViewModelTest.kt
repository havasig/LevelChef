@file:OptIn(ExperimentalCoroutinesApi::class)

package com.levelchef.feature.ingredients

import com.levelchef.core.model.Ingredient
import com.levelchef.core.model.IngredientCategory
import com.levelchef.core.model.IngredientMacros
import com.levelchef.core.model.MeasurementUnit
import com.levelchef.domain.usecase.SaveIngredientUseCase
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
import kotlin.test.assertNull
import kotlin.test.assertTrue

class IngredientFormViewModelTest {

    private val dispatcher = StandardTestDispatcher()

    @BeforeTest
    fun setUp() = Dispatchers.setMain(dispatcher)

    @AfterTest
    fun tearDown() = Dispatchers.resetMain()

    private fun viewModel(id: String?, repository: FakeIngredientRepository) =
        IngredientFormViewModel(id, repository, SaveIngredientUseCase(repository, newId = { "new-id" }))

    @Test
    fun add_mode_starts_blank_and_cannot_save_without_a_name() = runTest(dispatcher) {
        val vm = viewModel(null, FakeIngredientRepository())
        advanceUntilIdle()

        assertFalse(vm.uiState.value.editing)
        assertFalse(vm.uiState.value.canSave)

        vm.setName("Tofu")
        assertTrue(vm.uiState.value.canSave)
    }

    @Test
    fun add_saves_a_new_ingredient_with_a_category_emoji_and_no_macros() = runTest(dispatcher) {
        val repository = FakeIngredientRepository()
        val vm = viewModel(null, repository)
        advanceUntilIdle()

        vm.setName("  Tofu  ")
        vm.setCategory(IngredientCategory.OTHER)
        vm.setUnit(MeasurementUnit.GRAM)
        vm.save()
        advanceUntilIdle()

        val saved = repository.stored.single()
        assertEquals("new-id", saved.id)
        assertEquals("Tofu", saved.name)
        assertEquals(IngredientCategory.OTHER, saved.category)
        assertEquals(MeasurementUnit.GRAM, saved.defaultUnit)
        assertEquals("🥕", saved.emoji)
        assertNull(saved.macros)
        assertTrue(vm.uiState.value.saved)
    }

    @Test
    fun partial_macros_default_the_rest_to_zero() = runTest(dispatcher) {
        val repository = FakeIngredientRepository()
        val vm = viewModel(null, repository)
        advanceUntilIdle()

        vm.setName("Egg")
        vm.setProtein("13")
        vm.save()
        advanceUntilIdle()

        assertEquals(IngredientMacros(0, 13.0, 0.0, 0.0), repository.stored.single().macros)
    }

    @Test
    fun edit_mode_pre_fills_fields_and_keeps_id_and_image() = runTest(dispatcher) {
        val existing = Ingredient(
            id = "apple",
            name = "Apple",
            category = IngredientCategory.FRUIT,
            emoji = "🍎",
            defaultUnit = MeasurementUnit.PIECE,
            macros = IngredientMacros(52, 0.3, 14.0, 0.2),
            imageUrl = "https://img/apple.png",
        )
        val repository = FakeIngredientRepository(listOf(existing))
        val vm = viewModel("apple", repository)
        advanceUntilIdle()

        assertTrue(vm.uiState.value.editing)
        assertEquals("Apple", vm.uiState.value.name)
        assertEquals("14", vm.uiState.value.carbs)

        vm.setName("Green apple")
        vm.save()
        advanceUntilIdle()

        val saved = repository.stored.single()
        assertEquals("apple", saved.id)
        assertEquals("Green apple", saved.name)
        assertEquals("🍎", saved.emoji)
        assertEquals("https://img/apple.png", saved.imageUrl)
    }

    @Test
    fun numeric_setters_strip_letters_and_extra_dots() = runTest(dispatcher) {
        val vm = viewModel(null, FakeIngredientRepository())
        advanceUntilIdle()

        vm.setCalories("1a2b3")
        vm.setFat("1.2.3")

        assertEquals("123", vm.uiState.value.calories)
        assertEquals("1.23", vm.uiState.value.fat)
    }
}
