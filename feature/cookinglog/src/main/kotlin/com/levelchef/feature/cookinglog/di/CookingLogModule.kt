package com.levelchef.feature.cookinglog.di

import com.levelchef.feature.cookinglog.CookingLogViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val cookingLogModule = module {
    viewModel { CookingLogViewModel(get(), get(), get()) }
}
