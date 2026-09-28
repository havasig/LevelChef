package com.levelchef.core.designsystem

import androidx.compose.ui.graphics.vector.ImageVector

/** One tab rendered by [LevelChefBottomNavigationBar]. */
data class LevelChefNavItem(
    val icon: ImageVector,
    val label: String,
    val selected: Boolean,
    val onClick: () -> Unit,
)
