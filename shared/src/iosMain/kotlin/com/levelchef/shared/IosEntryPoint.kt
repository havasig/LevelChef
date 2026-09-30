package com.levelchef.shared

import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.window.ComposeUIViewController
import com.levelchef.core.ui.theme.LevelChefTheme
import com.levelchef.data.di.dataModule
import com.levelchef.data.di.databaseModule
import com.levelchef.domain.repository.IngredientRepository
import com.levelchef.feature.cookinglog.di.cookingLogModule
import com.levelchef.feature.home.di.homeModule
import com.levelchef.feature.ingredients.di.ingredientsModule
import com.levelchef.feature.mealreview.di.mealReviewModule
import com.levelchef.feature.onboarding.OnboardingGate
import com.levelchef.feature.onboarding.di.onboardingModule
import com.levelchef.feature.recipedetail.di.recipeDetailModule
import com.levelchef.feature.settings.AppSettingsController
import com.levelchef.feature.settings.IosThemeBridge
import com.levelchef.feature.settings.di.settingsModule
import com.levelchef.feature.trophyroom.di.trophyroomModule
import com.levelchef.shared.nav.SharedApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.core.context.startKoin
import org.koin.core.qualifier.named
import org.koin.dsl.module
import platform.UIKit.UIViewController

private val appScope = CoroutineScope(Dispatchers.Default)

/**
 * iOS entry point, called once from `iOSApp.swift`'s `init()` — mirrors `LevelChefApplication`'s
 * `startKoin` call on Android. `geminiApiKey` is blank until a real key is plumbed in from Xcode
 * build settings/Info.plist (see AGENTS.md's iOS "Not yet done" item); `RecipeRepositoryImpl`
 * already treats a blank key as "serve the bundled fallback recipes", so this never fails.
 */
fun doInitKoin() {
    val koinApp = startKoin {
        modules(
            module { single(named("geminiApiKey")) { "" } },
            databaseModule,
            dataModule,
            homeModule,
            onboardingModule,
            settingsModule,
            ingredientsModule,
            trophyroomModule,
            recipeDetailModule,
            mealReviewModule,
            cookingLogModule,
        )
    }
    // Seeds IosThemeBridge from the persisted theme choice — mirrors LevelChefApplication.onCreate()
    // calling appSettingsController.applyPersistedThemeMode() right after startKoin on Android.
    koinApp.koin.get<AppSettingsController>().applyPersistedThemeMode()
    // Mirrors LevelChefApplication.onCreate()'s appScope.launch { ingredientRepository.seedDefaults() }
    // — without it the starter pantry never existed on iOS, leaving the Ingredients screen empty.
    appScope.launch { koinApp.koin.get<IngredientRepository>().seedDefaults() }
}

/**
 * Called from `ContentView.swift` via a `UIViewControllerRepresentable`. Gates the full bottom-nav
 * graph (`SharedApp`) behind the mandatory first-launch survey (`OnboardingGate`), same as
 * androidApp's own `LevelChefApp()` — everything reachable from Home except the Android-only debug
 * showcase. `darkTheme` is read explicitly from [IosThemeBridge] rather than left to
 * `LevelChefTheme`'s `isSystemInDarkTheme()` default — see that bridge's doc comment for why.
 */
fun MainViewController(): UIViewController = ComposeUIViewController {
    val isDark by IosThemeBridge.effectiveDarkTheme.collectAsState(initial = false)
    LevelChefTheme(darkTheme = isDark) {
        OnboardingGate {
            SharedApp()
        }
    }
}
