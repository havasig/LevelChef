package com.levelchef.domain.repository

import com.levelchef.core.model.Recipe

/** Source of recipe recommendations and lookups. */
interface RecipeRepository {
    suspend fun getRecommendations(): List<Recipe>
    suspend fun getById(id: String): Recipe?
}
