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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
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
import com.levelchef.core.ui.theme.OnAccent

/** The Checkbox extended component (Figma node 251:948) — unchecked/checked states with a label. */
@Composable
fun LevelChefCheckbox(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
) {
    val colors = LevelChefTheme.colors
    Row(
        modifier = modifier.clickable(interactionSource = null, indication = null) { onCheckedChange(!checked) },
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(20.dp)
                .let {
                    if (checked) {
                        it.background(colors.accentPrimary, RoundedCornerShape(4.dp))
                    } else {
                        it.border(
                            1.5.dp,
                            colors.border,
                            RoundedCornerShape(4.dp),
                        )
                    }
                },
            contentAlignment = Alignment.Center,
        ) {
            if (checked) Icon(
                Icons.Filled.Check,
                contentDescription = null,
                tint = OnAccent,
                modifier = Modifier.size(12.dp),
            )
        }
        Text(
            label,
            color = if (checked) colors.textPrimary else colors.textSecondary,
            style = if (checked) LevelChefTextStyles.bodyRegularBold else LevelChefTextStyles.bodyRegular,
        )
    }
}

@LevelChefPreview
@Composable
private fun LevelChefCheckboxPreview() {
    var checked by remember { mutableStateOf(true) }
    LevelChefTheme {
        LevelChefCheckbox(
            checked = checked,
            onCheckedChange = { checked = it },
            label = if (checked) "Checked" else "Unchecked",
        )
    }
}
