package com.levelchef.core.designsystem

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.levelchef.core.ui.theme.LevelChefTextStyles
import com.levelchef.core.ui.theme.LevelChefTheme
import com.levelchef.core.ui.theme.OnAccent

/** A labelled +/- stepper for a small bounded count (Figma node 371:728's "Set servings" control)
 * — [unitLabel] is the word shown after [value] (e.g. "servings"), so this reuses for any future
 * count-with-unit control. */
@Composable
fun LevelChefServingsStepper(
    label: String,
    value: Int,
    unitLabel: String,
    onChange: (delta: Int) -> Unit,
    modifier: Modifier = Modifier,
    decreaseContentDescription: String = "Decrease",
    increaseContentDescription: String = "Increase",
) {
    val colors = LevelChefTheme.colors
    Row(
        modifier = modifier
            .fillMaxWidth()
            .border(0.5.dp, colors.border, RoundedCornerShape(16.dp))
            .background(colors.surface, RoundedCornerShape(16.dp))
            .padding(14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(label, color = colors.textPrimary, style = LevelChefTextStyles.bodyRegularBold)
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
            StepperButton(
                symbol = "−",
                contentDescription = decreaseContentDescription,
                background = colors.accentPrimary.copy(alpha = 0.14f),
                tint = colors.accentPrimary,
                onClick = { onChange(-1) },
            )
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {
                Text("$value", color = colors.textPrimary, style = LevelChefTextStyles.bodyRegularBold)
                Text(unitLabel, color = colors.textSecondary, style = LevelChefTextStyles.bodySmall)
            }
            StepperButton(
                symbol = "+",
                contentDescription = increaseContentDescription,
                background = colors.accentPrimary,
                tint = OnAccent,
                onClick = { onChange(1) },
            )
        }
    }
}

@Composable
private fun StepperButton(
    symbol: String,
    contentDescription: String,
    background: Color,
    tint: Color,
    onClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(background, RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .size(32.dp)
            .semantics { this.contentDescription = contentDescription },
        contentAlignment = Alignment.Center,
    ) {
        Text(symbol, color = tint, style = LevelChefTextStyles.bodyLargeBold)
    }
}

@LevelChefPreview
@Composable
private fun LevelChefServingsStepperPreview() {
    LevelChefTheme {
        LevelChefServingsStepper(label = "Set servings", value = 2, unitLabel = "servings", onChange = {})
    }
}
