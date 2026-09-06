package com.levelchef.domain.usecase

import com.levelchef.core.model.Ingredient
import com.levelchef.core.model.IngredientCategory
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class SaveIngredientUseCaseTest {

    private fun ingredient(id: String) = Ingredient(id, "Carrot", IngredientCategory.VEGETABLE, "🥕")

    @Test
    fun assigns_a_generated_id_when_the_id_is_blank() = runTest {
        val repository = FakeIngredientRepository()
        val useCase = SaveIngredientUseCase(repository, newId = { "generated-1" })

        val saved = useCase(ingredient(id = ""))

        assertEquals("generated-1", saved.id)
        assertEquals(listOf("generated-1"), repository.stored.map { it.id })
    }

    @Test
    fun keeps_the_existing_id_when_editing() = runTest {
        val repository = FakeIngredientRepository(listOf(ingredient("carrot")))
        val useCase = SaveIngredientUseCase(repository, newId = { "should-not-be-used" })

        val saved = useCase(ingredient("carrot").copy(name = "Baby carrot"))

        assertEquals("carrot", saved.id)
        assertEquals("Baby carrot", repository.stored.single().name)
    }
}
