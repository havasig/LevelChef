package com.levelchef.core.designsystem

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.levelchef.core.ui.theme.LevelChefTextStyles
import com.levelchef.core.ui.theme.LevelChefTheme

/** The Bottom Navigation Bar extended component (Figma node 251:1007). */
@Composable
fun LevelChefBottomNavigationBar(items: List<LevelChefNavItem>, modifier: Modifier = Modifier) {
    val colors = LevelChefTheme.colors
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(64.dp)
            .background(colors.surface, RoundedCornerShape(bottomStart = 16.dp, bottomEnd = 16.dp)),
    ) {
        items.forEach { item ->
            val color = if (item.selected) colors.accentPrimary else colors.textSecondary
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .clickable(interactionSource = null, indication = null, onClick = item.onClick),
                verticalArrangement = Arrangement.spacedBy(6.dp, Alignment.CenterVertically),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Icon(item.icon, contentDescription = item.label, tint = color, modifier = Modifier.height(20.dp))
                Text(item.label, color = color, style = LevelChefTextStyles.captionBold)
            }
        }
    }
}

@LevelChefPreview
@Composable
private fun LevelChefBottomNavigationBarPreview() {
    var selected by remember { mutableIntStateOf(0) }
    val tabs = listOf(
        Triple("Home", Icons.Filled.Home, 0),
        Triple("Recipes", Icons.AutoMirrored.Filled.List, 1),
        Triple("Trophies", Icons.Filled.Star, 2),
    )
    LevelChefTheme {
        LevelChefBottomNavigationBar(
            items = tabs.map { (label, icon, index) ->
                LevelChefNavItem(
                    icon = icon,
                    label = label,
                    selected = index == selected,
                    onClick = { selected = index },
                )
            },
        )
    }
}
