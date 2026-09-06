package com.levelchef.feature.recipedetail.di

import com.levelchef.domain.usecase.RecordCookingSessionUseCase
import com.levelchef.feature.recipedetail.RecipeDetailViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val recipeDetailModule = module {
    factory { RecordCookingSessionUseCase(get()) }
    viewModel { params -> RecipeDetailViewModel(params.get(), get(), get(), get()) }
}
