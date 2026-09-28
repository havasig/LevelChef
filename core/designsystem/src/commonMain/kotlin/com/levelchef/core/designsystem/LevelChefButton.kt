package com.levelchef.core.designsystem

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.levelchef.core.ui.theme.DestructiveRed
import com.levelchef.core.ui.theme.LevelChefTextStyles
import com.levelchef.core.ui.theme.LevelChefTheme
import com.levelchef.core.ui.theme.OnAccent

private val ButtonShape = RoundedCornerShape(12.dp)
private const val DisabledAlpha = 0.4f

private data class ButtonLook(val background: Color?, val border: Color?, val textColor: Color, val underline: Boolean)

private fun lookFor(type: ButtonType, accentPrimary: Color): ButtonLook = when (type) {
    ButtonType.PRIMARY -> ButtonLook(
        background = accentPrimary,
        border = null,
        textColor = OnAccent,
        underline = false,
    )

    ButtonType.SECONDARY -> ButtonLook(
        background = null,
        border = accentPrimary,
        textColor = accentPrimary,
        underline = false,
    )

    ButtonType.TERTIARY -> ButtonLook(
        background = null,
        border = null,
        textColor = accentPrimary,
        underline = true,
    )

    ButtonType.DESTRUCTIVE -> ButtonLook(
        background = DestructiveRed,
        border = null,
        textColor = OnAccent,
        underline = false,
    )

    ButtonType.DESTRUCTIVE_SECONDARY -> ButtonLook(
        background = null,
        border = DestructiveRed,
        textColor = DestructiveRed,
        underline = false,
    )

    ButtonType.DESTRUCTIVE_TERTIARY -> ButtonLook(
        background = null,
        border = null,
        textColor = DestructiveRed,
        underline = true,
    )
}

/** The Button base component (Figma node 199:264) — 6 "Type=" variants. */
@Composable
fun LevelChefButton(
    label: String,
    type: ButtonType,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    val look = lookFor(type, LevelChefTheme.colors.accentPrimary)
    var styledModifier = modifier.height(48.dp)
    if (look.background != null) styledModifier = styledModifier.background(look.background, ButtonShape)
    if (look.border != null) styledModifier = styledModifier.border(1.5.dp, look.border, ButtonShape)
    Row(
        modifier = styledModifier
            .clip(ButtonShape)
            .alpha(if (enabled) 1f else DisabledAlpha)
            .clickable(
                enabled = enabled,
                interactionSource = null,
                indication = ripple(color = look.textColor),
                onClick = onClick,
            )
            .padding(horizontal = 28.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            color = look.textColor,
            style = LevelChefTextStyles.bodyRegularBold,
            textDecoration = if (look.underline) TextDecoration.Underline else null,
        )
    }
}

@LevelChefPreview
@Composable
private fun LevelChefButtonPreview() {
    LevelChefTheme {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            ButtonType.entries.forEach { type ->
                LevelChefButton(
                    label = type.name.lowercase().replace('_', ' ').replaceFirstChar { it.uppercase() },
                    type = type,
                    onClick = {},
                )
            }
        }
    }
}
