package com.levelchef.core.designsystem

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.levelchef.core.ui.theme.BadgeInverseText
import com.levelchef.core.ui.theme.LevelChefTheme

/**
 * Small pill badge — "Style=Light" (dark theme: solid accent fill; light theme: soft tonal fill)
 * / "Style=Dark" (dark theme: white fill; light theme: pale-lavender fill) from the Figma base
 * components. The two styles genuinely redesign per theme rather than just swapping neutrals.
 */
@Composable
fun LevelChefBadge(text: String, style: BadgeStyle = BadgeStyle.LIGHT, modifier: Modifier = Modifier) {
    val colors = LevelChefTheme.colors
    val bg = if (style == BadgeStyle.LIGHT) colors.badgePrimaryBg else colors.badgeInverseBg
    val fg = if (style == BadgeStyle.LIGHT) colors.badgePrimaryText else BadgeInverseText
    Box(
        modifier = modifier
            .background(bg, RoundedCornerShape(8.dp))
            .padding(horizontal = 10.dp, vertical = 5.dp),
    ) {
        Text(text = text, color = fg, style = MaterialTheme.typography.labelSmall)
    }
}

@LevelChefPreview
@Composable
private fun LevelChefBadgePreview() {
    LevelChefTheme {
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            LevelChefBadge("Active", style = BadgeStyle.LIGHT)
            LevelChefBadge("Active", style = BadgeStyle.DARK)
        }
    }
}
