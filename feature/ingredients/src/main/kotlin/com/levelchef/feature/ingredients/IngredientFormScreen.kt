package com.levelchef.feature.ingredients

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.levelchef.core.designsystem.ButtonType
import com.levelchef.core.designsystem.LevelChefButton
import com.levelchef.core.designsystem.LevelChefDropdown
import com.levelchef.core.designsystem.LevelChefInputField
import com.levelchef.core.designsystem.LevelChefPreview
import com.levelchef.core.designsystem.LevelChefTopAppBarInner
import com.levelchef.core.model.IngredientCategory
import com.levelchef.core.model.MeasurementUnit
import com.levelchef.core.ui.theme.LevelChefTextStyles
import com.levelchef.core.ui.theme.LevelChefTheme

@Composable
fun IngredientFormScreen(
    state: IngredientFormUiState = IngredientFormUiState(),
    actions: IngredientFormActions = IngredientFormActions(),
    onBackClick: () -> Unit = {},
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(LevelChefTheme.colors.background)
            .navigationBarsPadding(),
    ) {
        LevelChefTopAppBarInner(
            title = stringResource(
                if (state.editing) R.string.ingredient_form_title_edit else R.string.ingredient_form_title_new,
            ),
            onBackClick = onBackClick,
            modifier = Modifier.statusBarsPadding(),
        )
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            ImagePreview(state.category)

            LevelChefInputField(
                label = stringResource(R.string.ingredient_form_name_label),
                value = state.name,
                onValueChange = actions.onNameChange,
                placeholder = stringResource(R.string.ingredient_form_name_placeholder),
            )

            CategoryPicker(state.category, actions.onCategoryChange)
            UnitPicker(state.unit, actions.onUnitChange)
            MacrosInput(state, actions)

            LevelChefButton(
                label = stringResource(R.string.ingredient_form_save),
                type = ButtonType.PRIMARY,
                onClick = actions.onSave,
                enabled = state.canSave,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Composable
private fun ImagePreview(category: IngredientCategory) {
    val colors = LevelChefTheme.colors
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .border(0.5.dp, colors.border, RoundedCornerShape(16.dp))
            .background(colors.surface, RoundedCornerShape(16.dp))
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Box(
            modifier = Modifier
                .height(64.dp)
                .fillMaxWidth(),
            contentAlignment = Alignment.Center,
        ) {
            Text(categoryEmoji(category), fontSize = 40.sp)
        }
        Text(
            stringResource(R.string.ingredient_form_image_caption),
            color = colors.textSecondary,
            style = LevelChefTextStyles.captionRegular,
        )
    }
}

@Composable
private fun CategoryPicker(selected: IngredientCategory, onChange: (IngredientCategory) -> Unit) {
    val labels = IngredientCategory.entries.associateWith { it.label() }
    LevelChefDropdown(
        label = stringResource(R.string.ingredient_form_category_label),
        selectedOption = labels.getValue(selected),
        options = labels.values.toList(),
        onOptionSelected = { picked -> labels.entries.first { it.value == picked }.key.let(onChange) },
    )
}

@Composable
private fun UnitPicker(selected: MeasurementUnit?, onChange: (MeasurementUnit?) -> Unit) {
    val none = stringResource(R.string.ingredient_form_unit_none)
    val unitLabels = MeasurementUnit.entries.associateWith { it.label() }
    LevelChefDropdown(
        label = stringResource(R.string.ingredient_form_unit_label),
        selectedOption = selected?.let { unitLabels.getValue(it) } ?: none,
        options = listOf(none) + unitLabels.values,
        onOptionSelected = { picked ->
            onChange(unitLabels.entries.firstOrNull { it.value == picked }?.key)
        },
    )
}

@Composable
private fun MacrosInput(state: IngredientFormUiState, actions: IngredientFormActions) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(
                stringResource(R.string.ingredient_form_macros_label),
                color = LevelChefTheme.colors.textPrimary,
                style = LevelChefTextStyles.bodyRegularBold,
            )
            Text(
                stringResource(R.string.ingredient_form_optional),
                color = LevelChefTheme.colors.textSecondary,
                style = LevelChefTextStyles.captionRegular,
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            NumberField(R.string.ingredient_form_calories_label, state.calories, actions.onCaloriesChange, Modifier.weight(1f))
            NumberField(R.string.ingredient_form_protein_label, state.protein, actions.onProteinChange, Modifier.weight(1f))
        }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            NumberField(R.string.ingredient_form_carbs_label, state.carbs, actions.onCarbsChange, Modifier.weight(1f))
            NumberField(R.string.ingredient_form_fat_label, state.fat, actions.onFatChange, Modifier.weight(1f))
        }
    }
}

@Composable
private fun NumberField(labelRes: Int, value: String, onChange: (String) -> Unit, modifier: Modifier = Modifier) {
    LevelChefInputField(
        label = stringResource(labelRes),
        value = value,
        onValueChange = onChange,
        modifier = modifier,
        placeholder = "0",
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
    )
}

@LevelChefPreview
@Composable
private fun IngredientFormScreenPreview() {
    LevelChefTheme { IngredientFormScreen(state = sampleIngredientFormState) }
}
