package com.levelchef.feature.trophyroom

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.levelchef.core.designsystem.LevelChefPreview
import com.levelchef.core.designsystem.LevelChefTopAppBarHome
import com.levelchef.core.designsystem.PlaceholderScreen
import com.levelchef.core.ui.theme.LevelChefTheme

/** Figma node 504:1026. */
@Composable
fun TrophyRoomScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(LevelChefTheme.colors.background),
    ) {
        LevelChefTopAppBarHome(
            title = stringResource(R.string.trophy_room_title),
            modifier = Modifier.statusBarsPadding(),
        )
        PlaceholderScreen(stringResource(R.string.trophy_room_title))
    }
}

@LevelChefPreview
@Composable
private fun TrophyRoomScreenPreview() {
    LevelChefTheme { TrophyRoomScreen() }
}
