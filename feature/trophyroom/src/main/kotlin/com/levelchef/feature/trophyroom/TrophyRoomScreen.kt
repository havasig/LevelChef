package com.levelchef.feature.trophyroom

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.levelchef.core.designsystem.LevelChefPreview
import com.levelchef.core.designsystem.LevelChefTopAppBarHome
import com.levelchef.core.ui.theme.LevelChefTheme

/** Figma node 504:1026 — chef-level header, weekly-challenge/kitchen-time stats, streaks and badges. */
@Composable
fun TrophyRoomScreen(state: TrophyRoomUiState = TrophyRoomUiState()) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(LevelChefTheme.colors.background),
    ) {
        LevelChefTopAppBarHome(
            title = stringResource(R.string.trophy_room_title),
            modifier = Modifier.statusBarsPadding(),
        )
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            item { ProfileCard(state) }
            item { TrophyStatCardsRow(state) }
            item { SectionHeader(stringResource(R.string.trophy_room_streaks_header)) }
            items(state.streakBadges) { StreakBadgeCard(it) }
            item { SectionHeader(stringResource(R.string.trophy_room_badges_header)) }
            items(state.badges) { BadgeCard(it) }
        }
    }
}

@LevelChefPreview
@Composable
private fun TrophyRoomScreenPreview() {
    LevelChefTheme { TrophyRoomScreen() }
}
