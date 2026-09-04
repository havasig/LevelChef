package com.levelchef.core.designsystem

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.levelchef.core.ui.theme.AccentPrimary
import com.levelchef.core.ui.theme.BackgroundSurface
import com.levelchef.core.ui.theme.LevelChefTextStyles
import com.levelchef.core.ui.theme.TextSecondary

/** One tab rendered by [LevelChefBottomNavigationBar]. */
data class LevelChefNavItem(val icon: ImageVector, val label: String, val selected: Boolean, val onClick: () -> Unit)

/** The Bottom Navigation Bar extended component (Figma node 251:1007). */
@Composable
fun LevelChefBottomNavigationBar(items: List<LevelChefNavItem>, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(64.dp)
            .background(BackgroundSurface, RoundedCornerShape(bottomStart = 16.dp, bottomEnd = 16.dp)),
    ) {
        items.forEach { item ->
            val color = if (item.selected) AccentPrimary else TextSecondary
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null, onClick = item.onClick),
                verticalArrangement = Arrangement.spacedBy(6.dp, Alignment.CenterVertically),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Icon(item.icon, contentDescription = item.label, tint = color, modifier = Modifier.height(20.dp))
                Text(item.label, color = color, style = LevelChefTextStyles.captionBold)
            }
        }
    }
}
