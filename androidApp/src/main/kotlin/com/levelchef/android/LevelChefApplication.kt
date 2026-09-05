package com.levelchef.android

import android.app.Application
import com.levelchef.data.di.dataModule
import com.levelchef.data.di.databaseModule
import com.levelchef.feature.home.di.homeModule
import com.levelchef.feature.onboarding.di.onboardingModule
import com.levelchef.feature.settings.AppSettingsController
import com.levelchef.feature.settings.di.settingsModule
import org.koin.android.ext.android.inject
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class LevelChefApplication : Application() {

    private val appSettingsController: AppSettingsController by inject()

    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@LevelChefApplication)
            modules(databaseModule, dataModule, homeModule, onboardingModule, settingsModule)
        }
        // Re-apply the saved light/dark choice before the first Activity is created.
        appSettingsController.applyPersistedThemeMode()
    }
}
