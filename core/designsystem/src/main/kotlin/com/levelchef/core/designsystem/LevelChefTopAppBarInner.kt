package com.levelchef.core.designsystem

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.levelchef.core.ui.theme.LevelChefTextStyles
import com.levelchef.core.ui.theme.LevelChefTheme

/**
 * The "Top App Bar/Inner" extended component (Figma instance 371:673) — back arrow + centered title
 * + an optional trailing action ([trailingIcon] / [onTrailingClick]); a spacer keeps the title
 * centred when there is no action.
 */
@Composable
fun LevelChefTopAppBarInner(
    title: String,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    trailingIcon: ImageVector? = null,
    trailingContentDescription: String? = null,
    onTrailingClick: (() -> Unit)? = null,
) {
    val colors = LevelChefTheme.colors
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(colors.background)
            .height(56.dp)
            .padding(horizontal = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        LevelChefIconButton(
            icon = Icons.AutoMirrored.Filled.ArrowBack,
            contentDescription = "Back",
            style = IconButtonStyle.PLAIN,
            onClick = onBackClick,
        )
        Text(
            title,
            color = colors.textPrimary,
            style = LevelChefTextStyles.bodyRegularBold,
            textAlign = TextAlign.Center,
            modifier = Modifier.weight(1f),
        )
        if (trailingIcon != null && onTrailingClick != null) {
            LevelChefIconButton(
                icon = trailingIcon,
                contentDescription = trailingContentDescription,
                style = IconButtonStyle.PLAIN,
                onClick = onTrailingClick,
            )
        } else {
            Spacer(modifier = Modifier.size(40.dp))
        }
    }
}

@LevelChefPreview
@Composable
private fun LevelChefTopAppBarInnerPreview() {
    LevelChefTheme {
        LevelChefTopAppBarInner(
            "Used Ingredients",
            onBackClick = {},
            trailingIcon = Icons.Filled.Add,
            trailingContentDescription = "Add",
            onTrailingClick = {},
        )
    }
}
