package com.levelchef.android

import android.app.Application
import com.levelchef.data.di.dataModule
import com.levelchef.data.di.databaseModule
import com.levelchef.feature.home.di.homeModule
import com.levelchef.feature.onboarding.di.onboardingModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class LevelChefApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@LevelChefApplication)
            modules(databaseModule, dataModule, homeModule, onboardingModule)
        }
    }
}
