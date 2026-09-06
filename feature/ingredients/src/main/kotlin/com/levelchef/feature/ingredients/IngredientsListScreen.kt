package com.levelchef.feature.ingredients

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.levelchef.core.designsystem.LevelChefPreview
import com.levelchef.core.designsystem.LevelChefTopAppBarInner
import com.levelchef.core.ui.theme.LevelChefTextStyles
import com.levelchef.core.ui.theme.LevelChefTheme

@Composable
fun IngredientsListScreen(
    state: IngredientsListUiState = IngredientsListUiState(),
    onBackClick: () -> Unit = {},
    onIngredientClick: (String) -> Unit = {},
    onAddClick: () -> Unit = {},
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(LevelChefTheme.colors.background)
            .navigationBarsPadding(),
    ) {
        LevelChefTopAppBarInner(
            title = stringResource(R.string.ingredients_list_title),
            onBackClick = onBackClick,
            trailingIcon = Icons.Filled.Add,
            trailingContentDescription = stringResource(R.string.ingredients_add),
            onTrailingClick = onAddClick,
            modifier = Modifier.statusBarsPadding(),
        )
        if (state.isEmpty) {
            EmptyState()
        } else {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp),
            ) {
                state.sections.forEach { section ->
                    IngredientCategoryBlock(section = section, onIngredientClick = onIngredientClick)
                }
            }
        }
    }
}

@Composable
private fun EmptyState() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            stringResource(R.string.ingredients_empty),
            color = LevelChefTheme.colors.textSecondary,
            style = LevelChefTextStyles.bodyRegular,
            textAlign = TextAlign.Center,
        )
    }
}

@LevelChefPreview
@Composable
private fun IngredientsListScreenPreview() {
    LevelChefTheme { IngredientsListScreen(state = sampleIngredientsListState) }
}

@LevelChefPreview
@Composable
private fun IngredientsListScreenEmptyPreview() {
    LevelChefTheme { IngredientsListScreen(state = IngredientsListUiState(loading = false)) }
}
