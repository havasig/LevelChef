package com.levelchef.core.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.levelchef.core.ui.R

/** The Figma-specced typeface, loaded from the variable font at `res/font/inter_variable.ttf`
 * (see `THIRD_PARTY_NOTICES.md`); each weight below selects an instance on its `wght` axis. */
private val Inter = FontFamily(
    Font(R.font.inter_variable, weight = FontWeight.Normal, variationSettings = FontVariation.Settings(FontVariation.weight(400))),
    Font(R.font.inter_variable, weight = FontWeight.SemiBold, variationSettings = FontVariation.Settings(FontVariation.weight(600))),
    Font(R.font.inter_variable, weight = FontWeight.Bold, variationSettings = FontVariation.Settings(FontVariation.weight(700))),
)

/**
 * The Figma "Text Styles" ramp (node 79:109), named and specced exactly as Figma has them.
 * Material3's [Typography] only exposes 8 slots and has no "Bold" variant of a given size, so
 * this is the source of truth — reach for these directly when a component needs a style
 * [LevelChefTypography] doesn't cover (e.g. [bodyLargeBold], [captionRegular]).
 */
object LevelChefTextStyles {
    val h1 = TextStyle(fontFamily = Inter, fontWeight = FontWeight.Bold, fontSize = 32.sp, lineHeight = 40.sp)
    val h2 = TextStyle(fontFamily = Inter, fontWeight = FontWeight.Bold, fontSize = 24.sp, lineHeight = 32.sp)
    val bodyLarge = TextStyle(fontFamily = Inter, fontWeight = FontWeight.Normal, fontSize = 18.sp, lineHeight = 28.sp)
    val bodyLargeBold = TextStyle(fontFamily = Inter, fontWeight = FontWeight.SemiBold, fontSize = 18.sp, lineHeight = 28.sp)
    val bodyRegular = TextStyle(fontFamily = Inter, fontWeight = FontWeight.Normal, fontSize = 16.sp, lineHeight = 24.sp)
    val bodyRegularBold = TextStyle(fontFamily = Inter, fontWeight = FontWeight.SemiBold, fontSize = 16.sp, lineHeight = 24.sp)
    val bodySmall = TextStyle(fontFamily = Inter, fontWeight = FontWeight.Normal, fontSize = 14.sp, lineHeight = 20.sp)
    val bodySmallBold = TextStyle(fontFamily = Inter, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, lineHeight = 20.sp)
    val captionRegular = TextStyle(fontFamily = Inter, fontWeight = FontWeight.Normal, fontSize = 12.sp, lineHeight = 16.sp)
    val captionBold = TextStyle(fontFamily = Inter, fontWeight = FontWeight.SemiBold, fontSize = 12.sp, lineHeight = 16.sp)
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
