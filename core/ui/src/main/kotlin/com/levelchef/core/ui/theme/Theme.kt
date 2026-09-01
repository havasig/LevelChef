package com.levelchef.core.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp

// LevelChef is dark-theme-only, flat design: no gradients, no shadows,
// 0.5px borders, 12px corner radius on cards per the design spec.
private val LevelChefColorScheme = darkColorScheme(
    primary = AccentPrimary,
    onPrimary = TextPrimary,
    background = BackgroundPrimary,
    onBackground = TextPrimary,
    surface = BackgroundSurface,
    onSurface = TextPrimary,
    onSurfaceVariant = TextSecondary,
    outline = BorderDefault,
    error = TagRedStroke,
)

val LevelChefShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(12.dp),
    large = RoundedCornerShape(16.dp),
    extraLarge = RoundedCornerShape(20.dp),
)

@Composable
fun LevelChefTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = LevelChefColorScheme,
        typography = LevelChefTypography,
        shapes = LevelChefShapes,
        content = content,
    )
}
