package com.levelchef.data.repository

import com.levelchef.core.model.Ingredient
import com.levelchef.domain.repository.IngredientRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

/** Hand-written fake following the `Fake…Repository` pattern. */
internal class FakeIngredientRepository(private val ingredients: List<Ingredient> = emptyList()) : IngredientRepository {
    override fun observeAll(): Flow<List<Ingredient>> = flowOf(ingredients)
    override suspend fun getById(id: String): Ingredient? = ingredients.firstOrNull { it.id == id }
    override suspend fun save(ingredient: Ingredient) = Unit
    override suspend fun delete(id: String) = Unit
    override suspend fun count(): Int = ingredients.size
    override suspend fun seedDefaults() = Unit
}
