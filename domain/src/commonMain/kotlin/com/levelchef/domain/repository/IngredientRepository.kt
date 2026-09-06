package com.levelchef.domain.repository

import com.levelchef.core.model.Ingredient
import kotlinx.coroutines.flow.Flow

/** Persistence boundary for the user's pantry of logged ingredients. */
interface IngredientRepository {

    /** Emits the full pantry, ordered by name, re-emitting on every change. */
    fun observeAll(): Flow<List<Ingredient>>

    suspend fun getById(id: String): Ingredient?

    /** Inserts or replaces [ingredient] by its id. */
    suspend fun save(ingredient: Ingredient)

    suspend fun delete(id: String)

    /** Wipes the entire pantry — backs account deletion. */
    suspend fun deleteAll()

    suspend fun count(): Int

    /** Populates the default ingredient set — a no-op unless the pantry is empty. */
    suspend fun seedDefaults()
}
