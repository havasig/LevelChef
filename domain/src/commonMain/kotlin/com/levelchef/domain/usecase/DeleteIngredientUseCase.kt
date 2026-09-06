package com.levelchef.domain.usecase

import com.levelchef.domain.repository.IngredientRepository

/** Removes an ingredient from the pantry. */
class DeleteIngredientUseCase(private val ingredientRepository: IngredientRepository) {
    suspend operator fun invoke(id: String) = ingredientRepository.delete(id)
}
