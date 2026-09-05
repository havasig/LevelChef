package com.levelchef.core.designsystem

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.levelchef.core.ui.theme.LevelChefTextStyles
import com.levelchef.core.ui.theme.LevelChefTheme

/** The Tag base component (Figma node 202:264) — 4 colors x 2 selection states. */
@Composable
fun LevelChefTag(
    label: String,
    color: TagColor = TagColor.PURPLE,
    modifier: Modifier = Modifier,
    emoji: String? = null,
    selected: Boolean = false,
    showClose: Boolean = false,
    onClose: (() -> Unit)? = null,
) {
    val colors = tagColorsFor(color, selected)
    val textColor = colors.text
    Row(
        modifier = modifier
            .background(colors.background, RoundedCornerShape(20.dp))
            .border(1.dp, colors.stroke, RoundedCornerShape(20.dp))
            .padding(horizontal = 14.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (emoji != null) Text(emoji, style = LevelChefTextStyles.bodySmall, color = textColor)
        Text(label, style = LevelChefTextStyles.bodySmall, color = textColor)
        if (showClose) {
            Icon(
                imageVector = Icons.Filled.Close,
                contentDescription = "Remove $label",
                tint = textColor,
                modifier = Modifier
                    .size(14.dp)
                    .let { m -> if (onClose != null) m.clickable(interactionSource = remember { MutableInteractionSource() }, indication = null, onClick = onClose) else m },
            )
        }
    }
}

@LevelChefPreview
@Composable
private fun LevelChefTagPreview() {
    LevelChefTheme {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            TagColor.entries.forEach { color ->
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    LevelChefTag(label = "Tag Label", color = color, emoji = "⚡", selected = false)
                    LevelChefTag(label = "Tag Label", color = color, emoji = "⚡", selected = true, showClose = true, onClose = {})
                }
            }
        }
    }
}
