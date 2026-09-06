package com.levelchef.domain.usecase

import com.levelchef.core.model.Ingredient
import com.levelchef.domain.repository.IngredientRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

/** Hand-written fake following the `Fake…Repository` pattern. */
internal class FakeIngredientRepository(initial: List<Ingredient> = emptyList()) : IngredientRepository {

    private val state = MutableStateFlow(initial)
    val deleted = mutableListOf<String>()
    var seededCount = 0
        private set

    val stored: List<Ingredient> get() = state.value

    override fun observeAll(): Flow<List<Ingredient>> = state

    override suspend fun getById(id: String): Ingredient? = state.value.firstOrNull { it.id == id }

    override suspend fun save(ingredient: Ingredient) {
        state.value = state.value.filterNot { it.id == ingredient.id } + ingredient
    }

    override suspend fun delete(id: String) {
        deleted += id
        state.value = state.value.filterNot { it.id == id }
    }

    override suspend fun count(): Int = state.value.size

    override suspend fun seedDefaults() {
        seededCount++
    }
}
