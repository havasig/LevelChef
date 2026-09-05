package com.levelchef.core.designsystem

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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.levelchef.core.ui.theme.LevelChefTextStyles
import com.levelchef.core.ui.theme.LevelChefTheme

/** The Recipe Card base component (Figma node 152:504). */
@Composable
fun LevelChefRecipeCard(
    emoji: String,
    title: String,
    xp: Int,
    minutes: Int,
    difficulty: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    tag: RecipeCardTag? = null,
) {
    val colors = LevelChefTheme.colors
    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = colors.surface),
        border = cardBorder(),
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .background(colors.accentPrimary, RoundedCornerShape(14.dp)),
                contentAlignment = Alignment.Center,
            ) {
                Text(emoji, style = MaterialTheme.typography.headlineMedium)
            }
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(
                        title,
                        color = colors.textPrimary,
                        style = LevelChefTextStyles.bodyRegularBold,
                        modifier = Modifier.weight(1f),
                    )
                    Text("+$xp XP", color = colors.accentPrimary, style = LevelChefTextStyles.bodySmall)
                }
                Text(
                    "⏱ $minutes min · $difficulty",
                    color = colors.textSecondary,
                    style = LevelChefTextStyles.bodySmall,
                )
                if (tag != null) {
                    LevelChefTag(label = tag.label, emoji = tag.emoji, color = tag.color)
                }
            }
        }
    }
}

@LevelChefPreview
@Composable
private fun LevelChefRecipeCardPreview() {
    LevelChefTheme {
        LevelChefRecipeCard(
            emoji = "🥩",
            title = "Chicken curry with coconut milk",
            xp = 45,
            minutes = 25,
            difficulty = "Easy",
            onClick = {},
            tag = RecipeCardTag(label = "New ingredient", emoji = "🌿", color = TagColor.PURPLE),
        )
    }
}
