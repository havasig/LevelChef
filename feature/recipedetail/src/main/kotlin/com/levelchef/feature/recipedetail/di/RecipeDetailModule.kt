package com.levelchef.feature.recipedetail.di

import com.levelchef.feature.recipedetail.RecipeDetailViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val recipeDetailModule = module {
    viewModel { params -> RecipeDetailViewModel(params.get(), get(), get()) }
}
