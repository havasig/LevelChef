package com.levelchef.feature.recipedetail

import com.levelchef.core.model.RecipeIngredient
import kotlin.test.Test
import kotlin.test.assertEquals

class RecipeDetailDomainMappersTest {

    @Test
    fun a_quantity_less_ingredient_renders_its_name_unchanged() {
        val line = RecipeIngredient("Rosemary, salt, pepper").toDisplayLine(servings = 4, baseServings = 2)
        assertEquals("Rosemary, salt, pepper", line)
    }

    @Test
    fun an_unscaled_serving_keeps_the_original_amount() {
        val line = RecipeIngredient("chicken breast", quantity = 300.0, unit = "g")
            .toDisplayLine(servings = 2, baseServings = 2)
        assertEquals("300 g chicken breast", line)
    }

    @Test
    fun doubling_the_servings_doubles_the_amount() {
        val line = RecipeIngredient("chicken breast", quantity = 300.0, unit = "g")
            .toDisplayLine(servings = 4, baseServings = 2)
        assertEquals("600 g chicken breast", line)
    }

    @Test
    fun a_fractional_result_is_trimmed_to_two_decimals() {
        val line = RecipeIngredient("lemon juice", quantity = 1.0)
            .toDisplayLine(servings = 3, baseServings = 2)
        assertEquals("1.5 lemon juice", line)
    }

    @Test
    fun a_zero_base_serving_is_treated_as_one() {
        val line = RecipeIngredient("garlic", quantity = 2.0, unit = "cloves")
            .toDisplayLine(servings = 2, baseServings = 0)
        assertEquals("4 cloves garlic", line)
    }
}
