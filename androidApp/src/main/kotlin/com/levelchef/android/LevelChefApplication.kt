package com.levelchef.android

import android.app.Application
import co.touchlab.kermit.Logger
import co.touchlab.kermit.Severity
import co.touchlab.kermit.platformLogWriter
import com.levelchef.android.logging.CrashLogWriter
import com.levelchef.android.logging.installGlobalExceptionHandler
import com.levelchef.data.di.dataModule
import com.levelchef.data.di.databaseModule
import com.levelchef.domain.repository.IngredientRepository
import com.levelchef.feature.home.di.homeModule
import com.levelchef.feature.ingredients.di.ingredientsModule
import com.levelchef.feature.onboarding.di.onboardingModule
import com.levelchef.feature.recipedetail.di.recipeDetailModule
import com.levelchef.feature.settings.AppSettingsController
import com.levelchef.feature.settings.di.settingsModule
import com.levelchef.feature.trophyroom.di.trophyroomModule
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class LevelChefApplication : Application() {

    private val appScope = CoroutineScope(
        SupervisorJob() + Dispatchers.Default +
            CoroutineExceptionHandler { _, throwable ->
                Logger.e(throwable) { "Uncaught exception in the application coroutine scope" }
            },
    )
    private val appSettingsController: AppSettingsController by inject()
    private val ingredientRepository: IngredientRepository by inject()

    override fun onCreate() {
        super.onCreate()
        Logger.setTag("LevelChef")
        Logger.setLogWriters(platformLogWriter(), CrashLogWriter())
        if (!BuildConfig.DEBUG) {
            Logger.setMinSeverity(Severity.Warn)
        }
        installGlobalExceptionHandler()
        Logger.i { "LevelChef ${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE}) starting" }

        startKoin {
            androidContext(this@LevelChefApplication)
            modules(
                databaseModule,
                dataModule,
                homeModule,
                onboardingModule,
                settingsModule,
                ingredientsModule,
                trophyroomModule,
                recipeDetailModule,
            )
        }
        appSettingsController.applyPersistedThemeMode()
        appScope.launch { ingredientRepository.seedDefaults() }
    }
}
