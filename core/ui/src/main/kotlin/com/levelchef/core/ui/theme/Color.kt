package com.levelchef.core.ui.theme

import androidx.compose.ui.graphics.Color

// All values below are sourced directly from the Figma file's variables (dark = the original
// design; light = the user's duplicated-and-recolored "Base Components" frame, node 590:963).
// Where a token isn't bound to a Figma variable and no light-mode data exists (status colors,
// the "extended-components" frame — never recolored), the dark value is kept for both themes.

// Backgrounds — dark
val BackgroundPrimary = Color(0xFF0F0F1A)
val BackgroundSurface = Color(0xFF1A1A2E)

// Backgrounds — light
val BackgroundPrimaryLight = Color(0xFFFFFFFF)
val BackgroundSurfaceLight = Color(0xFFF5F5FA)

// Accent (purple) — accent/primary itself shifts hue between themes
val AccentPrimary = Color(0xFF534AB7)
val AccentPrimaryLight = Color(0xFF6157C7)

// AccentText: the Tag component's stroke role (Figma "tag/store"), confirmed identical in both
// themes' variable dumps — NOT the same as the themed caption-text role below.
val AccentText = Color(0xFFA78BFA)

// Text — dark
val TextPrimary = Color(0xFFFFFFFF)
val TextSecondary = Color(0xFFB2B2BF)

// Text — light
val TextPrimaryLight = Color(0xFF1A1A26)
val TextSecondaryLight = Color(0xFF666673)

// Text/icons drawn on top of a filled accent-colored shape — invariant white in both themes
// (Figma "text/on-accent").
val OnAccent = Color(0xFFFFFFFF)

// Borders
val BorderDefault = Color(0xFF424266)
val BorderDefaultLight = Color(0xFFD9D9E5)

// Status — not variable-bound in Figma in either theme; kept invariant
val SuccessGreen = Color(0xFF68D391)
val WarningYellow = Color(0xFFF6AD55)

// Destructive actions — confirmed invariant (accent/destructive identical in both dumps)
val DestructiveRed = Color(0xFFC81C1C)

// Weekly Challenge Card's "WEEKLY CHALLENGE" caption — confirmed a hardcoded Figma literal,
// unchanged between the original and the light duplicate. Also reused as light theme's
// tag-purple text color (Figma coincidentally reuses the same violet for both).
val Violet600 = Color(0xFF7C3AED)

// Badge "Style=Light" (Figma accent/badge-bg + accent/badge-text) — dark is a solid accent fill;
// light is a soft/tonal fill, a genuine per-theme redesign, not a palette swap.
val BadgePrimaryBgLight = Color(0xFFEEF2FF)
val BadgePrimaryTextLight = Color(0xFF4F46E5)

// Badge "Style=Dark" (Figma accent/badge-bg-light + accent/badge-text-light) — bg flips
// white(dark theme) -> pale lavender(light theme); text is invariant.
val BadgeInverseBgLight = Color(0xFFD7D2FF)
val BadgeInverseText = Color(0xFF4F46E5)

// Tag colors — fill hue, at the alpha the unselected/selected tag variants use
val TagPurpleBase = Color(0xFF8B5CF6)
val TagPurpleBgLight = Color(0xFFEAE2FD) // solid in light theme, vs. a translucent wash in dark
val TagGreenBg = Color(0x3326B266)
val TagGreenStroke = Color(0xFF26B266)
val TagGreenTextLight = Color(0xFF0D7333)
val TagYellowBg = Color(0x33E5BF26)
val TagYellowStroke = Color(0xFFE5BF26)
val TagYellowTextLight = Color(0xFF8C730D)
val TagRedBg = Color(0x33F24D4D)
val TagRedStroke = Color(0xFFF24D4D)
val TagRedTextLight = Color(0xFFA61F1F)
