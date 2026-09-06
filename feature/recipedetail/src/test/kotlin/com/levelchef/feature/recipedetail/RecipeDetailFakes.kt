package com.levelchef.feature.recipedetail

import com.levelchef.core.model.CookingSession
import com.levelchef.core.model.Recipe
import com.levelchef.domain.repository.CookingSessionRepository
import com.levelchef.domain.repository.RecipeRepository
import com.levelchef.domain.repository.SavedRecipeRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map

internal class FakeRecipeRepository(private val recipes: List<Recipe>) : RecipeRepository {
    override suspend fun getRecommendations(): List<Recipe> = recipes
    override suspend fun getById(id: String): Recipe? = recipes.firstOrNull { it.id == id }
}

internal class FakeSavedRecipeRepository(initial: Set<String> = emptySet()) : SavedRecipeRepository {
    private val saved = MutableStateFlow(initial)
    val calls = mutableListOf<Pair<String, Boolean>>()

    override fun observeIsSaved(recipeId: String): Flow<Boolean> = saved.map { recipeId in it }

    override suspend fun setSaved(recipeId: String, saved: Boolean) {
        calls += recipeId to saved
        this.saved.value = if (saved) this.saved.value + recipeId else this.saved.value - recipeId
    }
}

internal class RecordingCookingSessionRepository : CookingSessionRepository {
    val recorded = mutableListOf<CookingSession>()
    override fun observeAll(): Flow<List<CookingSession>> = flowOf(recorded)
    override suspend fun recordSession(session: CookingSession) {
        recorded += session
    }
    override suspend fun mostRecent(): CookingSession? = recorded.lastOrNull()
    override suspend fun totalXp(): Int = recorded.sumOf { it.xpEarned }
    override suspend fun sessionCount(): Int = recorded.size
    override suspend fun totalDurationMinutes(): Int = recorded.sumOf { it.durationMinutes }
    override suspend fun deleteAll() {
        recorded.clear()
    }
}
