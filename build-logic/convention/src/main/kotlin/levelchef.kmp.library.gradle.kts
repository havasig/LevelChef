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
    // still configures the targets but their compile/link tasks are skipped automatically.
    iosX64()
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
