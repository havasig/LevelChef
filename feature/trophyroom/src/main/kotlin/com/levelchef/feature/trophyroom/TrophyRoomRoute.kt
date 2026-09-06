package com.levelchef.feature.trophyroom

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import org.koin.compose.viewmodel.koinViewModel

/** Stateful entry point: collects [TrophyRoomViewModel]'s state and hands it to the stateless
 * [TrophyRoomScreen]. */
@Composable
fun TrophyRoomRoute(viewModel: TrophyRoomViewModel = koinViewModel()) {
    val state by viewModel.uiState.collectAsState()
    TrophyRoomScreen(state = state)
}
