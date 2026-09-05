package com.levelchef.core.designsystem

import android.content.res.Configuration
import androidx.compose.ui.tooling.preview.Preview

/**
 * Renders a `@Composable` preview twice — once per [LevelChefTheme] variant — mirroring Compose's
 * own `@PreviewLightDark`. Wrap the preview body in `LevelChefTheme { … }` without an explicit
 * `darkTheme` argument so each configuration's `isSystemInDarkTheme()` picks the matching palette.
 */
@Preview(name = "Light", uiMode = Configuration.UI_MODE_NIGHT_NO, showBackground = true, backgroundColor = 0xFFFFFFFF)
@Preview(name = "Dark", uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true, backgroundColor = 0xFF0F0F1A)
annotation class LevelChefPreview
