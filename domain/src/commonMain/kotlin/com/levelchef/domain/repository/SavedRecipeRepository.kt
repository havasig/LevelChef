package com.levelchef.domain.repository

import kotlinx.coroutines.flow.Flow

/** Persistence boundary for recipes the user has bookmarked ("Save") from the recipe detail screen. */
interface SavedRecipeRepository {

    /** Emits whether [recipeId] is currently saved, re-emitting on every change. */
    fun observeIsSaved(recipeId: String): Flow<Boolean>

    /** Emits every currently-saved recipe id, most recently saved first. */
    fun observeSavedRecipeIds(): Flow<List<String>>

    suspend fun setSaved(recipeId: String, saved: Boolean)
}
