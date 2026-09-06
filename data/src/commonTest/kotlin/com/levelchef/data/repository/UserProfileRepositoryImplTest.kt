package com.levelchef.data.repository

import com.levelchef.core.model.CookingSession
import com.levelchef.core.model.Ingredient
import com.levelchef.core.model.IngredientCategory
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.Instant
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class UserProfileRepositoryImplTest {

    private fun ingredient(id: String) = Ingredient(id, id, IngredientCategory.OTHER, "🥕")

    private fun session(cookedAt: String, rating: Int? = null) = CookingSession(
        id = cookedAt,
        recipeId = "r",
        recipeName = "Recipe",
        cookedAt = Instant.parse(cookedAt),
        xpEarned = 10,
        rating = rating,
    )

    @Test
    fun profile_aggregates_xp_and_session_count_from_cooking_sessions() = runTest {
        val repository = UserProfileRepositoryImpl(
            FakeCookingSessionRepository(xp = 450, count = 7),
            FakeIngredientRepository(),
            FakeWeeklyChallengeRepository(),
        )

        val profile = repository.getProfile()

        assertEquals(450, profile.totalXp)
        assertEquals(7, profile.cookingSessionsCount)
    }

    @Test
    fun total_xp_folds_in_weekly_challenge_bonus_xp() = runTest {
        val repository = UserProfileRepositoryImpl(
            FakeCookingSessionRepository(xp = 450, count = 7),
            FakeIngredientRepository(),
            FakeWeeklyChallengeRepository(awardedXp = 150),
        )

        assertEquals(600, repository.getProfile().totalXp)
    }

    @Test
    fun new_ingredients_count_reflects_the_pantry_size() = runTest {
        val repository = UserProfileRepositoryImpl(
            FakeCookingSessionRepository(),
            FakeIngredientRepository(listOf(ingredient("a"), ingredient("b"), ingredient("c"))),
            FakeWeeklyChallengeRepository(),
        )

        assertEquals(3, repository.getProfile().newIngredientsCount)
    }

    @Test
    fun kitchen_time_reflects_total_duration_minutes() = runTest {
        val repository = UserProfileRepositoryImpl(
            FakeCookingSessionRepository(durationMinutes = 340),
            FakeIngredientRepository(),
            FakeWeeklyChallengeRepository(),
        )

        assertEquals(340, repository.getProfile().kitchenTimeMinutes)
    }

    @Test
    fun streak_counts_consecutive_days_back_from_the_most_recent_session() = runTest {
        val repository = UserProfileRepositoryImpl(
            FakeCookingSessionRepository(
                all = listOf(
                    session("2026-01-03T10:00:00Z"),
                    session("2026-01-02T10:00:00Z"),
                    session("2026-01-01T10:00:00Z"),
                    session("2025-12-20T10:00:00Z"), // not consecutive - breaks the streak
                ),
            ),
            FakeIngredientRepository(),
            FakeWeeklyChallengeRepository(),
        )

        assertEquals(3, repository.getProfile().currentStreakDays)
    }

    @Test
    fun avg_rating_percent_is_null_without_any_rated_sessions() = runTest {
        val repository = UserProfileRepositoryImpl(
            FakeCookingSessionRepository(all = listOf(session("2026-01-01T10:00:00Z"))),
            FakeIngredientRepository(),
            FakeWeeklyChallengeRepository(),
        )

        assertNull(repository.getProfile().avgRatingPercent)
    }

    @Test
    fun avg_rating_percent_scales_the_average_star_rating_to_a_percentage() = runTest {
        val repository = UserProfileRepositoryImpl(
            FakeCookingSessionRepository(
                all = listOf(
                    session("2026-01-01T10:00:00Z", rating = 4),
                    session("2026-01-02T10:00:00Z", rating = 5),
                ),
            ),
            FakeIngredientRepository(),
            FakeWeeklyChallengeRepository(),
        )

        assertEquals(90, repository.getProfile().avgRatingPercent)
    }
}
