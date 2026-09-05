package com.levelchef.core.designsystem

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.levelchef.core.ui.theme.LevelChefTextStyles
import com.levelchef.core.ui.theme.LevelChefTheme

/** Shared scaffolding for feature screens not yet built out from their Figma node. */
@Composable
fun PlaceholderScreen(title: String) {
    Box(modifier = Modifier
        .fillMaxSize()
        .padding(24.dp), contentAlignment = Alignment.Center) {
        Text(
            "$title — coming next",
            color = LevelChefTheme.colors.textSecondary,
            style = LevelChefTextStyles.bodyRegular,
        )
    }
}

@LevelChefPreview
@Composable
private fun PlaceholderScreenPreview() {
    LevelChefTheme { PlaceholderScreen("Recipe Detail") }
}
