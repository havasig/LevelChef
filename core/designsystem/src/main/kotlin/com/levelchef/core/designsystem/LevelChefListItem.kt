package com.levelchef.core.designsystem

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.levelchef.core.ui.theme.LevelChefTextStyles
import com.levelchef.core.ui.theme.LevelChefTheme

/** The List Item base component (Figma node 23:38) — avatar, title/subtitle, trailing badge. */
@Composable
fun LevelChefListItem(
    avatarInitials: String,
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier,
    badgeText: String? = "Active",
) {
    val colors = LevelChefTheme.colors
    Row(
        modifier = modifier.fillMaxWidth().background(colors.surface).padding(horizontal = 16.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        LevelChefAvatar(initials = avatarInitials, size = 40.dp)
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(title, color = colors.textPrimary, style = LevelChefTextStyles.bodyRegularBold)
            Text(subtitle, color = colors.textSecondary, style = LevelChefTextStyles.bodySmall)
        }
        if (badgeText != null) LevelChefBadge(badgeText, style = BadgeStyle.LIGHT)
    }
}

@LevelChefPreview
@Composable
private fun LevelChefListItemPreview() {
    LevelChefTheme { LevelChefListItem(avatarInitials = "AB", title = "List Item Title", subtitle = "Supporting text") }
}
