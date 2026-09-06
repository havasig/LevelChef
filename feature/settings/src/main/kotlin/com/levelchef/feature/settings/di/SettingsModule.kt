package com.levelchef.feature.settings.di

import com.levelchef.domain.usecase.ClearSurveyResponseUseCase
import com.levelchef.domain.usecase.DeleteAccountDataUseCase
import com.levelchef.feature.settings.AndroidAppSettingsController
import com.levelchef.feature.settings.AppSettingsController
import com.levelchef.feature.settings.SettingsViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val settingsModule = module {
    single<AppSettingsController> { AndroidAppSettingsController(androidContext()) }
    factory { ClearSurveyResponseUseCase(get()) }
    factory { DeleteAccountDataUseCase(get(), get()) }
    viewModel { SettingsViewModel(get(), get(), get(), get()) }
}
