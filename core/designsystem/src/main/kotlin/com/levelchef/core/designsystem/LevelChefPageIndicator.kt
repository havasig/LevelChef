package com.levelchef.core.designsystem

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.levelchef.core.ui.theme.LevelChefTheme

/** The Page Indicator base component (Figma node 18:2030) — dots, active one wider. */
@Composable
fun LevelChefPageIndicator(pageCount: Int, currentPage: Int, modifier: Modifier = Modifier) {
    val colors = LevelChefTheme.colors
    Row(modifier = modifier, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        repeat(pageCount) { index ->
            val active = index == currentPage
            Box(
                modifier = Modifier
                    .height(6.dp)
                    .width(if (active) 28.dp else 8.dp)
                    .background(
                        if (active) colors.accentPrimary else colors.pageIndicatorInactive,
                        RoundedCornerShape(3.dp),
                    ),
            )
        }
    }
}

@LevelChefPreview
@Composable
private fun LevelChefPageIndicatorPreview() {
    LevelChefTheme { LevelChefPageIndicator(pageCount = 4, currentPage = 1) }
}
