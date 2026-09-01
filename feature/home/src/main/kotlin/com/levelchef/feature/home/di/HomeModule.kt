package com.levelchef.feature.home.di

import com.levelchef.domain.usecase.GetChefLevelUseCase
import com.levelchef.feature.home.HomeViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val homeModule = module {
    factory { GetChefLevelUseCase(get()) }
    viewModel { HomeViewModel(get(), get(), get(), get()) }
}
