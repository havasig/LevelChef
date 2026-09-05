@file:OptIn(ExperimentalTime::class)

package com.levelchef.data.repository

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToOneOrNull
import com.levelchef.core.database.db.LevelChefDatabase
import com.levelchef.core.model.Allergen
import com.levelchef.core.model.CookingExperience
import com.levelchef.core.model.CookingGoal
import com.levelchef.core.model.Cuisine
import com.levelchef.core.model.DietaryPreference
import com.levelchef.core.model.HouseholdSize
import com.levelchef.core.model.SpiceTolerance
import com.levelchef.core.model.SurveyResponse
import com.levelchef.core.model.WeeknightTime
import com.levelchef.domain.repository.SurveyRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

/** SQLDelight-backed [SurveyRepository]. Single-row table; enum sets are comma-joined strings. */
class SurveyResponseRepositoryImpl(
    private val database: LevelChefDatabase,
) : SurveyRepository {

    override fun observeResponse(): Flow<SurveyResponse?> =
        database.surveyResponseQueries.selectResponse()
            .asFlow()
            .mapToOneOrNull(Dispatchers.Default)
            .map { row -> row?.toDomain() }

    override suspend fun save(response: SurveyResponse) {
        database.surveyResponseQueries.upsertResponse(
            completedAt = response.completedAt.toString(),
            cookingExperience = response.cookingExperience.name,
            dietaryPreference = response.dietaryPreference.name,
            allergens = response.allergens.joinToString(SEPARATOR) { it.name },
            cuisines = response.cuisines.joinToString(SEPARATOR) { it.name },
            spiceTolerance = response.spiceTolerance.name,
            cookingGoal = response.cookingGoal.name,
            weeknightTime = response.weeknightTime.name,
            householdSize = response.householdSize.name,
        )
    }
}

private const val SEPARATOR = ","

private inline fun <reified T : Enum<T>> String.toEnumSet(): Set<T> =
    if (isEmpty()) emptySet() else split(SEPARATOR).map { enumValueOf<T>(it) }.toSet()

private fun com.levelchef.core.database.db.SurveyResponse.toDomain() = SurveyResponse(
    completedAt = Instant.parse(completedAt),
    cookingExperience = enumValueOf<CookingExperience>(cookingExperience),
    dietaryPreference = enumValueOf<DietaryPreference>(dietaryPreference),
    allergens = allergens.toEnumSet<Allergen>(),
    cuisines = cuisines.toEnumSet<Cuisine>(),
    spiceTolerance = enumValueOf<SpiceTolerance>(spiceTolerance),
    cookingGoal = enumValueOf<CookingGoal>(cookingGoal),
    weeknightTime = enumValueOf<WeeknightTime>(weeknightTime),
    householdSize = enumValueOf<HouseholdSize>(householdSize),
)
