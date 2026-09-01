package com.levelchef.core.designsystem

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun LevelChefTag(emoji: String, label: String, colors: TagColors, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.background(colors.background, RoundedCornerShape(20.dp)),
    ) {
        Text(
            text = "$emoji $label",
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
            style = MaterialTheme.typography.bodySmall,
        )
    }
}
