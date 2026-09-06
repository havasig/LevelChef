package com.levelchef.feature.trophyroom.di

import com.levelchef.domain.usecase.GetChefLevelUseCase
import com.levelchef.feature.trophyroom.TrophyRoomViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val trophyroomModule = module {
    factory { GetChefLevelUseCase(get()) }
    viewModel { TrophyRoomViewModel(get(), get(), get(), get()) }
}
