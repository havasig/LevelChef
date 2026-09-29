package com.levelchef.shared

import androidx.compose.ui.window.ComposeUIViewController
import com.levelchef.core.ui.theme.LevelChefTheme
import com.levelchef.data.di.dataModule
import com.levelchef.data.di.databaseModule
import com.levelchef.feature.cookinglog.di.cookingLogModule
import com.levelchef.feature.home.di.homeModule
import com.levelchef.feature.ingredients.di.ingredientsModule
import com.levelchef.feature.mealreview.di.mealReviewModule
import com.levelchef.feature.recipedetail.di.recipeDetailModule
import com.levelchef.feature.settings.di.settingsModule
import com.levelchef.feature.trophyroom.di.trophyroomModule
import com.levelchef.shared.nav.SharedApp
import org.koin.core.context.startKoin
import org.koin.core.qualifier.named
import org.koin.dsl.module
import platform.UIKit.UIViewController

/**
 * iOS entry point, called once from `iOSApp.swift`'s `init()` — mirrors `LevelChefApplication`'s
 * `startKoin` call on Android (minus `onboardingModule`: `feature:onboarding` isn't KMP yet, so
 * the iOS shell skips straight to Home). `geminiApiKey` is blank until a real key is plumbed in
 * from Xcode build settings/Info.plist (see AGENTS.md's iOS "Not yet done" item);
 * `RecipeRepositoryImpl` already treats a blank key as "serve the bundled fallback recipes", so
 * this never fails.
 */
fun doInitKoin() {
    startKoin {
        modules(
            module { single(named("geminiApiKey")) { "" } },
            databaseModule,
            dataModule,
            homeModule,
            settingsModule,
            ingredientsModule,
            trophyroomModule,
            recipeDetailModule,
            mealReviewModule,
            cookingLogModule,
        )
    }
}

/**
 * Called from `ContentView.swift` via a `UIViewControllerRepresentable`. Boots the full bottom-nav
 * graph (`SharedApp`) — everything reachable from Home except onboarding (skipped, see
 * `doInitKoin`'s doc comment) and the Android-only debug showcase.
 */
fun MainViewController(): UIViewController = ComposeUIViewController {
    LevelChefTheme {
        SharedApp()
    }
}
