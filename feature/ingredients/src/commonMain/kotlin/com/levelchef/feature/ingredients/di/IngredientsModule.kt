package com.levelchef.feature.ingredients.di

import com.levelchef.domain.usecase.DeleteIngredientUseCase
import com.levelchef.domain.usecase.SaveIngredientUseCase
import com.levelchef.feature.ingredients.IngredientDetailViewModel
import com.levelchef.feature.ingredients.IngredientFormViewModel
import com.levelchef.feature.ingredients.IngredientsListViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val ingredientsModule = module {
    factory { SaveIngredientUseCase(get()) }
    factory { DeleteIngredientUseCase(get()) }
    viewModel { IngredientsListViewModel(get()) }
    viewModel { params -> IngredientDetailViewModel(params.get(), get(), get()) }
    viewModel { params -> IngredientFormViewModel(params.getOrNull(), get(), get()) }
}
