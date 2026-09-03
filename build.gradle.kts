import dev.detekt.gradle.Detekt
import kotlinx.kover.gradle.plugin.dsl.CoverageUnit

plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.multiplatform) apply false
    alias(libs.plugins.compose.compiler) apply false
    alias(libs.plugins.sqldelight) apply false
    alias(libs.plugins.kotlin.serialization) apply false
    alias(libs.plugins.detekt)
    alias(libs.plugins.kover)
}

dependencies {
    detektPlugins(libs.detekt.ktlint.wrapper)
    detektPlugins(libs.detekt.rules.compose)

    // Modules whose coverage is merged into the root report. Add a feature module here once
    // it gains a ViewModel worth covering.
    kover(project(":domain"))
    kover(project(":data"))
    kover(project(":feature:home"))
}

// Coverage — aggregated at the root; the plugin is applied per-module (see those build files).
// Kover 0.9's verify rules can't be filtered individually, so the report scope *is* the gate
// scope: reports and koverVerify both cover the "logic" layer only (use cases, repository
// implementations, feature ViewModels & domain mappers). Compose UI is deliberately out —
// it's left to future screenshot tests. See docs/TOOLING.md.
kover {
    reports {
        filters {
            includes {
                classes(
                    "com.levelchef.domain.usecase.*",
                    "com.levelchef.data.repository.*",
                    "com.levelchef.feature.*.*ViewModel",
                    "com.levelchef.feature.*.*DomainMappersKt",
                )
            }
            excludes {
                annotatedBy("androidx.compose.runtime.Composable")
            }
        }
        verify {
            rule("Logic layer line coverage") {
                minBound(90, CoverageUnit.LINE)
            }
        }
    }
}

detekt {
    config.setFrom(files("config/detekt/detekt.yml"))
    parallel = true
    basePath.set(layout.projectDirectory)
}

// Single aggregate analysis over every module's Kotlin sources. detekt is applied only to
// the (plugin-free) root so it never has to integrate with the Android/KMP Gradle plugins —
// detekt 2.0-alpha's plugin is not compatible with Kotlin Gradle Plugin 2.0.x otherwise.
tasks.withType<Detekt>().configureEach {
    setSource(files(projectDir))
    include("**/*.kt", "**/*.kts")
    exclude("**/build/**", "**/build-logic/**", "**/buildSrc/**", "**/.gradle/**", "**/resources/**")
    reports {
        html.required.set(true)
        sarif.required.set(true)
    }
}
