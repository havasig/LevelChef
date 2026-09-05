package com.levelchef.core.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/**
 * The theme-flipping colors — everything else (accent status colors, tag strokes, on-accent
 * white, …) is invariant between light and dark and stays a plain top-level [Color] constant.
 * Values are sourced from the Figma file's variables — see [Color.kt] for provenance.
 */
data class LevelChefColors(
    val background: Color,
    val surface: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val border: Color,
    val accentPrimary: Color,
    val badgePrimaryBg: Color,
    val badgePrimaryText: Color,
    val badgeInverseBg: Color,
    val tagPurpleBg: Color,
    val tagPurpleText: Color,
    val tagGreenText: Color,
    val tagRedText: Color,
    val tagYellowText: Color,
    val pageIndicatorInactive: Color,
)

val LevelChefDarkColors = LevelChefColors(
    background = BackgroundPrimary,
    surface = BackgroundSurface,
    textPrimary = TextPrimary,
    textSecondary = TextSecondary,
    border = BorderDefault,
    accentPrimary = AccentPrimary,
    badgePrimaryBg = AccentPrimary,
    badgePrimaryText = OnAccent,
    badgeInverseBg = OnAccent,
    tagPurpleBg = TagPurpleBase.copy(alpha = 0.2f),
    tagPurpleText = TextPrimary.copy(alpha = 0.8f),
    tagGreenText = TextPrimary.copy(alpha = 0.8f),
    tagRedText = TextPrimary.copy(alpha = 0.8f),
    tagYellowText = TextPrimary.copy(alpha = 0.8f),
    pageIndicatorInactive = Color.White.copy(alpha = 0.1f),
)

val LevelChefLightColors = LevelChefColors(
    background = BackgroundPrimaryLight,
    surface = BackgroundSurfaceLight,
    textPrimary = TextPrimaryLight,
    textSecondary = TextSecondaryLight,
    border = BorderDefaultLight,
    accentPrimary = AccentPrimaryLight,
    badgePrimaryBg = BadgePrimaryBgLight,
    badgePrimaryText = BadgePrimaryTextLight,
    badgeInverseBg = BadgeInverseBgLight,
    tagPurpleBg = TagPurpleBgLight,
    tagPurpleText = Violet600,
    tagGreenText = TagGreenTextLight,
    tagRedText = TagRedTextLight,
    tagYellowText = TagYellowTextLight,
    pageIndicatorInactive = Color.Black.copy(alpha = 0.1f),
)

val LocalLevelChefColors = staticCompositionLocalOf { LevelChefDarkColors }

private fun levelChefColorScheme(colors: LevelChefColors, darkTheme: Boolean): ColorScheme =
    if (darkTheme) {
        darkColorScheme(
            primary = colors.accentPrimary,
            onPrimary = OnAccent,
            background = colors.background,
            onBackground = colors.textPrimary,
            surface = colors.surface,
            onSurface = colors.textPrimary,
            onSurfaceVariant = colors.textSecondary,
            outline = colors.border,
            error = TagRedStroke,
        )
    } else {
        lightColorScheme(
            primary = colors.accentPrimary,
            onPrimary = OnAccent,
            background = colors.background,
            onBackground = colors.textPrimary,
            surface = colors.surface,
            onSurface = colors.textPrimary,
            onSurfaceVariant = colors.textSecondary,
            outline = colors.border,
            error = TagRedStroke,
        )
    }

val LevelChefShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(12.dp),
    large = RoundedCornerShape(16.dp),
    extraLarge = RoundedCornerShape(20.dp),
)

/**
 * Flat design, no gradients/shadows, 0.5px borders, 12px card radius — see the design spec.
 * Ships dark-only (`MainActivity` forces `darkTheme = true`); `darkTheme` exists so previews and
 * tests can render both, per the Figma light-mode duplicate.
 */
@Composable
fun LevelChefTheme(darkTheme: Boolean = isSystemInDarkTheme(), content: @Composable () -> Unit) {
    val colors = if (darkTheme) LevelChefDarkColors else LevelChefLightColors
    CompositionLocalProvider(LocalLevelChefColors provides colors) {
        MaterialTheme(
            colorScheme = levelChefColorScheme(colors, darkTheme),
            typography = LevelChefTypography,
            shapes = LevelChefShapes,
            content = content,
        )
    }
}

/** Mirrors how `androidx.compose.material3.MaterialTheme` itself is declared (a function and an
 * object of the same name) — `LevelChefTheme.colors` reads the semantic tokens above. */
object LevelChefTheme {
    val colors: LevelChefColors @Composable get() = LocalLevelChefColors.current
}
