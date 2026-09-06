package com.levelchef.data.repository

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import com.levelchef.core.database.db.LevelChefDatabase
import com.levelchef.core.model.CookingSession
import com.levelchef.domain.repository.CookingSessionRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.datetime.Instant

/** SQLDelight-backed [CookingSessionRepository]. */
class CookingSessionRepositoryImpl(
    private val database: LevelChefDatabase,
) : CookingSessionRepository {

    override fun observeAll(): Flow<List<CookingSession>> =
        database.cookingSessionQueries.selectAll()
            .asFlow()
            .mapToList(Dispatchers.Default)
            .map { rows -> rows.map { it.toDomain() } }

    override suspend fun recordSession(session: CookingSession) {
        database.cookingSessionQueries.insertSession(
            id = session.id,
            recipeId = session.recipeId,
            recipeName = session.recipeName,
            cookedAt = session.cookedAt.toString(),
            xpEarned = session.xpEarned.toLong(),
            durationMinutes = session.durationMinutes.toLong(),
            rating = session.rating?.toLong(),
            improvementNote = session.improvementNote,
            kcal = session.kcal?.toLong(),
            proteinGrams = session.proteinGrams?.toLong(),
            carbsGrams = session.carbsGrams?.toLong(),
            fatGrams = session.fatGrams?.toLong(),
        )
    }

    override suspend fun mostRecent(): CookingSession? =
        database.cookingSessionQueries.selectRecent().executeAsOneOrNull()?.toDomain()

    override suspend fun totalXp(): Int =
        database.cookingSessionQueries.totalXp().executeAsOne().toInt()

    override suspend fun sessionCount(): Int =
        database.cookingSessionQueries.sessionCount().executeAsOne().toInt()

    override suspend fun totalDurationMinutes(): Int =
        database.cookingSessionQueries.totalDurationMinutes().executeAsOne().toInt()

    override suspend fun deleteAll() {
        database.cookingSessionQueries.deleteAll()
    }
}

private fun com.levelchef.core.database.db.CookingSession.toDomain() = CookingSession(
    id = id,
    recipeId = recipeId,
    recipeName = recipeName,
    cookedAt = Instant.parse(cookedAt),
    xpEarned = xpEarned.toInt(),
    durationMinutes = durationMinutes.toInt(),
    rating = rating?.toInt(),
    improvementNote = improvementNote,
    kcal = kcal?.toInt(),
    proteinGrams = proteinGrams?.toInt(),
    carbsGrams = carbsGrams?.toInt(),
    fatGrams = fatGrams?.toInt(),
)
