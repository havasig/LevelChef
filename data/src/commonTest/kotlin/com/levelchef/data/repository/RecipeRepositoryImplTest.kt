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
}
