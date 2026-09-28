import com.levelchef.buildlogic.catalogLibs
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    id("org.jetbrains.kotlin.multiplatform")
    id("com.android.library")
}

kotlin {
    androidTarget {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_11)
        }
    }

    // Kotlin/Native can only compile these on a macOS host; on Linux (CI's ubuntu-latest job) Kotlin
    // still configures the targets but their compile/link tasks are skipped automatically. No
    // iosX64() (Intel simulator) — Compose Multiplatform 1.12.1 no longer publishes artifacts for
    // it (JetBrains dropped Intel-simulator support); iosSimulatorArm64 (Apple Silicon, what both
    // this repo's CI and any real dev machine actually use) and iosArm64 (real devices) cover it.
    iosArm64()
    iosSimulatorArm64()

    sourceSets.getByName("commonMain").dependencies {
        implementation(catalogLibs.findLibrary("kermit").get())
    }
}

android {
    compileSdk = 36

    defaultConfig {
        minSdk = 26
    }
}
