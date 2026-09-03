package com.levelchef.konsist

import com.lemonappdev.konsist.api.Konsist
import com.lemonappdev.konsist.api.verify.assertFalse
import org.junit.jupiter.api.Test

/**
 * Guards the one-way module dependency rules documented in AGENTS.md:
 *
 * ```
 * feature:*  -> core:designsystem, core:ui (+ domain / data when needed)
 * data       -> domain -> core:model
 * core:database  <- data only
 * ```
 */
class ModuleBoundaryKonsistTest {

    private val files = Konsist.scopeFromProject().files

    @Test
    fun no_feature_module_depends_on_another_feature_module() {
        files
            .filter { it.packagee?.name?.startsWith("com.levelchef.feature.") == true }
            .assertFalse { file ->
                val ownFeature = file.packagee!!.name
                    .removePrefix("com.levelchef.feature.")
                    .substringBefore(".")
                file.imports.any { import ->
                    import.name.startsWith("com.levelchef.feature.") &&
                        import.name.removePrefix("com.levelchef.feature.").substringBefore(".") != ownFeature
                }
            }
    }

    @Test
    fun domain_does_not_depend_on_android_compose_or_infrastructure() {
        files
            .filter { it.packagee?.name?.startsWith("com.levelchef.domain") == true }
            .assertFalse { file ->
                file.imports.any { it.name.isForbiddenInPureKotlinLayer() }
            }
    }

    @Test
    fun core_model_does_not_depend_on_android_compose_or_infrastructure() {
        files
            .filter { it.packagee?.name?.startsWith("com.levelchef.core.model") == true }
            .assertFalse { file ->
                file.imports.any { it.name.isForbiddenInPureKotlinLayer() }
            }
    }

    @Test
    fun domain_depends_only_on_core_model_within_the_project() {
        files
            .filter { it.packagee?.name?.startsWith("com.levelchef.domain") == true }
            .assertFalse { file ->
                file.imports.any { import ->
                    import.name.startsWith("com.levelchef.") &&
                        !import.name.startsWith("com.levelchef.domain") &&
                        !import.name.startsWith("com.levelchef.core.model")
                }
            }
    }

    @Test
    fun core_database_is_referenced_from_the_data_module_only() {
        files
            .filter { file ->
                file.packagee?.name?.startsWith("com.levelchef.") == true &&
                    file.packagee?.name?.startsWith("com.levelchef.data") != true &&
                    file.packagee?.name?.startsWith("com.levelchef.core.database") != true
            }
            .assertFalse { file ->
                file.imports.any { it.name.startsWith("com.levelchef.core.database") }
            }
    }
}

private fun String.isForbiddenInPureKotlinLayer(): Boolean =
    startsWith("android.") ||
        startsWith("androidx.") ||
        startsWith("org.koin.") ||
        startsWith("io.ktor.") ||
        startsWith("app.cash.sqldelight.")
