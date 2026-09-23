package com.levelchef.android.di

import com.levelchef.data.di.dataModule
import com.levelchef.data.di.databaseModule
import com.levelchef.feature.home.di.homeModule
import com.levelchef.feature.ingredients.di.ingredientsModule
import com.levelchef.feature.onboarding.di.onboardingModule
import com.levelchef.feature.recipedetail.di.recipeDetailModule
import com.levelchef.feature.settings.di.settingsModule
import io.ktor.client.engine.HttpClientEngine
import kotlin.test.Test
import org.koin.dsl.module
import org.koin.test.verify.verify

// `verifyAll` checks each module in isolation, so cross-module dependencies (e.g. homeModule's
// GetChefLevelUseCase needing dataModule's UserProfileRepository) need one combined module.
private val appModule = module {
    includes(
        databaseModule, dataModule, homeModule, onboardingModule,
        settingsModule, ingredientsModule, recipeDetailModule,
    )
}

/**
 * Statically verifies the same Koin modules [com.levelchef.android.LevelChefApplication] registers —
 * every constructor dependency in the graph has a matching binding. Catches "forgot to wire X into
 * a module" at build time instead of at app launch.
 */
class KoinModulesVerifyTest {

    @Test
    fun koin_modules_declare_every_dependency() {
        // String is the runtime-provided ingredientId parameter of the detail/form ViewModels.
        // HttpClientEngine: `HttpClient(Android) { ... }` is a factory function that creates its
        // engine internally, but Koin's bytecode-based verify() reads it as HttpClient's primary
        // constructor needing an HttpClientEngine dependency — there is no such Koin definition,
        // and none is needed, since the factory already satisfies it.
        appModule.verify(extraTypes = listOf(String::class, HttpClientEngine::class))
    }
}
