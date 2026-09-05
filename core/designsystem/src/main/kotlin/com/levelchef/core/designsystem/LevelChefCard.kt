package com.levelchef.core.designsystem

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.levelchef.core.ui.theme.LevelChefTextStyles
import com.levelchef.core.ui.theme.LevelChefTheme

/** The generic Card base component (Figma node 23:35) — the primitive other cards build on. */
@Composable
fun LevelChefCard(
    modifier: Modifier = Modifier,
    horizontalAlignment: Alignment.Horizontal = Alignment.Start,
    content: @Composable ColumnScope.() -> Unit,
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = LevelChefTheme.colors.surface),
        border = cardBorder(),
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalAlignment = horizontalAlignment,
            content = content,
        )
    }
}

@LevelChefPreview
@Composable
private fun LevelChefCardPreview() {
    LevelChefTheme {
        LevelChefCard {
            Text("Card Title", color = LevelChefTheme.colors.textPrimary, style = LevelChefTextStyles.bodyLargeBold)
            Text(
                "Description text goes here. This card can hold any content.",
                color = LevelChefTheme.colors.textSecondary,
                style = LevelChefTextStyles.bodyRegular,
            )
        }
    }
}
