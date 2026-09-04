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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.levelchef.core.ui.theme.AccentPrimary
import com.levelchef.core.ui.theme.BorderDefault
import com.levelchef.core.ui.theme.LevelChefTextStyles
import com.levelchef.core.ui.theme.TextPrimary
import com.levelchef.core.ui.theme.TextSecondary

/** The Checkbox extended component (Figma node 251:948) — unchecked/checked states with a label. */
@Composable
fun LevelChefCheckbox(checked: Boolean, onCheckedChange: (Boolean) -> Unit, label: String, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) { onCheckedChange(!checked) },
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(20.dp)
                .let {
                    if (checked) it.background(AccentPrimary, RoundedCornerShape(4.dp)) else it.border(1.5.dp, BorderDefault, RoundedCornerShape(4.dp))
                },
            contentAlignment = Alignment.Center,
        ) {
            if (checked) Icon(Icons.Filled.Check, contentDescription = null, tint = TextPrimary, modifier = Modifier.size(12.dp))
        }
        Text(
            label,
            color = if (checked) TextPrimary else TextSecondary,
            style = if (checked) LevelChefTextStyles.bodyRegularBold else LevelChefTextStyles.bodyRegular,
        )
    }
}
