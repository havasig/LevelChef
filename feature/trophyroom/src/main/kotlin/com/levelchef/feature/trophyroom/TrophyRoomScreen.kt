package com.levelchef.feature.trophyroom

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.levelchef.core.designsystem.LevelChefPreview
import com.levelchef.core.designsystem.PlaceholderScreen
import com.levelchef.core.ui.theme.LevelChefTheme

/** Figma node 504:1026. */
@Composable
fun TrophyRoomScreen() = PlaceholderScreen(stringResource(R.string.trophy_room_title))

@LevelChefPreview
@Composable
private fun TrophyRoomScreenPreview() {
    LevelChefTheme { TrophyRoomScreen() }
}
