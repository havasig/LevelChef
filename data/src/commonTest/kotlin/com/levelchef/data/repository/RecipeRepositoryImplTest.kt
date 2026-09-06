package com.levelchef.data.repository

import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class RecipeRepositoryImplTest {

    private val repository = RecipeRepositoryImpl()

    @Test
    fun recommendations_return_the_sample_recipes() = runTest {
        val recommendations = repository.getRecommendations()

        assertEquals(3, recommendations.size)
        assertTrue(recommendations.any { it.id == "chicken-curry" })
    }

    @Test
    fun get_by_id_returns_the_matching_recipe() = runTest {
        assertEquals("Steak quinoa bowl", repository.getById("steak-quinoa-bowl")?.name)
    }

    @Test
    fun get_by_id_returns_null_for_an_unknown_id() = runTest {
        assertNull(repository.getById("does-not-exist"))
    }

    @Test
    fun every_sample_recipe_is_fully_populated_for_the_detail_screen() = runTest {
        repository.getRecommendations().forEach { recipe ->
            assertTrue(recipe.ingredients.isNotEmpty(), "${recipe.id} has no ingredients")
            assertTrue(recipe.steps.isNotEmpty(), "${recipe.id} has no steps")
            assertTrue(recipe.caloriesKcal != null, "${recipe.id} has no calories")
            assertTrue(recipe.servings >= 1, "${recipe.id} has a bad serving count")
        }
    }
}
