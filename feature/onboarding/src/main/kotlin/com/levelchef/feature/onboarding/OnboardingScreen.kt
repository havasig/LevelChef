package com.levelchef.feature.onboarding

import androidx.compose.runtime.Composable
import com.levelchef.core.designsystem.LevelChefPreview
import com.levelchef.core.designsystem.PlaceholderScreen
import com.levelchef.core.ui.theme.LevelChefTheme

/** Figma nodes 296:1679 / 296:1678 / 296:1729 / 465:645 / 480:631 (4 steps). */
@Composable
fun OnboardingScreen() = PlaceholderScreen("Onboarding")

@LevelChefPreview
@Composable
private fun OnboardingScreenPreview() {
    LevelChefTheme { OnboardingScreen() }
}
