package com.levelchef.core.designsystem

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.levelchef.core.ui.theme.AccentText
import com.levelchef.core.ui.theme.LevelChefTextStyles
import com.levelchef.core.ui.theme.SuccessGreen
import com.levelchef.core.ui.theme.TextPrimary
import com.levelchef.core.ui.theme.TextSecondary

/**
 * The Weekly Challenge Card base component (Figma node 187:255). Figma's component has no
 * button; [action] lets callers (e.g. Home's "Done" button) inject one without forking the base
 * component's look.
 */
@Composable
fun LevelChefWeeklyChallengeCard(
    title: String,
    xp: Int,
    inProgress: Boolean,
    modifier: Modifier = Modifier,
    category: String = "WEEKLY CHALLENGE",
    action: (@Composable () -> Unit)? = null,
) {
    LevelChefCard(modifier = modifier.fillMaxWidth()) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text(category, color = AccentText, style = LevelChefTextStyles.captionBold)
            LevelChefBadge("+$xp XP", style = BadgeStyle.DARK)
        }
        Text(title, color = TextPrimary, style = LevelChefTextStyles.bodyRegularBold)
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Box(modifier = Modifier.size(8.dp).background(SuccessGreen, RoundedCornerShape(50)))
                Text(if (inProgress) "In progress" else "Not started", color = TextSecondary, style = LevelChefTextStyles.bodySmallBold)
            }
            action?.invoke()
        }
    }
}
