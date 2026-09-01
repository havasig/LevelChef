import dev.detekt.gradle.Detekt

plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.multiplatform) apply false
    alias(libs.plugins.compose.compiler) apply false
    alias(libs.plugins.sqldelight) apply false
    alias(libs.plugins.kotlin.serialization) apply false
    alias(libs.plugins.detekt)
}

dependencies {
    detektPlugins(libs.detekt.ktlint.wrapper)
    detektPlugins(libs.detekt.rules.compose)
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
