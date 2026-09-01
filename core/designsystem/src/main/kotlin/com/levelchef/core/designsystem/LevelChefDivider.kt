package com.levelchef.core.designsystem

import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.levelchef.core.ui.theme.BorderDefault

/** 0.5px hairline divider, per the flat design spec (no shadows/gradients). */
@Composable
fun LevelChefDivider(modifier: Modifier = Modifier) {
    HorizontalDivider(modifier = modifier, thickness = 0.5.dp, color = BorderDefault)
}
