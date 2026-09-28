package com.levelchef.core.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.levelchef.core.ui.generated.resources.Res
import com.levelchef.core.ui.generated.resources.inter_variable
import org.jetbrains.compose.resources.Font

/**
 * The Figma-specced typeface, loaded from the variable font at
 * `commonMain/composeResources/font/inter_variable.ttf` (see `THIRD_PARTY_NOTICES.md`). Compose
 * Multiplatform's resource-backed [Font] loader is `@Composable` — unlike Android's old
 * resource-id overload this replaces — so this, and everything built from it
 * ([LevelChefTextStylesHolder], [levelChefTypography]), is computed once in [LevelChefTheme] and
 * handed down via [LocalLevelChefTextStyles], the same pattern [LocalLevelChefColors] already uses
 * for colors. Each weight is registered separately (Compose derives the matching variation-axis
 * settings from [FontWeight] automatically); iOS/desktop don't yet honor a variable font's weight
 * axis the way Android does (JetBrains/compose-multiplatform#3127), so all three may render at the
 * font's default weight there until upstream support lands — verify visually once Stage F's iOS
 * shell exists.
 */
@Composable
internal fun rememberInterFontFamily(): FontFamily {
    val regular = Font(Res.font.inter_variable, weight = FontWeight.Normal)
    val semiBold = Font(Res.font.inter_variable, weight = FontWeight.SemiBold)
    val bold = Font(Res.font.inter_variable, weight = FontWeight.Bold)
    return remember(regular, semiBold, bold) { FontFamily(regular, semiBold, bold) }
}

/**
 * The Figma "Text Styles" ramp (node 79:109), named and specced exactly as Figma has them.
 * Material3's [Typography] only exposes 8 slots and has no "Bold" variant of a given size, so
 * this is the source of truth — reach for these directly (`LevelChefTextStyles.x`, inside any
 * `LevelChefTheme` content) when a component needs a style [levelChefTypography] doesn't cover
 * (e.g. [bodyLargeBold], [captionRegular]).
 */
class LevelChefTextStylesHolder internal constructor(inter: FontFamily) {
    val h1 = TextStyle(fontFamily = inter, fontWeight = FontWeight.Bold, fontSize = 32.sp, lineHeight = 40.sp)
    val h2 = TextStyle(fontFamily = inter, fontWeight = FontWeight.Bold, fontSize = 24.sp, lineHeight = 32.sp)
    val bodyLarge = TextStyle(fontFamily = inter, fontWeight = FontWeight.Normal, fontSize = 18.sp, lineHeight = 28.sp)
    val bodyLargeBold = TextStyle(fontFamily = inter, fontWeight = FontWeight.SemiBold, fontSize = 18.sp, lineHeight = 28.sp)
    val bodyRegular = TextStyle(fontFamily = inter, fontWeight = FontWeight.Normal, fontSize = 16.sp, lineHeight = 24.sp)
    val bodyRegularBold = TextStyle(fontFamily = inter, fontWeight = FontWeight.SemiBold, fontSize = 16.sp, lineHeight = 24.sp)
    val bodySmall = TextStyle(fontFamily = inter, fontWeight = FontWeight.Normal, fontSize = 14.sp, lineHeight = 20.sp)
    val bodySmallBold = TextStyle(fontFamily = inter, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, lineHeight = 20.sp)
    val captionRegular = TextStyle(fontFamily = inter, fontWeight = FontWeight.Normal, fontSize = 12.sp, lineHeight = 16.sp)
    val captionBold = TextStyle(fontFamily = inter, fontWeight = FontWeight.SemiBold, fontSize = 12.sp, lineHeight = 16.sp)
}

val LocalLevelChefTextStyles = staticCompositionLocalOf<LevelChefTextStylesHolder> {
    error("LevelChefTextStyles read outside LevelChefTheme")
}

/**
 * `LevelChefTextStyles.h1` etc. — unchanged call syntax for every existing call site (Kotlin
 * property-access syntax doesn't differ for a `@Composable get()`); only the declaration changed
 * from a plain `object` to composition-local-backed, since building the underlying [FontFamily]
 * needs composable context on Compose Multiplatform. Every call site already runs inside a
 * `LevelChefTheme { … }` subtree, so [LocalLevelChefTextStyles] is always populated in practice.
 */
val LevelChefTextStyles: LevelChefTextStylesHolder
    @Composable get() = LocalLevelChefTextStyles.current

// Material3's 8 typography slots, re-derived from a [LevelChefTextStylesHolder] so existing
// MaterialTheme.typography.* call sites keep working unchanged.
internal fun levelChefTypography(textStyles: LevelChefTextStylesHolder): Typography = Typography(
    headlineLarge = textStyles.h1,
    headlineMedium = textStyles.h2,
    bodyLarge = textStyles.bodyLarge,
    bodyMedium = textStyles.bodyRegular,
    bodySmall = textStyles.bodySmall,
    labelLarge = textStyles.bodyRegularBold,
    labelMedium = textStyles.bodySmallBold,
    labelSmall = textStyles.captionBold,
)
