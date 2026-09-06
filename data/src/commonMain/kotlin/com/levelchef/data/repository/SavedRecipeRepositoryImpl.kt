@file:OptIn(ExperimentalTime::class)

package com.levelchef.data.repository

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToOne
import com.levelchef.core.database.db.LevelChefDatabase
import com.levelchef.domain.repository.SavedRecipeRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

/** SQLDelight-backed [SavedRecipeRepository]. `savedAt` stores the ISO-8601 instant of the save. */
class SavedRecipeRepositoryImpl(
    private val database: LevelChefDatabase,
    private val clock: Clock = Clock.System,
) : SavedRecipeRepository {

    private val queries get() = database.savedRecipeQueries

    override fun observeIsSaved(recipeId: String): Flow<Boolean> =
        queries.isSaved(recipeId).asFlow().mapToOne(Dispatchers.Default)

    override suspend fun setSaved(recipeId: String, saved: Boolean) {
        if (saved) queries.save(recipeId, clock.now().toString()) else queries.unsave(recipeId)
    }
}
