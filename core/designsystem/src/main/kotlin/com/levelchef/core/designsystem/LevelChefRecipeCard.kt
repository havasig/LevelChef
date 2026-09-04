package com.levelchef.core.designsystem

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import com.levelchef.core.ui.theme.AccentPrimary
import com.levelchef.core.ui.theme.BackgroundSurface
import com.levelchef.core.ui.theme.LevelChefTextStyles
import com.levelchef.core.ui.theme.TextPrimary
import com.levelchef.core.ui.theme.TextSecondary

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
    tagLabel: String? = null,
    tagEmoji: String? = null,
    tagColor: TagColor = TagColor.PURPLE,
) {
    Card(
        modifier = modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = BackgroundSurface),
        border = cardBorder(),
    ) {
        Row(modifier = Modifier.padding(16.dp), horizontalArrangement = Arrangement.spacedBy(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier.size(56.dp).background(AccentPrimary, RoundedCornerShape(14.dp)),
                contentAlignment = Alignment.Center,
            ) {
                Text(emoji, style = MaterialTheme.typography.headlineMedium)
            }
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(title, color = TextPrimary, style = LevelChefTextStyles.bodyRegularBold, modifier = Modifier.weight(1f))
                    Text("+$xp XP", color = AccentPrimary, style = LevelChefTextStyles.bodySmall)
                }
                Text("⏱ $minutes min · $difficulty", color = TextSecondary, style = LevelChefTextStyles.bodySmall)
                if (tagLabel != null) {
                    LevelChefTag(label = tagLabel, emoji = tagEmoji, color = tagColor)
                }
            }
        }
    }
}
