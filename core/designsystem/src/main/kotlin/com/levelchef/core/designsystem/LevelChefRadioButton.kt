package com.levelchef.core.designsystem

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.levelchef.core.ui.theme.LevelChefTextStyles
import com.levelchef.core.ui.theme.LevelChefTheme

/** The Radio Button extended component (Figma node 251:958) — unselected/selected states with a label. */
@Composable
fun LevelChefRadioButton(selected: Boolean, onClick: () -> Unit, label: String, modifier: Modifier = Modifier) {
    val colors = LevelChefTheme.colors
    Row(
        modifier = modifier.clickable(interactionSource = remember { MutableInteractionSource() }, indication = null, onClick = onClick),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(20.dp)
                .border(1.5.dp, if (selected) colors.accentPrimary else colors.border, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            if (selected) Box(modifier = Modifier.size(10.dp).background(colors.accentPrimary, CircleShape))
        }
        Text(
            label,
            color = if (selected) colors.textPrimary else colors.textSecondary,
            style = if (selected) LevelChefTextStyles.bodyRegularBold else LevelChefTextStyles.bodyRegular,
        )
    }
}

@LevelChefPreview
@Composable
private fun LevelChefRadioButtonPreview() {
    var selected by remember { mutableStateOf(true) }
    LevelChefTheme {
        LevelChefRadioButton(selected = selected, onClick = { selected = !selected }, label = if (selected) "Selected" else "Unselected")
    }
}
