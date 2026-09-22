package com.levelchef.feature.recipedetail

import com.levelchef.core.model.Recipe
import com.levelchef.domain.repository.RecipeRepository
import com.levelchef.domain.repository.SavedRecipeRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

internal class FakeRecipeRepository(private val recipes: List<Recipe>) : RecipeRepository {
    override suspend fun getRecommendations(): List<Recipe> = recipes
    override suspend fun getById(id: String): Recipe? = recipes.firstOrNull { it.id == id }
}

internal class FakeSavedRecipeRepository(initial: Set<String> = emptySet()) : SavedRecipeRepository {
    private val saved = MutableStateFlow(initial)
    val calls = mutableListOf<Pair<String, Boolean>>()

    override fun observeIsSaved(recipeId: String): Flow<Boolean> = saved.map { recipeId in it }

    override fun observeSavedRecipeIds(): Flow<List<String>> = saved.map { it.toList() }

    override suspend fun setSaved(recipeId: String, saved: Boolean) {
        calls += recipeId to saved
        this.saved.value = if (saved) this.saved.value + recipeId else this.saved.value - recipeId
    }
}
