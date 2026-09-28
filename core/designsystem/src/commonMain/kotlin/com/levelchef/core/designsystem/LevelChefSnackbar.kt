package com.levelchef.core.designsystem

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.levelchef.core.ui.theme.LevelChefTextStyles
import com.levelchef.core.ui.theme.LevelChefTheme

/** The Snackbar extended component (Figma node 251:972) — a presentational message bar. */
@Composable
fun LevelChefSnackbar(message: String, modifier: Modifier = Modifier) {
    val colors = LevelChefTheme.colors
    Text(
        text = message,
        color = colors.textPrimary,
        style = LevelChefTextStyles.bodyRegular,
        modifier = modifier
            .fillMaxWidth()
            .background(colors.surface, RoundedCornerShape(12.dp))
            .border(1.dp, colors.accentPrimary, RoundedCornerShape(12.dp))
            .padding(horizontal = 16.dp, vertical = 14.dp),
    )
}

@LevelChefPreview
@Composable
private fun LevelChefSnackbarPreview() {
    LevelChefTheme { LevelChefSnackbar("Your profile has been updated.") }
}
