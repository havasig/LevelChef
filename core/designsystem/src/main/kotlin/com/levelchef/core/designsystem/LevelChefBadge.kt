package com.levelchef.core.designsystem

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.levelchef.core.ui.theme.AccentPrimary
import com.levelchef.core.ui.theme.TextPrimary

/** Small pill badge — "Style=Light" (filled purple) / "Style=Dark" (white fill) from the Figma base components. */
@Composable
fun LevelChefBadge(text: String, style: BadgeStyle = BadgeStyle.LIGHT, modifier: Modifier = Modifier) {
    val bg = if (style == BadgeStyle.LIGHT) AccentPrimary else TextPrimary
    val fg = if (style == BadgeStyle.LIGHT) TextPrimary else AccentPrimary
    Box(
        modifier = modifier
            .background(bg, RoundedCornerShape(8.dp))
            .padding(horizontal = 10.dp, vertical = 5.dp),
    ) {
        Text(text = text, color = fg, style = MaterialTheme.typography.labelSmall)
    }
}
