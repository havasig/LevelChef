package com.levelchef.feature.ingredients

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.levelchef.core.model.Ingredient
import com.levelchef.core.model.IngredientCategory
import com.levelchef.core.model.IngredientMacros
import com.levelchef.core.model.MeasurementUnit
import com.levelchef.domain.repository.IngredientRepository
import com.levelchef.domain.usecase.SaveIngredientUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class IngredientFormViewModel(
    private val ingredientId: String?,
    private val ingredientRepository: IngredientRepository,
    private val saveIngredient: SaveIngredientUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(IngredientFormUiState(editing = ingredientId != null))
    val uiState: StateFlow<IngredientFormUiState> = _uiState.asStateFlow()

    /** The ingredient being edited — keeps its emoji / imageUrl when saving. */
    private var loaded: Ingredient? = null

    init {
        if (ingredientId != null) {
            viewModelScope.launch {
                ingredientRepository.getById(ingredientId)?.let { existing ->
                    loaded = existing
                    _uiState.update {
                        it.copy(
                            name = existing.name,
                            category = existing.category,
                            unit = existing.defaultUnit,
                            calories = existing.macros?.calories?.toString().orEmpty(),
                            protein = existing.macros?.proteinGrams?.trimZeros().orEmpty(),
                            carbs = existing.macros?.carbsGrams?.trimZeros().orEmpty(),
                            fat = existing.macros?.fatGrams?.trimZeros().orEmpty(),
                        )
                    }
                }
            }
        }
    }

    fun setName(value: String) = _uiState.update { it.copy(name = value) }

    fun setCategory(value: IngredientCategory) = _uiState.update { it.copy(category = value) }

    fun setUnit(value: MeasurementUnit?) = _uiState.update { it.copy(unit = value) }

    fun setCalories(value: String) = _uiState.update { it.copy(calories = value.filter(Char::isDigit)) }

    fun setProtein(value: String) = _uiState.update { it.copy(protein = decimal(value)) }

    fun setCarbs(value: String) = _uiState.update { it.copy(carbs = decimal(value)) }

    fun setFat(value: String) = _uiState.update { it.copy(fat = decimal(value)) }

    fun save() {
        val state = _uiState.value
        if (!state.canSave) return
        val existing = loaded
        // Keep a hand-picked emoji only while the category is unchanged; otherwise derive it.
        val emoji = if (existing != null && existing.category == state.category) {
            existing.emoji
        } else {
            categoryEmoji(state.category)
        }
        viewModelScope.launch {
            saveIngredient(
                Ingredient(
                    id = existing?.id.orEmpty(),
                    name = state.name.trim(),
                    category = state.category,
                    emoji = emoji,
                    defaultUnit = state.unit,
                    macros = state.macros(),
                    imageUrl = existing?.imageUrl,
                ),
            )
            _uiState.update { it.copy(saved = true) }
        }
    }
}

private fun IngredientFormUiState.macros(): IngredientMacros? {
    if (calories.isBlank() && protein.isBlank() && carbs.isBlank() && fat.isBlank()) return null
    return IngredientMacros(
        calories = calories.toIntOrNull() ?: 0,
        proteinGrams = protein.toDoubleOrNull() ?: 0.0,
        carbsGrams = carbs.toDoubleOrNull() ?: 0.0,
        fatGrams = fat.toDoubleOrNull() ?: 0.0,
    )
}

private fun decimal(raw: String): String {
    val filtered = raw.filter { it.isDigit() || it == '.' }
    val dot = filtered.indexOf('.')
    return if (dot == -1) filtered else filtered.substring(0, dot + 1) + filtered.substring(dot + 1).replace(".", "")
}
