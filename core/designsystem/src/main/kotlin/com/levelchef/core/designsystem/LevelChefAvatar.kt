package com.levelchef.core.designsystem

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.levelchef.core.ui.theme.LevelChefTextStyles
import com.levelchef.core.ui.theme.LevelChefTheme
import com.levelchef.core.ui.theme.OnAccent

/** The Avatar base component (Figma node 23:30) — a circular initials chip. Default 48dp; List Item uses 40dp. */
@Composable
fun LevelChefAvatar(initials: String, modifier: Modifier = Modifier, size: Dp = 48.dp) {
    Box(
        modifier = modifier.size(size).background(LevelChefTheme.colors.accentPrimary, CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        Text(text = initials, color = OnAccent, style = LevelChefTextStyles.bodyLargeBold)
    }
}

@LevelChefPreview
@Composable
private fun LevelChefAvatarPreview() {
    LevelChefTheme {
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            LevelChefAvatar(initials = "AB")
        }
    }
}
