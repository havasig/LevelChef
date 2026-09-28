package com.levelchef.core.designsystem

/**
 * Renders a `@Composable` preview twice — once per [com.levelchef.core.ui.theme.LevelChefTheme]
 * variant — mirroring Compose's own `@PreviewLightDark`. Wrap the preview body in
 * `LevelChefTheme { … }` without an explicit `darkTheme` argument so each configuration's
 * `isSystemInDarkTheme()` picks the matching palette.
 *
 * `expect`/`actual` rather than a shared definition: the real preview tooling annotations
 * (`android.content.res.Configuration`, `androidx.compose.ui.tooling.preview.Preview`) are
 * Android-only IDE tooling with no Compose Multiplatform equivalent yet — see the androidMain
 * actual. The iosMain actual is a no-op marker so every `@LevelChefPreview`-annotated composable
 * across the design system keeps compiling on both targets unchanged.
 */
expect annotation class LevelChefPreview()
