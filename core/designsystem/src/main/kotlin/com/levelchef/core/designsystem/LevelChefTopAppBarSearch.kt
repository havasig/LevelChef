package com.levelchef.core.designsystem

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.levelchef.core.ui.theme.BackgroundPrimary
import com.levelchef.core.ui.theme.LevelChefTextStyles
import com.levelchef.core.ui.theme.TextPrimary

/** The "Top App Bar/Search" extended component (Figma instance 371:674) — back arrow + an inline search field. */
@Composable
fun LevelChefTopAppBarSearch(placeholder: String, onBackClick: () -> Unit, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(BackgroundPrimary)
            .height(56.dp)
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
            contentDescription = "Back",
            tint = TextPrimary,
            modifier = Modifier
                .size(24.dp)
                .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null, onClick = onBackClick),
        )
        Row(
            modifier = Modifier
                .weight(1f)
                .background(Color.White.copy(alpha = 0.08f), RoundedCornerShape(12.dp))
                .padding(horizontal = 12.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(Icons.Filled.Search, contentDescription = null, tint = Color.White.copy(alpha = 0.5f), modifier = Modifier.size(20.dp))
            Text(placeholder, color = Color.White.copy(alpha = 0.5f), style = LevelChefTextStyles.bodySmallBold)
        }
    }
}
