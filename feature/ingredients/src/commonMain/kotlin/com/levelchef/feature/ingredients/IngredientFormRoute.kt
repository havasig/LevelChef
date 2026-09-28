package com.levelchef.feature.ingredients

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

/** Stateful entry point for the add / edit ingredient form. [ingredientId] null means "add". */
@Composable
fun IngredientFormRoute(
    ingredientId: String?,
    onBackClick: () -> Unit,
    onSaved: () -> Unit,
    viewModel: IngredientFormViewModel = koinViewModel { parametersOf(ingredientId) },
) {
    val state by viewModel.uiState.collectAsState()

    LaunchedEffect(state.saved) {
        if (state.saved) onSaved()
    }

    IngredientFormScreen(
        state = state,
        actions = IngredientFormActions(
            onNameChange = viewModel::setName,
            onCategoryChange = viewModel::setCategory,
            onUnitChange = viewModel::setUnit,
            onCaloriesChange = viewModel::setCalories,
            onProteinChange = viewModel::setProtein,
            onCarbsChange = viewModel::setCarbs,
            onFatChange = viewModel::setFat,
            onSave = viewModel::save,
        ),
        onBackClick = onBackClick,
    )
}
