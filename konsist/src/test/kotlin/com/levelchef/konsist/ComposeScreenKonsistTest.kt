package com.levelchef.konsist

import com.lemonappdev.konsist.api.Konsist
import com.lemonappdev.konsist.api.verify.assertTrue
import org.junit.jupiter.api.Test

/**
 * Guards the Compose screen pattern from AGENTS.md: a stateless `XScreen(state, on…)` plus a
 * stateful `XRoute(viewModel = …)`, with UI state modelled as a single fully-defaulted data class.
 */
class ComposeScreenKonsistTest {

    private val scope = Konsist.scopeFromProject()

    @Test
    fun screen_functions_are_composable() {
        scope.functions()
            .filter { it.isTopLevel && it.name.endsWith("Screen") }
            .assertTrue { fn -> fn.annotations.any { it.name == "Composable" } }
    }

    @Test
    fun route_functions_are_composable() {
        scope.functions()
            .filter { it.isTopLevel && it.name.endsWith("Route") }
            .assertTrue { fn -> fn.annotations.any { it.name == "Composable" } }
    }

    @Test
    fun ui_state_types_are_data_classes_with_every_parameter_defaulted() {
        scope.classes()
            .filter { it.name.endsWith("UiState") }
            .assertTrue { klass ->
                klass.hasDataModifier &&
                    klass.primaryConstructor?.parameters.orEmpty().all { it.defaultValue != null }
            }
    }

    @Test
    fun ui_state_types_live_in_a_feature_package() {
        scope.classes()
            .filter { it.name.endsWith("UiState") }
            .assertTrue { it.resideInPackage("com.levelchef.feature..") }
    }
}
