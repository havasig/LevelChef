package com.levelchef.core.designsystem

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.levelchef.core.ui.theme.LevelChefTextStyles
import com.levelchef.core.ui.theme.LevelChefTheme

/** The "Top App Bar/Home" extended component (Figma instance 371:672) — title + optional settings action. */
@Composable
fun LevelChefTopAppBarHome(
    title: String,
    modifier: Modifier = Modifier,
    onSettingsClick: (() -> Unit)? = null,
) {
    val colors = LevelChefTheme.colors
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(colors.background)
            .height(56.dp)
            .padding(start = 16.dp, end = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(title, color = colors.textPrimary, style = LevelChefTextStyles.h2)
        if (onSettingsClick != null) {
            LevelChefIconButton(
                icon = Icons.Filled.Settings,
                contentDescription = "Settings",
                style = IconButtonStyle.PLAIN,
                onClick = onSettingsClick,
            )
        }
    }
}

@LevelChefPreview
@Composable
private fun LevelChefTopAppBarHomePreview() {
    LevelChefTheme { LevelChefTopAppBarHome("LevelChef", onSettingsClick = {}) }
}
