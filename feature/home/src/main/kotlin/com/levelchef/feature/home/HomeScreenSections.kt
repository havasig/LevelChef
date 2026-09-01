package com.levelchef.feature.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.levelchef.core.designsystem.BadgeStyle
import com.levelchef.core.designsystem.LevelChefBadge
import com.levelchef.core.designsystem.LevelChefDivider
import com.levelchef.core.designsystem.LevelChefTag
import com.levelchef.core.designsystem.cardBorder
import com.levelchef.core.ui.theme.AccentPrimary
import com.levelchef.core.ui.theme.BackgroundSurface
import com.levelchef.core.ui.theme.SuccessGreen
import com.levelchef.core.ui.theme.TextPrimary
import com.levelchef.core.ui.theme.TextSecondary
import com.levelchef.core.ui.theme.WarningYellow

/** Section composables for [HomeScreen], kept internal since they're implementation details of this feature. */

@Composable
internal fun LevelProgressSection(state: HomeUiState) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        LevelChefBadge(state.levelLabel, style = BadgeStyle.LIGHT)
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            val progress = (state.currentXp.toFloat() / state.xpForNextLevel).coerceIn(0f, 1f)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .size(height = 8.dp, width = 0.dp)
                    .background(Color.White.copy(alpha = 0.1f), RoundedCornerShape(100.dp)),
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(progress)
                        .size(height = 8.dp, width = 0.dp)
                        .background(AccentPrimary, RoundedCornerShape(100.dp)),
                )
            }
            Text(
                "${state.currentXp} / ${state.xpForNextLevel} XP to next level",
                color = TextSecondary,
                style = MaterialTheme.typography.bodySmall,
            )
        }
    }
}

@Composable
internal fun StatCardsRow(state: HomeUiState) {
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        StatCard("🍳 ${state.cookingSessions}", "Cooking sessions", Modifier.weight(1f))
        StatCard("🌿 ${state.ingredientsTried}", "Ingredients tried", Modifier.weight(1f))
    }
}

@Composable
internal fun StatCard(value: String, label: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = BackgroundSurface),
        border = cardBorder(),
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(value, color = TextPrimary, style = MaterialTheme.typography.headlineMedium)
            Text(label, color = TextSecondary, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
internal fun WeeklyChallengeCard(state: HomeUiState) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = BackgroundSurface),
        border = cardBorder(),
    ) {
        Column(modifier = Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("WEEKLY CHALLENGE", color = TextPrimary.copy(alpha = 0.6f), style = MaterialTheme.typography.labelSmall)
                LevelChefBadge("+${state.challengeXp} XP", style = BadgeStyle.DARK)
            }
            Text(state.challengeTitle, color = TextPrimary, style = MaterialTheme.typography.labelLarge)
            Text(state.challengeDescription, color = TextSecondary, style = MaterialTheme.typography.bodySmall)
            LevelChefDivider()
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Box(modifier = Modifier.size(8.dp).background(SuccessGreen, RoundedCornerShape(50)))
                    Text(if (state.challengeInProgress) "In progress" else "Not started", color = TextSecondary, style = MaterialTheme.typography.bodySmall)
                }
                OutlinedButton(onClick = {}, shape = RoundedCornerShape(12.dp)) {
                    Text("Done", color = AccentPrimary, style = MaterialTheme.typography.labelLarge)
                }
            }
        }
    }
}

@Composable
internal fun RecipeRecommendationCard(rec: RecipeRecommendation, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = BackgroundSurface),
        border = cardBorder(),
    ) {
        Row(modifier = Modifier.padding(16.dp), horizontalArrangement = Arrangement.spacedBy(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier.size(56.dp).background(AccentPrimary, RoundedCornerShape(14.dp)),
                contentAlignment = Alignment.Center,
            ) {
                Text(rec.emoji, style = MaterialTheme.typography.headlineMedium)
            }
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(rec.name, color = TextPrimary, style = MaterialTheme.typography.labelLarge, modifier = Modifier.weight(1f))
                    Text("+${rec.xp} XP", color = AccentPrimary, style = MaterialTheme.typography.bodySmall)
                }
                Text("⏱ ${rec.minutes} min · ${rec.difficulty}", color = TextSecondary, style = MaterialTheme.typography.bodySmall)
                LevelChefTag(rec.tagEmoji, rec.tagLabel, rec.tagColors)
            }
        }
    }
}

@Composable
internal fun LastCookedCard(lastCooked: LastCooked) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = BackgroundSurface),
        border = cardBorder(),
    ) {
        Row(modifier = Modifier.padding(16.dp), horizontalArrangement = Arrangement.spacedBy(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier.size(56.dp).background(AccentPrimary, RoundedCornerShape(14.dp)),
                contentAlignment = Alignment.Center,
            ) { Text("🕐") }
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("Last Cooked", color = TextSecondary, style = MaterialTheme.typography.labelSmall)
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(lastCooked.recipeName, color = TextPrimary, style = MaterialTheme.typography.labelLarge)
                    Text("· ${lastCooked.whenText}", color = TextSecondary, style = MaterialTheme.typography.bodySmall)
                }
                Text("★".repeat(lastCooked.stars) + "☆".repeat(5 - lastCooked.stars), color = WarningYellow)
            }
            Text("✓", color = SuccessGreen, style = MaterialTheme.typography.headlineMedium)
        }
    }
}
