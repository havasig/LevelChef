package com.levelchef.core.designsystem

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.levelchef.core.ui.theme.AccentPrimary
import com.levelchef.core.ui.theme.BackgroundSurface
import com.levelchef.core.ui.theme.BorderDefault
import com.levelchef.core.ui.theme.LevelChefTextStyles
import com.levelchef.core.ui.theme.TextPrimary
import com.levelchef.core.ui.theme.TextSecondary

/** The Tab Bar extended component (Figma node 251:1016) — segmented tabs with an underline indicator. */
@Composable
fun LevelChefTabBar(tabs: List<String>, selectedIndex: Int, onTabSelected: (Int) -> Unit, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(48.dp)
            .background(BackgroundSurface)
            .border(0.5.dp, BorderDefault),
    ) {
        tabs.forEachIndexed { index, tab ->
            val selected = index == selectedIndex
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) { onTabSelected(index) },
                verticalArrangement = Arrangement.Bottom,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    tab,
                    color = if (selected) TextPrimary else TextSecondary,
                    style = LevelChefTextStyles.bodyRegularBold,
                    modifier = Modifier.padding(bottom = 12.dp),
                )
                if (selected) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(2.dp)
                            .background(AccentPrimary),
                    )
                }
            }
        }
    }
}
