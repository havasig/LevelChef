package com.levelchef.core.designsystem

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.levelchef.core.ui.theme.AccentPrimary
import com.levelchef.core.ui.theme.DestructiveRed
import com.levelchef.core.ui.theme.LevelChefTextStyles
import com.levelchef.core.ui.theme.TextPrimary

private val ButtonShape = RoundedCornerShape(12.dp)

private data class ButtonLook(val background: Color?, val border: Color?, val textColor: Color, val underline: Boolean)

private fun lookFor(type: ButtonType): ButtonLook = when (type) {
    ButtonType.PRIMARY -> ButtonLook(background = AccentPrimary, border = null, textColor = TextPrimary, underline = false)
    ButtonType.SECONDARY -> ButtonLook(background = null, border = AccentPrimary, textColor = AccentPrimary, underline = false)
    ButtonType.TERTIARY -> ButtonLook(background = null, border = null, textColor = AccentPrimary, underline = true)
    ButtonType.DESTRUCTIVE -> ButtonLook(background = DestructiveRed, border = null, textColor = TextPrimary, underline = false)
    ButtonType.DESTRUCTIVE_SECONDARY -> ButtonLook(background = null, border = DestructiveRed, textColor = DestructiveRed, underline = false)
    ButtonType.DESTRUCTIVE_TERTIARY -> ButtonLook(background = null, border = null, textColor = DestructiveRed, underline = true)
}

/** The Button base component (Figma node 199:264) — 6 "Type=" variants. */
@Composable
fun LevelChefButton(
    label: String,
    type: ButtonType,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val look = lookFor(type)
    var styledModifier = modifier.height(48.dp)
    if (look.background != null) styledModifier = styledModifier.background(look.background, ButtonShape)
    if (look.border != null) styledModifier = styledModifier.border(1.5.dp, look.border, ButtonShape)
    Row(
        modifier = styledModifier
            .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null, onClick = onClick)
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
