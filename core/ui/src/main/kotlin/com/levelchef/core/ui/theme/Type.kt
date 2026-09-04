package com.levelchef.core.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// Inter is used in the design; falls back to the platform default font family
// unless an Inter font resource is added under res/font.

/**
 * The Figma "Text Styles" ramp (node 79:109), named and specced exactly as Figma has them.
 * Material3's [Typography] only exposes 8 slots and has no "Bold" variant of a given size, so
 * this is the source of truth — reach for these directly when a component needs a style
 * [LevelChefTypography] doesn't cover (e.g. [bodyLargeBold], [captionRegular]).
 */
object LevelChefTextStyles {
    val h1 = TextStyle(fontWeight = FontWeight.Bold, fontSize = 32.sp, lineHeight = 40.sp)
    val h2 = TextStyle(fontWeight = FontWeight.Bold, fontSize = 24.sp, lineHeight = 32.sp)
    val bodyLarge = TextStyle(fontWeight = FontWeight.Normal, fontSize = 18.sp, lineHeight = 28.sp)
    val bodyLargeBold = TextStyle(fontWeight = FontWeight.SemiBold, fontSize = 18.sp, lineHeight = 28.sp)
    val bodyRegular = TextStyle(fontWeight = FontWeight.Normal, fontSize = 16.sp, lineHeight = 24.sp)
    val bodyRegularBold = TextStyle(fontWeight = FontWeight.SemiBold, fontSize = 16.sp, lineHeight = 24.sp)
    val bodySmall = TextStyle(fontWeight = FontWeight.Normal, fontSize = 14.sp, lineHeight = 20.sp)
    val bodySmallBold = TextStyle(fontWeight = FontWeight.SemiBold, fontSize = 14.sp, lineHeight = 20.sp)
    val captionRegular = TextStyle(fontWeight = FontWeight.Normal, fontSize = 12.sp, lineHeight = 16.sp)
    val captionBold = TextStyle(fontWeight = FontWeight.SemiBold, fontSize = 12.sp, lineHeight = 16.sp)
}

// Material3's 8 typography slots, re-derived from LevelChefTextStyles so existing
// MaterialTheme.typography.* call sites keep working unchanged.
val LevelChefTypography = Typography(
    headlineLarge = LevelChefTextStyles.h1,
    headlineMedium = LevelChefTextStyles.h2,
    bodyLarge = LevelChefTextStyles.bodyLarge,
    bodyMedium = LevelChefTextStyles.bodyRegular,
    bodySmall = LevelChefTextStyles.bodySmall,
    labelLarge = LevelChefTextStyles.bodyRegularBold,
    labelMedium = LevelChefTextStyles.bodySmallBold,
    labelSmall = LevelChefTextStyles.captionBold,
)
