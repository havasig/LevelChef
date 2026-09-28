package com.levelchef.feature.settings.di

import com.levelchef.domain.usecase.ClearSurveyResponseUseCase
import com.levelchef.domain.usecase.DeleteAccountDataUseCase
import com.levelchef.feature.settings.SettingsViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

/** Platform-independent bindings shared by each platform's `settingsModule` (see `androidMain`/
 * `iosMain`), which each `includes` this and adds their own `AppSettingsController` binding —
 * that one needs a platform-specific implementation (Koin's `androidContext()` on Android, an
 * in-memory stub on iOS for now), so it can't live here. */
internal val settingsCommonModule = module {
    factory { ClearSurveyResponseUseCase(get()) }
    factory { DeleteAccountDataUseCase(get(), get(), get(), get(), get()) }
    viewModel { SettingsViewModel(get(), get(), get(), get()) }
}
