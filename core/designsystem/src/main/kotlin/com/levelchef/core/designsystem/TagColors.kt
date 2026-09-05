package com.levelchef.core.designsystem

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.levelchef.core.ui.theme.AccentText
import com.levelchef.core.ui.theme.LevelChefTheme
import com.levelchef.core.ui.theme.TagGreenStroke
import com.levelchef.core.ui.theme.TagPurpleBase
import com.levelchef.core.ui.theme.TagRedStroke
import com.levelchef.core.ui.theme.TagYellowStroke

data class TagColors(val background: Color, val stroke: Color, val text: Color)

/**
 * Resolves a [TagColor]/selected-state pair to its (background, stroke, text) colors, per the
 * Figma Tag base component (node 202:264) and its light-mode duplicate (node 590:963): the
 * *selected* look is a hardcoded 70%-alpha wash — confirmed identical in both Figma themes, for
 * all 4 colors. Only the *unselected* purple background is genuinely themed (a translucent wash
 * in dark, a solid pale fill in light — green/red/yellow's unselected backgrounds are already
 * theme-invariant). Every color's label text is themed too (translucent white on a dark page vs.
 * a solid per-color dark shade on a light page).
 */
@Composable
internal fun tagColorsFor(color: TagColor, selected: Boolean): TagColors {
    val colors = LevelChefTheme.colors
    return when (color) {
        TagColor.PURPLE -> TagColors(
            background = if (selected) TagPurpleBase.copy(alpha = 0.7f) else colors.tagPurpleBg,
            stroke = AccentText.copy(alpha = if (selected) 1f else 0.4f),
            text = colors.tagPurpleText,
        )
        TagColor.GREEN -> tagColorsFor(TagGreenStroke, selected, colors.tagGreenText)
        TagColor.RED -> tagColorsFor(TagRedStroke, selected, colors.tagRedText)
        TagColor.YELLOW -> tagColorsFor(TagYellowStroke, selected, colors.tagYellowText)
    }
}

private fun tagColorsFor(stroke: Color, selected: Boolean, text: Color) =
    TagColors(background = stroke.copy(alpha = if (selected) 0.7f else 0.2f), stroke = stroke, text = text)
