package com.levelchef.core.designsystem

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.levelchef.core.ui.theme.LevelChefTheme
import com.levelchef.core.ui.theme.OnAccent

/** The Icon Button extended component (Figma node 251:1025) — see [IconButtonStyle] for the variants. */
@Composable
fun LevelChefIconButton(
    icon: ImageVector,
    contentDescription: String?,
    style: IconButtonStyle,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = LevelChefTheme.colors
    val shape = RoundedCornerShape(8.dp)
    val contentColor = when (style) {
        IconButtonStyle.FILLED -> OnAccent
        IconButtonStyle.OUTLINED -> colors.accentPrimary
        IconButtonStyle.PLAIN -> colors.textPrimary
    }
    val sized = modifier.size(40.dp)
    val decorated = when (style) {
        IconButtonStyle.FILLED -> sized.background(colors.accentPrimary, shape)
        IconButtonStyle.OUTLINED -> sized.border(1.5.dp, colors.accentPrimary, shape)
        IconButtonStyle.PLAIN -> sized
    }
    Box(
        modifier = decorated
            .clip(shape)
            .clickable(interactionSource = null, indication = ripple(color = contentColor), onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(icon, contentDescription = contentDescription, tint = contentColor, modifier = Modifier.size(18.dp))
    }
}

@LevelChefPreview
@Composable
private fun LevelChefIconButtonPreview() {
    LevelChefTheme {
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            LevelChefIconButton(
                icon = Icons.Filled.Add,
                contentDescription = "Add",
                style = IconButtonStyle.FILLED,
                onClick = {},
            )
            LevelChefIconButton(
                icon = Icons.Filled.Settings,
                contentDescription = "Settings",
                style = IconButtonStyle.OUTLINED,
                onClick = {},
            )
            LevelChefIconButton(
                icon = Icons.Filled.Settings,
                contentDescription = "Settings",
                style = IconButtonStyle.PLAIN,
                onClick = {},
            )
        }
    }
}
