package com.levelchef.feature.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.levelchef.core.designsystem.LevelChefDivider
import com.levelchef.core.ui.theme.AccentPrimary
import com.levelchef.core.ui.theme.TextPrimary

@Composable
fun HomeScreen(
    state: HomeUiState = HomeUiState(),
    onCookToday: () -> Unit = {},
    onRecipeClick: (RecipeRecommendation) -> Unit = {},
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 24.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp),
    ) {
        item { LevelProgressSection(state) }
        item { StatCardsRow(state) }
        item { LevelChefDivider() }
        item { WeeklyChallengeCard(state) }
        item { LevelChefDivider() }
        item {
            Button(
                onClick = onCookToday,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = AccentPrimary, contentColor = TextPrimary),
            ) {
                Text("Cook today — show me a recipe! 🍽", style = MaterialTheme.typography.labelLarge)
            }
        }
        item { LevelChefDivider() }
        item {
            Text("Recommended for you", color = TextPrimary, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
        }
        items(state.recommendations) { rec ->
            RecipeRecommendationCard(rec, onClick = { onRecipeClick(rec) })
        }
        state.lastCooked?.let { lastCooked ->
            item { LevelChefDivider() }
            item { LastCookedCard(lastCooked) }
        }
    }
}
