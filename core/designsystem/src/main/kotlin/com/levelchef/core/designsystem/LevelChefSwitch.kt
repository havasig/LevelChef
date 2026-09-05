package com.levelchef.core.designsystem

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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

/** The Toggle extended component (Figma node 251:969) — an on/off switch with an "On"/"Off" label. */
@Composable
fun LevelChefSwitch(checked: Boolean, onCheckedChange: (Boolean) -> Unit, modifier: Modifier = Modifier) {
    val colors = LevelChefTheme.colors
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedTrackColor = colors.accentPrimary,
                checkedThumbColor = colors.textPrimary,
                uncheckedTrackColor = colors.surface,
                uncheckedThumbColor = colors.textSecondary,
                uncheckedBorderColor = colors.border,
            ),
        )
        Text(
            if (checked) "On" else "Off",
            color = if (checked) colors.textPrimary else colors.textSecondary,
            style = if (checked) LevelChefTextStyles.bodyRegularBold else LevelChefTextStyles.bodyRegular,
        )
    }
}

@LevelChefPreview
@Composable
private fun LevelChefSwitchPreview() {
    var checked by remember { mutableStateOf(true) }
    LevelChefTheme { LevelChefSwitch(checked = checked, onCheckedChange = { checked = it }) }
}
