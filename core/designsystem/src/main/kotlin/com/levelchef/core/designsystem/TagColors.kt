package com.levelchef.core.designsystem

import androidx.compose.ui.graphics.Color
import com.levelchef.core.ui.theme.AccentText
import com.levelchef.core.ui.theme.TagGreenStroke
import com.levelchef.core.ui.theme.TagPurpleBase
import com.levelchef.core.ui.theme.TagRedStroke
import com.levelchef.core.ui.theme.TagYellowStroke

data class TagColors(val background: Color, val stroke: Color)

/**
 * Resolves a [TagColor]/selected-state pair to its (background, stroke) colors, per the Figma
 * Tag base component (node 202:264): unselected = 20% background / solid stroke (purple's stroke
 * is instead 40%, since its unselected look is softer); selected = 70% background / solid stroke.
 */
internal fun tagColorsFor(color: TagColor, selected: Boolean): TagColors {
    val backgroundAlpha = if (selected) 0.7f else 0.2f
    return when (color) {
        TagColor.PURPLE -> TagColors(
            background = TagPurpleBase.copy(alpha = backgroundAlpha),
            stroke = AccentText.copy(alpha = if (selected) 1f else 0.4f),
        )
        TagColor.GREEN -> TagColors(TagGreenStroke.copy(alpha = backgroundAlpha), TagGreenStroke)
        TagColor.RED -> TagColors(TagRedStroke.copy(alpha = backgroundAlpha), TagRedStroke)
        TagColor.YELLOW -> TagColors(TagYellowStroke.copy(alpha = backgroundAlpha), TagYellowStroke)
    }
}
