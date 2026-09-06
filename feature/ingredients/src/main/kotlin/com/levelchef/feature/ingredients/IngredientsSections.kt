package com.levelchef.feature.ingredients

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.levelchef.core.designsystem.LevelChefIngredientCard
import com.levelchef.core.model.Ingredient
import com.levelchef.core.model.IngredientMacros
import com.levelchef.core.ui.theme.LevelChefTextStyles
import com.levelchef.core.ui.theme.LevelChefTheme

private const val COLLAPSED_COUNT = 4

@Composable
internal fun IngredientCategoryBlock(
    section: IngredientCategorySection,
    onIngredientClick: (String) -> Unit,
) {
    val colors = LevelChefTheme.colors
    var expanded by remember { mutableStateOf(false) }
    val visible = if (expanded) section.ingredients else section.ingredients.take(COLLAPSED_COUNT)

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(section.category.label(), color = colors.textSecondary, style = LevelChefTextStyles.captionRegular)
        visible.chunked(2).forEach { pair ->
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                pair.forEach { ingredient ->
                    LevelChefIngredientCard(
                        emoji = ingredient.emoji,
                        name = ingredient.name,
                        subtitle = ingredient.subtitleLabel(),
                        onClick = { onIngredientClick(ingredient.id) },
                        modifier = Modifier.weight(1f),
                    )
                }
                if (pair.size == 1) Spacer(Modifier.weight(1f))
            }
        }
        if (section.ingredients.size > COLLAPSED_COUNT) {
            Text(
                stringResource(if (expanded) R.string.ingredients_show_less else R.string.ingredients_show_all),
                color = colors.accentPrimary,
                style = LevelChefTextStyles.captionRegular,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expanded = !expanded },
            )
        }
    }
}

@Composable
private fun Ingredient.subtitleLabel(): String = defaultUnit?.label().orEmpty()

@Composable
internal fun MacroGrid(macros: IngredientMacros, modifier: Modifier = Modifier) {
    val colors = LevelChefTheme.colors
    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            MacroTile(
                stringResource(R.string.ingredient_macro_calories),
                stringResource(R.string.ingredient_macro_calories_value, macros.calories),
                colors.tagYellowText,
                Modifier.weight(1f),
            )
            MacroTile(
                stringResource(R.string.ingredient_macro_protein),
                stringResource(R.string.ingredient_macro_grams_value, macros.proteinGrams.trimZeros()),
                colors.accentPrimary,
                Modifier.weight(1f),
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            MacroTile(
                stringResource(R.string.ingredient_macro_carbs),
                stringResource(R.string.ingredient_macro_grams_value, macros.carbsGrams.trimZeros()),
                colors.tagGreenText,
                Modifier.weight(1f),
            )
            MacroTile(
                stringResource(R.string.ingredient_macro_fat),
                stringResource(R.string.ingredient_macro_grams_value, macros.fatGrams.trimZeros()),
                colors.tagRedText,
                Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun MacroTile(label: String, value: String, dotColor: Color, modifier: Modifier = Modifier) {
    val colors = LevelChefTheme.colors
    Column(
        modifier = modifier
            .border(0.5.dp, colors.border, RoundedCornerShape(12.dp))
            .background(colors.background, RoundedCornerShape(12.dp))
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
            Spacer(
                Modifier
                    .size(8.dp)
                    .background(dotColor, CircleShape),
            )
            Text(label, color = colors.textSecondary, style = LevelChefTextStyles.captionBold)
        }
        Text(value, color = colors.textPrimary, style = LevelChefTextStyles.bodyLargeBold)
    }
}
