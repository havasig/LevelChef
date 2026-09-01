plugins {
    // Version-less: the Kotlin Gradle Plugin is already on the build classpath via build-logic.
    id("org.jetbrains.kotlin.jvm")
}

// Test-only meta-module: Konsist architecture rules that guard the conventions in AGENTS.md.
// Not an app/library module, so it deliberately skips the levelchef.* convention plugins.
kotlin {
    jvmToolchain(17)
}

dependencies {
    testImplementation(libs.konsist)
    testImplementation(libs.junit.jupiter)
    testRuntimeOnly(libs.junit.platform.launcher)
}

tasks.test {
    useJUnitPlatform()
}
