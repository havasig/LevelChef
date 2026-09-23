package com.levelchef.feature.trophyroom

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import org.koin.compose.viewmodel.koinViewModel

/** Stateful entry point: collects [TrophyRoomViewModel]'s state and hands it to the stateless
 * [TrophyRoomScreen]. */
@Composable
fun TrophyRoomRoute(viewModel: TrophyRoomViewModel = koinViewModel()) {
    val state by viewModel.uiState.collectAsState()

    // The bottom nav restores this tab's retained ViewModel, so reload on every return — e.g. after
    // logging a cook, XP, streak and badge progress must reflect it.
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) { viewModel.refresh() }

    TrophyRoomScreen(state = state)
}
