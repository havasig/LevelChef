package com.levelchef.domain.usecase

import com.levelchef.core.model.Ingredient
import com.levelchef.core.model.IngredientCategory
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class DeleteIngredientUseCaseTest {

    @Test
    fun removes_the_ingredient_from_the_repository() = runTest {
        val repository = FakeIngredientRepository(
            listOf(Ingredient("apple", "Apple", IngredientCategory.FRUIT, "🍎")),
        )

        DeleteIngredientUseCase(repository)("apple")

        assertEquals(listOf("apple"), repository.deleted)
        assertEquals(emptyList(), repository.stored)
    }
}
