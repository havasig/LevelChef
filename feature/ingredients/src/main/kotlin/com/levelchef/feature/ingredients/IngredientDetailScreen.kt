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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.levelchef.core.designsystem.ButtonType
import com.levelchef.core.designsystem.LevelChefButton
import com.levelchef.core.designsystem.LevelChefCard
import com.levelchef.core.designsystem.LevelChefModal
import com.levelchef.core.designsystem.LevelChefPreview
import com.levelchef.core.designsystem.LevelChefTag
import com.levelchef.core.designsystem.LevelChefTopAppBarInner
import com.levelchef.core.designsystem.TagColor
import com.levelchef.core.model.Ingredient
import com.levelchef.core.ui.theme.LevelChefTextStyles
import com.levelchef.core.ui.theme.LevelChefTheme

@Composable
fun IngredientDetailScreen(
    state: IngredientDetailUiState = IngredientDetailUiState(),
    onBackClick: () -> Unit = {},
    onEditClick: () -> Unit = {},
    onDeleteConfirmed: () -> Unit = {},
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(LevelChefTheme.colors.background)
            .navigationBarsPadding(),
    ) {
        LevelChefTopAppBarInner(
            title = stringResource(R.string.ingredient_detail_title),
            onBackClick = onBackClick,
            modifier = Modifier.statusBarsPadding(),
        )
        state.ingredient?.let { DetailContent(it, onEditClick, onDeleteConfirmed) }
    }
}

@Composable
private fun DetailContent(ingredient: Ingredient, onEditClick: () -> Unit, onDeleteConfirmed: () -> Unit) {
    val colors = LevelChefTheme.colors
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .background(colors.accentPrimary.copy(alpha = 0.12f), RoundedCornerShape(20.dp)),
            contentAlignment = Alignment.Center,
        ) {
            Text(ingredient.emoji, fontSize = 88.sp)
        }

        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    ingredient.name,
                    color = colors.textPrimary,
                    style = LevelChefTextStyles.h1,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f),
                )
                LevelChefTag(label = ingredient.category.label(), color = TagColor.PURPLE)
            }
            UnitBox(ingredient)
        }

        ingredient.macros?.let { macros ->
            LevelChefCard {
                Text(
                    stringResource(R.string.ingredient_detail_macros_title),
                    color = colors.textPrimary,
                    style = LevelChefTextStyles.bodySmallBold,
                )
                MacroGrid(macros)
            }
        }

        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.padding(top = 8.dp),
        ) {
            LevelChefButton(
                label = stringResource(R.string.ingredient_detail_edit),
                type = ButtonType.PRIMARY,
                onClick = onEditClick,
                modifier = Modifier.fillMaxWidth(),
            )
            DeleteButton(ingredientName = ingredient.name, onConfirmed = onDeleteConfirmed)
        }
    }
}

@Composable
private fun UnitBox(ingredient: Ingredient) {
    val colors = LevelChefTheme.colors
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .border(0.5.dp, colors.border, RoundedCornerShape(14.dp))
            .background(colors.surface, RoundedCornerShape(14.dp))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Text(
            stringResource(R.string.ingredient_detail_default_unit),
            color = colors.textSecondary,
            style = LevelChefTextStyles.captionRegular,
        )
        Text(
            ingredient.defaultUnit?.label() ?: stringResource(R.string.ingredient_detail_no_unit),
            color = colors.textPrimary,
            style = LevelChefTextStyles.bodyRegularBold,
        )
    }
}

@Composable
private fun DeleteButton(ingredientName: String, onConfirmed: () -> Unit) {
    var showConfirm by remember { mutableStateOf(false) }
    LevelChefButton(
        label = stringResource(R.string.ingredient_detail_delete),
        type = ButtonType.DESTRUCTIVE_SECONDARY,
        onClick = { showConfirm = true },
        modifier = Modifier.fillMaxWidth(),
    )
    if (showConfirm) {
        LevelChefModal(
            title = stringResource(R.string.ingredient_detail_delete_confirm_title),
            message = stringResource(R.string.ingredient_detail_delete_confirm_message, ingredientName),
            onDismiss = { showConfirm = false },
            onConfirm = {
                showConfirm = false
                onConfirmed()
            },
            cancelLabel = stringResource(R.string.ingredient_dialog_cancel),
            confirmLabel = stringResource(R.string.ingredient_dialog_delete),
        )
    }
}

@LevelChefPreview
@Composable
private fun IngredientDetailScreenPreview() {
    LevelChefTheme { IngredientDetailScreen(state = sampleIngredientDetailState) }
}
