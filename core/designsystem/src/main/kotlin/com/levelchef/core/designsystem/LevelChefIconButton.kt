package com.levelchef.core.designsystem

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.levelchef.core.ui.theme.LevelChefTheme
import com.levelchef.core.ui.theme.OnAccent

/** The Icon Button extended component (Figma node 251:1025) — 2 "Style=" variants. */
@Composable
fun LevelChefIconButton(
    icon: ImageVector,
    contentDescription: String?,
    style: IconButtonStyle,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val accentPrimary = LevelChefTheme.colors.accentPrimary
    val shape = RoundedCornerShape(8.dp)
    val filled = style == IconButtonStyle.FILLED
    Box(
        modifier = modifier
            .size(40.dp)
            .let { if (filled) it.background(accentPrimary, shape) else it.border(1.5.dp, accentPrimary, shape) }
            .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(icon, contentDescription = contentDescription, tint = if (filled) OnAccent else accentPrimary, modifier = Modifier.size(18.dp))
    }
}

@LevelChefPreview
@Composable
private fun LevelChefIconButtonPreview() {
    LevelChefTheme {
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            LevelChefIconButton(icon = Icons.Filled.Add, contentDescription = "Add", style = IconButtonStyle.FILLED, onClick = {})
            LevelChefIconButton(icon = Icons.Filled.Settings, contentDescription = "Settings", style = IconButtonStyle.OUTLINED, onClick = {})
        }
    }
}
