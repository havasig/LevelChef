package com.levelchef.shared

import androidx.compose.ui.window.ComposeUIViewController
import com.levelchef.core.ui.theme.LevelChefTheme
import com.levelchef.data.di.dataModule
import com.levelchef.data.di.databaseModule
import com.levelchef.feature.home.HomeRoute
import com.levelchef.feature.home.di.homeModule
import org.koin.core.context.startKoin
import org.koin.core.qualifier.named
import org.koin.dsl.module
import platform.UIKit.UIViewController

/**
 * iOS entry point, called once from `iOSApp.swift`'s `init()` — mirrors `LevelChefApplication`'s
 * `startKoin` call on Android. `geminiApiKey` is blank until a real key is plumbed in from Xcode
 * build settings/Info.plist (see AGENTS.md's iOS "Not yet done" item); `RecipeRepositoryImpl`
 * already treats a blank key as "serve the bundled fallback recipes", so this never fails.
 */
fun doInitKoin() {
    startKoin {
        modules(
            module { single(named("geminiApiKey")) { "" } },
            databaseModule,
            dataModule,
            homeModule,
        )
    }
}

/**
 * Called from `ContentView.swift` via a `UIViewControllerRepresentable`. Minimal shell: just the
 * Home screen, no navigation — every `HomeRoute` callback defaults to a no-op.
 */
fun MainViewController(): UIViewController = ComposeUIViewController {
    LevelChefTheme {
        HomeRoute()
    }
}
