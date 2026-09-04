package com.levelchef.core.designsystem

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.levelchef.core.ui.theme.BackgroundPrimary
import com.levelchef.core.ui.theme.LevelChefTextStyles
import com.levelchef.core.ui.theme.TextPrimary

/** The "Top App Bar/Inner" extended component (Figma instance 371:673) — back arrow + centered title + settings. */
@Composable
fun LevelChefTopAppBarInner(
    title: String,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    onSettingsClick: (() -> Unit)? = null,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(BackgroundPrimary)
            .height(56.dp)
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
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
        Text(
            title,
            color = TextPrimary,
            style = LevelChefTextStyles.bodyRegularBold,
            textAlign = TextAlign.Center,
            modifier = Modifier.weight(1f),
        )
        if (onSettingsClick != null) {
            Icon(
                imageVector = Icons.Filled.Settings,
                contentDescription = "Settings",
                tint = TextPrimary,
                modifier = Modifier
                    .size(24.dp)
                    .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null, onClick = onSettingsClick),
            )
        } else {
            Spacer(modifier = Modifier.size(24.dp))
        }
    }
}
