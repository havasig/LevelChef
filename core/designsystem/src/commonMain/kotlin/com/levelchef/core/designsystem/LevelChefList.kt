package com.levelchef.core.designsystem

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.levelchef.core.ui.theme.LevelChefTheme

/** The List base component (Figma node 18:2031) — bordered container of [LevelChefListItem]s. */
@Composable
fun LevelChefList(entries: List<LevelChefListEntry>, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .border(0.5.dp, LevelChefTheme.colors.border, RoundedCornerShape(16.dp)),
    ) {
        entries.forEachIndexed { index, entry ->
            LevelChefListItem(entry.avatarInitials, entry.title, entry.subtitle, badgeText = entry.badgeText)
            if (index != entries.lastIndex) LevelChefDivider()
        }
    }
}

@LevelChefPreview
@Composable
private fun LevelChefListPreview() {
    LevelChefTheme {
        LevelChefList(
            entries = listOf(
                LevelChefListEntry("JA", "John Appleseed", "Product Designer"),
                LevelChefListEntry("SC", "Sarah Chen", "Engineer"),
                LevelChefListEntry("ML", "Marcus Lee", "Design Lead"),
            ),
        )
    }
}
