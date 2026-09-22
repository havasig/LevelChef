package com.levelchef.feature.mealreview

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.levelchef.core.designsystem.BadgeStyle
import com.levelchef.core.designsystem.ButtonType
import com.levelchef.core.designsystem.LevelChefBadge
import com.levelchef.core.designsystem.LevelChefButton
import com.levelchef.core.ui.theme.LevelChefTextStyles
import com.levelchef.core.ui.theme.LevelChefTheme
import com.levelchef.core.ui.theme.MacroCaloriesRed
import com.levelchef.core.ui.theme.MacroCarbsOrange
import com.levelchef.core.ui.theme.MacroFatGreen
import com.levelchef.core.ui.theme.MacroProteinPurple
import com.levelchef.feature.mealreview.MealReviewUiState.Companion.MAX_RATING

/** Section composables for [MealReviewScreen], kept internal to this feature. */

@Composable
internal fun RecipeHeaderCard(recipeName: String, xpReward: Int) {
    val colors = LevelChefTheme.colors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .border(0.5.dp, colors.border, RoundedCornerShape(12.dp))
            .background(colors.surface, RoundedCornerShape(12.dp))
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            recipeName,
            color = colors.textPrimary,
            style = LevelChefTextStyles.bodyRegularBold,
            modifier = Modifier.weight(1f),
        )
        LevelChefBadge(stringResource(R.string.meal_review_xp_badge, xpReward), style = BadgeStyle.DARK)
    }
}

@Composable
internal fun RatingSection(rating: Int, onRatingChange: (Int) -> Unit) {
    SectionCard {
        SectionLabel(stringResource(R.string.meal_review_rating_label))
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
            val colors = LevelChefTheme.colors
            repeat(MAX_RATING) { index ->
                val starValue = index + 1
                Icon(
                    Icons.Filled.Star,
                    contentDescription = stringResource(R.string.meal_review_rating_star, starValue),
                    tint = if (starValue <= rating) colors.accentPrimary else colors.border,
                    modifier = Modifier
                        .size(20.dp)
                        .clickable { onRatingChange(starValue) },
                )
            }
            Text(
                stringResource(R.string.meal_review_rating_value, rating),
                color = colors.textSecondary,
                style = LevelChefTextStyles.bodySmallBold,
            )
        }
    }
}

@Composable
internal fun NoteSection(note: String, onNoteChange: (String) -> Unit) {
    val colors = LevelChefTheme.colors
    SectionCard {
        SectionLabel(stringResource(R.string.meal_review_note_label))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 102.dp)
                .border(0.5.dp, colors.border, RoundedCornerShape(12.dp))
                .background(colors.background, RoundedCornerShape(12.dp))
                .padding(horizontal = 16.dp, vertical = 14.dp),
        ) {
            if (note.isEmpty()) {
                Text(
                    stringResource(R.string.meal_review_note_placeholder),
                    color = colors.textSecondary,
                    style = LevelChefTextStyles.bodyRegular,
                )
            }
            BasicTextField(
                value = note,
                onValueChange = onNoteChange,
                textStyle = LevelChefTextStyles.bodyRegular.copy(color = colors.textPrimary),
                cursorBrush = SolidColor(colors.textPrimary),
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Composable
internal fun DurationRow(durationMinutes: Int, onDurationChange: (Int) -> Unit) {
    val colors = LevelChefTheme.colors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .border(0.5.dp, colors.border, RoundedCornerShape(12.dp))
            .background(colors.surface, RoundedCornerShape(12.dp))
            .padding(12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        NeutralStepperButton(
            symbol = "−",
            contentDescription = stringResource(R.string.meal_review_duration_decrease),
            onClick = { onDurationChange(-MealReviewUiState.DURATION_STEP_MINUTES) },
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
            Text("⏱", style = LevelChefTextStyles.bodyRegular)
            Text(
                stringResource(R.string.meal_review_duration_unit, durationMinutes),
                color = colors.textSecondary,
                style = LevelChefTextStyles.bodyRegular,
            )
        }
        NeutralStepperButton(
            symbol = "+",
            contentDescription = stringResource(R.string.meal_review_duration_increase),
            onClick = { onDurationChange(MealReviewUiState.DURATION_STEP_MINUTES) },
        )
    }
}

@Composable
internal fun MacroValuesSection(
    caloriesKcal: Int,
    proteinGrams: Int,
    carbsGrams: Int,
    fatGrams: Int,
    onCaloriesChange: (Int) -> Unit,
    onProteinChange: (Int) -> Unit,
    onCarbsChange: (Int) -> Unit,
    onFatChange: (Int) -> Unit,
) {
    val kcalUnit = stringResource(R.string.meal_review_unit_kcal)
    val gramsUnit = stringResource(R.string.meal_review_unit_grams)
    SectionCard {
        SectionLabel(stringResource(R.string.meal_review_macros_label))
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            MacroStepperRow(
                label = stringResource(R.string.meal_review_macro_calories),
                color = MacroCaloriesRed,
                value = caloriesKcal,
                unit = kcalUnit,
                onDecrease = { onCaloriesChange(-MealReviewUiState.CALORIES_STEP) },
                onIncrease = { onCaloriesChange(MealReviewUiState.CALORIES_STEP) },
            )
            MacroStepperRow(
                label = stringResource(R.string.meal_review_macro_protein),
                color = MacroProteinPurple,
                value = proteinGrams,
                unit = gramsUnit,
                onDecrease = { onProteinChange(-MealReviewUiState.GRAMS_STEP) },
                onIncrease = { onProteinChange(MealReviewUiState.GRAMS_STEP) },
            )
            MacroStepperRow(
                label = stringResource(R.string.meal_review_macro_carbs),
                color = MacroCarbsOrange,
                value = carbsGrams,
                unit = gramsUnit,
                onDecrease = { onCarbsChange(-MealReviewUiState.GRAMS_STEP) },
                onIncrease = { onCarbsChange(MealReviewUiState.GRAMS_STEP) },
            )
            MacroStepperRow(
                label = stringResource(R.string.meal_review_macro_fat),
                color = MacroFatGreen,
                value = fatGrams,
                unit = gramsUnit,
                onDecrease = { onFatChange(-MealReviewUiState.GRAMS_STEP) },
                onIncrease = { onFatChange(MealReviewUiState.GRAMS_STEP) },
            )
        }
    }
}

@Composable
private fun MacroStepperRow(
    label: String,
    color: Color,
    value: Int,
    unit: String,
    onDecrease: () -> Unit,
    onIncrease: () -> Unit,
) {
    val colors = LevelChefTheme.colors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .border(0.5.dp, colors.border, RoundedCornerShape(12.dp))
            .background(colors.background, RoundedCornerShape(12.dp))
            .padding(12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(label, color = color, style = LevelChefTextStyles.bodyRegularBold, modifier = Modifier.width(96.dp))
        Row(
            modifier = Modifier
                .weight(1f)
                .height(56.dp)
                .border(0.5.dp, colors.border, RoundedCornerShape(12.dp))
                .background(colors.background, RoundedCornerShape(12.dp))
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            SurfaceStepperButton(
                symbol = "−",
                contentDescription = stringResource(R.string.meal_review_macro_decrease, label),
                onClick = onDecrease,
            )
            Row(
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.spacedBy(4.dp, Alignment.CenterHorizontally),
                verticalAlignment = Alignment.Bottom,
            ) {
                Text("$value", color = colors.textPrimary, style = LevelChefTextStyles.bodyLargeBold)
                Text(unit, color = colors.textSecondary, style = LevelChefTextStyles.captionRegular)
            }
            SurfaceStepperButton(
                symbol = "+",
                contentDescription = stringResource(R.string.meal_review_macro_increase, label),
                onClick = onIncrease,
            )
        }
    }
}

@Composable
internal fun IngredientsChecklist(ingredientLines: List<String>) {
    val colors = LevelChefTheme.colors
    SectionCard {
        SectionLabel(stringResource(R.string.meal_review_ingredients_label))
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            ingredientLines.forEach { line ->
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        Icons.Filled.Check,
                        contentDescription = null,
                        tint = colors.accentPrimary,
                        modifier = Modifier.size(18.dp),
                    )
                    Text(line, color = colors.textSecondary, style = LevelChefTextStyles.bodySmall)
                }
            }
        }
    }
}

@Composable
private fun SectionCard(content: @Composable ColumnScope.() -> Unit) {
    val colors = LevelChefTheme.colors
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .border(0.5.dp, colors.border, RoundedCornerShape(12.dp))
            .background(colors.surface, RoundedCornerShape(12.dp))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        content = content,
    )
}

@Composable
private fun SectionLabel(text: String) {
    Text(text, color = LevelChefTheme.colors.textSecondary, style = LevelChefTextStyles.captionBold)
}

@Composable
private fun NeutralStepperButton(symbol: String, contentDescription: String, onClick: () -> Unit) {
    val colors = LevelChefTheme.colors
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .border(0.5.dp, colors.border, RoundedCornerShape(16.dp))
            .background(colors.background, RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .size(32.dp)
            .semantics { this.contentDescription = contentDescription },
        contentAlignment = Alignment.Center,
    ) {
        Text(symbol, color = colors.textSecondary, style = LevelChefTextStyles.bodyLargeBold)
    }
}

@Composable
private fun SurfaceStepperButton(symbol: String, contentDescription: String, onClick: () -> Unit) {
    val colors = LevelChefTheme.colors
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .border(0.5.dp, colors.border, RoundedCornerShape(8.dp))
            .background(colors.surface, RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .size(32.dp)
            .semantics { this.contentDescription = contentDescription },
        contentAlignment = Alignment.Center,
    ) {
        Text(symbol, color = colors.textSecondary, style = LevelChefTextStyles.bodyRegularBold)
    }
}

@Composable
internal fun SaveButton(enabled: Boolean, onClick: () -> Unit) {
    LevelChefButton(
        label = stringResource(R.string.meal_review_save),
        type = ButtonType.PRIMARY,
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier.fillMaxWidth(),
    )
}
