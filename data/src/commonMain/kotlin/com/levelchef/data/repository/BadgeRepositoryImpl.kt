@file:OptIn(ExperimentalTime::class)

package com.levelchef.data.repository

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import com.levelchef.core.database.db.LevelChefDatabase
import com.levelchef.core.model.Badge
import com.levelchef.core.model.BadgeCategory
import com.levelchef.core.model.CookingSession
import com.levelchef.core.model.Ingredient
import com.levelchef.core.model.IngredientCategory
import com.levelchef.domain.repository.BadgeRepository
import com.levelchef.domain.repository.CookingSessionRepository
import com.levelchef.domain.repository.IngredientRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

/** What every badge's progress is derived from. */
private data class BadgeSnapshot(val sessions: List<CookingSession>, val ingredients: List<Ingredient>)

/** One catalog entry: how a badge's progress is read off a [BadgeSnapshot], capped at [target]. */
private class BadgeDefinition(
    val id: String,
    val name: String,
    val emoji: String,
    val category: BadgeCategory,
    val description: String,
    val target: Int,
    val progress: (BadgeSnapshot) -> Int,
)

private fun Instant.hourUtc(): Int = toLocalDateTime(TimeZone.UTC).hour

/**
 * Static catalog of 12 badges spanning [BadgeCategory.QUANTITY] (cooking/XP milestones),
 * [BadgeCategory.DISCOVERY] (pantry variety) and [BadgeCategory.ACHIEVEMENT] (cooking habits),
 * evaluated live against cooking-session and pantry data — see [BadgeRepositoryImpl].
 */
private val CATALOG = listOf(
    BadgeDefinition(
        "first-bite", "First Bite", "🍽️", BadgeCategory.QUANTITY,
        "Log your very first cooking session.", target = 1,
    ) { it.sessions.size },
    BadgeDefinition(
        "ten-meals-deep", "Ten Meals Deep", "🍲", BadgeCategory.QUANTITY,
        "Cook 10 meals.", target = 10,
    ) { it.sessions.size },
    BadgeDefinition(
        "century-chef", "Century Chef", "💯", BadgeCategory.QUANTITY,
        "Cook 100 meals.", target = 100,
    ) { it.sessions.size },
    BadgeDefinition(
        "xp-overachiever", "XP Overachiever", "🏆", BadgeCategory.QUANTITY,
        "Earn 2,000 total XP from cooking.", target = 2000,
    ) { it.sessions.sumOf(CookingSession::xpEarned) },
    BadgeDefinition(
        "pantry-starter", "Pantry Starter", "🌱", BadgeCategory.DISCOVERY,
        "Log 5 different ingredients in your pantry.", target = 5,
    ) { it.ingredients.size },
    BadgeDefinition(
        "ingredient-explorer", "Ingredient Explorer", "🧭", BadgeCategory.DISCOVERY,
        "Log 20 different ingredients in your pantry.", target = 20,
    ) { it.ingredients.size },
    BadgeDefinition(
        "full-shelf", "Full Shelf", "🗄️", BadgeCategory.DISCOVERY,
        "Stock ingredients from 6 different pantry categories.", target = 6,
    ) { snapshot ->
        snapshot.ingredients.map(Ingredient::category).filter { it != IngredientCategory.OTHER }.distinct().size
    },
    BadgeDefinition(
        "protein-pro", "Protein Pro", "🥩", BadgeCategory.DISCOVERY,
        "Cook 10 meals with at least 25g of protein.", target = 10,
    ) { snapshot -> snapshot.sessions.count { (it.proteinGrams ?: 0) >= 25 } },
    BadgeDefinition(
        "night-owl", "Night Owl", "🦉", BadgeCategory.ACHIEVEMENT,
        "Cook a meal after 10pm.", target = 1,
    ) { snapshot -> if (snapshot.sessions.any { it.cookedAt.hourUtc() >= 22 }) 1 else 0 },
    BadgeDefinition(
        "early-bird", "Early Bird", "🌅", BadgeCategory.ACHIEVEMENT,
        "Cook a meal before 7am.", target = 1,
    ) { snapshot -> if (snapshot.sessions.any { it.cookedAt.hourUtc() < 7 }) 1 else 0 },
    BadgeDefinition(
        "perfect-plate", "Perfect Plate", "⭐", BadgeCategory.ACHIEVEMENT,
        "Log a 5-star meal.", target = 1,
    ) { snapshot -> if (snapshot.sessions.any { it.rating == 5 }) 1 else 0 },
    BadgeDefinition(
        "marathon-cook", "Marathon Cook", "⏱️", BadgeCategory.ACHIEVEMENT,
        "Rack up 10 hours of kitchen time.", target = 600,
    ) { it.sessions.sumOf(CookingSession::durationMinutes) },
)

/** Derives [Badge] progress live from [CookingSessionRepository]/[IngredientRepository]; persists
 * earned dates in the SQLDelight-backed `badgeEarned` table (see [refreshEarned]) so a badge's
 * earned date stays stable once reached instead of reading as "now" on every recomposition. */
class BadgeRepositoryImpl(
    private val cookingSessionRepository: CookingSessionRepository,
    private val ingredientRepository: IngredientRepository,
    private val database: LevelChefDatabase,
) : BadgeRepository {

    override fun observeAll(): Flow<List<Badge>> =
        combine(
            cookingSessionRepository.observeAll(),
            ingredientRepository.observeAll(),
            database.badgeQueries.selectAll().asFlow().mapToList(Dispatchers.Default),
        ) { sessions, ingredients, earnedRows ->
            val snapshot = BadgeSnapshot(sessions, ingredients)
            val earnedAt = earnedRows.associate { it.badgeId to Instant.parse(it.earnedAt) }
            CATALOG.map { it.toBadge(snapshot, earnedAt[it.id]) }
        }

    override suspend fun refreshEarned() {
        val snapshot = BadgeSnapshot(
            sessions = cookingSessionRepository.observeAll().first(),
            ingredients = ingredientRepository.observeAll().first(),
        )
        val now = Clock.System.now().toString()
        CATALOG.forEach { def -> if (def.progress(snapshot) >= def.target) database.badgeQueries.markEarned(def.id, now) }
    }
}

private fun BadgeDefinition.toBadge(snapshot: BadgeSnapshot, earnedAt: Instant?) = Badge(
    id = id,
    name = name,
    emoji = emoji,
    category = category,
    description = description,
    progressCurrent = progress(snapshot).coerceAtMost(target),
    progressTarget = target,
    earnedAt = earnedAt,
)
