package com.levelchef.feature.cookinglog

import com.levelchef.core.model.CookingSession
import com.levelchef.core.model.Recipe
import com.levelchef.domain.repository.CookingSessionRepository
import com.levelchef.domain.repository.RecipeRepository
import com.levelchef.domain.repository.SavedRecipeRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map

internal class FakeRecipeRepository(private val recipes: List<Recipe>) : RecipeRepository {
    override suspend fun getRecommendations(): List<Recipe> = recipes
    override suspend fun getById(id: String): Recipe? = recipes.firstOrNull { it.id == id }
}

internal class FakeSavedRecipeRepository(initialIds: List<String> = emptyList()) : SavedRecipeRepository {
    private val savedIds = MutableStateFlow(initialIds)
    val calls = mutableListOf<Pair<String, Boolean>>()

    override fun observeIsSaved(recipeId: String): Flow<Boolean> = savedIds.map { recipeId in it }

    override fun observeSavedRecipeIds(): Flow<List<String>> = savedIds.asStateFlow()

    override suspend fun setSaved(recipeId: String, saved: Boolean) {
        calls += recipeId to saved
        savedIds.value = if (saved) listOf(recipeId) + savedIds.value else savedIds.value - recipeId
    }

    override suspend fun deleteAll() {
        savedIds.value = emptyList()
    }
}

internal class FakeCookingSessionRepository(initial: List<CookingSession> = emptyList()) : CookingSessionRepository {
    private val sessions = MutableStateFlow(initial)

    override fun observeAll(): Flow<List<CookingSession>> = sessions.asStateFlow()
    override suspend fun recordSession(session: CookingSession) {
        sessions.value += session
    }
    override suspend fun mostRecent(): CookingSession? = sessions.value.lastOrNull()
    override suspend fun totalXp(): Int = sessions.value.sumOf { it.xpEarned }
    override suspend fun sessionCount(): Int = sessions.value.size
    override suspend fun totalDurationMinutes(): Int = sessions.value.sumOf { it.durationMinutes }
    override suspend fun deleteAll() {
        sessions.value = emptyList()
    }
}
