package com.levelchef.feature.mealreview.di

import com.levelchef.domain.usecase.RecordCookingSessionUseCase
import com.levelchef.feature.mealreview.MealReviewViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val mealReviewModule = module {
    factory { RecordCookingSessionUseCase(get()) }
    viewModel { params -> MealReviewViewModel(params.get(), get(), get()) }
}
