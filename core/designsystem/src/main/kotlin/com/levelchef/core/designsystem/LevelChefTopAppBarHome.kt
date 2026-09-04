package com.levelchef.core.designsystem

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.levelchef.core.ui.theme.BackgroundPrimary
import com.levelchef.core.ui.theme.LevelChefTextStyles
import com.levelchef.core.ui.theme.TextPrimary

/** The "Top App Bar/Home" extended component (Figma instance 371:672) — title + settings action. */
@Composable
fun LevelChefTopAppBarHome(title: String, onSettingsClick: () -> Unit, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(BackgroundPrimary)
            .height(56.dp)
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(title, color = TextPrimary, style = LevelChefTextStyles.h2)
        Icon(
            imageVector = Icons.Filled.Settings,
            contentDescription = "Settings",
            tint = TextPrimary,
            modifier = Modifier
                .size(24.dp)
                .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null, onClick = onSettingsClick),
        )
    }
}
