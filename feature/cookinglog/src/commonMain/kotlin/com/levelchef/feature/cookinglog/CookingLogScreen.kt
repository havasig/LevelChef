package com.levelchef.feature.cookinglog

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.levelchef.core.designsystem.LevelChefPreview
import com.levelchef.core.designsystem.LevelChefSearchBar
import com.levelchef.core.designsystem.LevelChefTabBar
import com.levelchef.core.designsystem.LevelChefTopAppBarHome
import com.levelchef.core.ui.theme.LevelChefTheme
import com.levelchef.feature.cookinglog.generated.resources.Res
import com.levelchef.feature.cookinglog.generated.resources.cooking_log_search_placeholder
import com.levelchef.feature.cookinglog.generated.resources.cooking_log_tab_all
import com.levelchef.feature.cookinglog.generated.resources.cooking_log_tab_cooked
import com.levelchef.feature.cookinglog.generated.resources.cooking_log_tab_new
import com.levelchef.feature.cookinglog.generated.resources.cooking_log_title
import org.jetbrains.compose.resources.stringResource

/**
 * Figma node 489:1362 — "My saved recipes". This is the real content for the **Recipes** bottom-nav
 * tab (see `LevelChefNav.kt`), not a drill-down screen: search + an All/Cooked/New filter, and each
 * saved recipe as either a plain recipe card (never cooked) or a "Last cooked" card (rated, cooked
 * at least once). Stateless: [CookingLogRoute] owns the [CookingLogViewModel] and supplies [state] +
 * [actions].
 */
@Composable
fun CookingLogScreen(
    state: CookingLogUiState = CookingLogUiState(),
    actions: CookingLogActions = CookingLogActions(),
) {
    val colors = LevelChefTheme.colors
    val filteredItems = state.allItems.filtered(state.selectedTab, state.query)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background),
    ) {
        LevelChefTopAppBarHome(
            title = stringResource(Res.string.cooking_log_title),
            modifier = Modifier.statusBarsPadding(),
        )
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            LevelChefSearchBar(
                query = state.query,
                onQueryChange = actions.onQueryChange,
                placeholder = stringResource(Res.string.cooking_log_search_placeholder),
            )
            LevelChefTabBar(
                tabs = listOf(
                    stringResource(Res.string.cooking_log_tab_all),
                    stringResource(Res.string.cooking_log_tab_cooked),
                    stringResource(Res.string.cooking_log_tab_new),
                ),
                selectedIndex = state.selectedTab.ordinal,
                onTabSelected = { index -> actions.onTabSelected(CookingLogTab.entries[index]) },
            )
        }
        if (filteredItems.isEmpty() && !state.loading) {
            EmptyState(state.selectedTab, modifier = Modifier.weight(1f))
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                items(filteredItems, key = { it.recipeId }) { item ->
                    SavedRecipeRow(
                        item = item,
                        onClick = { actions.onRecipeClick(item.recipeId) },
                        onDelete = { actions.onDeleteClick(item.recipeId) },
                    )
                }
            }
        }
    }
}

@LevelChefPreview
@Composable
private fun CookingLogScreenPreview() {
    LevelChefTheme { CookingLogScreen(state = sampleCookingLogState) }
}
