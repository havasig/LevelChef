@file:OptIn(ExperimentalUuidApi::class)

package com.levelchef.domain.usecase

import com.levelchef.core.model.Ingredient
import com.levelchef.domain.repository.IngredientRepository
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

/** Generates a random ingredient id. */
fun randomIngredientId(): String = Uuid.random().toString()

/**
 * Persists an ingredient. A blank [Ingredient.id] means "new" and gets a generated id; a set id is
 * an edit and is kept. Returns the stored ingredient (with its final id).
 */
class SaveIngredientUseCase(
    private val ingredientRepository: IngredientRepository,
    private val newId: () -> String = ::randomIngredientId,
) {
    suspend operator fun invoke(ingredient: Ingredient): Ingredient {
        val stored = if (ingredient.id.isBlank()) ingredient.copy(id = newId()) else ingredient
        ingredientRepository.save(stored)
        return stored
    }
}
