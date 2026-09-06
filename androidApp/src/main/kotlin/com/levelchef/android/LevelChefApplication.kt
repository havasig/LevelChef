package com.levelchef.android

import android.app.Application
import com.levelchef.data.di.dataModule
import com.levelchef.data.di.databaseModule
import com.levelchef.domain.repository.IngredientRepository
import com.levelchef.feature.home.di.homeModule
import com.levelchef.feature.ingredients.di.ingredientsModule
import com.levelchef.feature.onboarding.di.onboardingModule
import com.levelchef.feature.settings.AppSettingsController
import com.levelchef.feature.settings.di.settingsModule
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class LevelChefApplication : Application() {

    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val appSettingsController: AppSettingsController by inject()
    private val ingredientRepository: IngredientRepository by inject()

    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@LevelChefApplication)
            modules(databaseModule, dataModule, homeModule, onboardingModule, settingsModule, ingredientsModule)
        }
        appSettingsController.applyPersistedThemeMode()
        appScope.launch { ingredientRepository.seedDefaults() }
    }
}
