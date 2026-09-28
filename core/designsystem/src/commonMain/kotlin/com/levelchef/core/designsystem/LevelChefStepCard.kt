package com.levelchef.core.designsystem

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.levelchef.core.ui.theme.LevelChefTextStyles
import com.levelchef.core.ui.theme.LevelChefTheme
import com.levelchef.core.ui.theme.OnAccent

/** A numbered instruction row (Figma node 371:728's "Steps" list): a circular index badge next to
 * [text], with an optional [trailingContent] slot below it (e.g. recipedetail's timer chip) so
 * this component itself stays free of any step-specific behavior. */
@Composable
fun LevelChefStepCard(
    number: Int,
    text: String,
    modifier: Modifier = Modifier,
    trailingContent: (@Composable ColumnScope.() -> Unit)? = null,
) {
    val colors = LevelChefTheme.colors
    Row(
        modifier = modifier
            .fillMaxWidth()
            .border(0.5.dp, colors.border, RoundedCornerShape(16.dp))
            .background(colors.surface, RoundedCornerShape(16.dp))
            .padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Box(
            modifier = Modifier
                .size(28.dp)
                .background(colors.accentPrimary, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Text("$number", color = OnAccent, style = LevelChefTextStyles.bodySmallBold)
        }
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(text, color = colors.textPrimary, style = LevelChefTextStyles.bodySmall)
            trailingContent?.invoke(this)
        }
    }
}

@LevelChefPreview
@Composable
private fun LevelChefStepCardPreview() {
    LevelChefTheme {
        LevelChefStepCard(number = 1, text = "Preheat the oven to 200°C.")
    }
}
