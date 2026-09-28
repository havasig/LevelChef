package com.levelchef.feature.settings.di

import com.levelchef.feature.settings.AndroidAppSettingsController
import com.levelchef.feature.settings.AppSettingsController
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val settingsModule = module {
    includes(settingsCommonModule)
    single<AppSettingsController> { AndroidAppSettingsController(androidContext()) }
}
