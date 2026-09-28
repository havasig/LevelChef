package com.levelchef.core.designsystem

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.levelchef.core.ui.theme.LevelChefTextStyles
import com.levelchef.core.ui.theme.LevelChefTheme

/**
 * The ingredient grid card from the "Used Ingredients" screen (Figma node 437:945) — an emoji band
 * over a name + a short caption ([subtitle], e.g. the default unit). Tap opens the detail screen.
 */
@Composable
fun LevelChefIngredientCard(
    emoji: String,
    name: String,
    subtitle: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = LevelChefTheme.colors
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .border(0.5.dp, colors.border, RoundedCornerShape(16.dp))
            .background(colors.surface)
            .clickable(onClick = onClick),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .background(colors.accentPrimary.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center,
        ) {
            Text(emoji, style = LevelChefTextStyles.h2.copy(fontSize = 28.sp))
        }
        Column(
            modifier = Modifier.padding(8.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                name,
                color = colors.textPrimary,
                style = LevelChefTextStyles.bodyRegularBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(subtitle, color = colors.textSecondary, style = LevelChefTextStyles.captionRegular)
        }
    }
}

@LevelChefPreview
@Composable
private fun LevelChefIngredientCardPreview() {
    LevelChefTheme {
        LevelChefIngredientCard(emoji = "🍗", name = "Chicken breast", subtitle = "Grams", onClick = {})
    }
}
