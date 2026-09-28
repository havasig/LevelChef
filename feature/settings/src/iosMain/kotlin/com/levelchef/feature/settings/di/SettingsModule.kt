package com.levelchef.feature.settings.di

import com.levelchef.feature.settings.AppSettingsController
import com.levelchef.feature.settings.IosAppSettingsController
import org.koin.dsl.module

val settingsModule = module {
    includes(settingsCommonModule)
    single<AppSettingsController> { IosAppSettingsController() }
}
