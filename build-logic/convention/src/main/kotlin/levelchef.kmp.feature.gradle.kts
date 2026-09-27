import com.levelchef.buildlogic.catalogLibs
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    id("org.jetbrains.kotlin.multiplatform")
    id("com.android.library")
    id("org.jetbrains.compose")
    id("org.jetbrains.kotlin.plugin.compose")
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

    sourceSets {
        commonMain.dependencies {
            implementation(project(":core:ui"))
            implementation(project(":core:designsystem"))

            // These `compose.*` accessors come from the org.jetbrains.compose plugin above: on the
            // android target they resolve to the matching androidx.compose artifacts (interop mode),
            // on iOS to Compose Multiplatform's own Skiko-based implementation — one declaration,
            // both platforms.
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.components.resources)

            implementation(catalogLibs.findLibrary("kermit").get())
        }
        androidMain.dependencies {
            // Renders @Preview in the IDE preview pane on the android target (ui-tooling-preview only
            // supplies the annotations). TODO(Stage C): confirm whether this can be scoped to the
            // android-debug-only dependency configuration once this plugin is first applied to a real
            // module and build-verified — the plain `debugImplementation` name from
            // levelchef.android.feature doesn't carry over 1:1 to a KMP module's android target.
            implementation(catalogLibs.findLibrary("compose-ui-tooling-preview").get())
        }
    }
}

android {
    compileSdk = 36

    defaultConfig {
        minSdk = 26
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    buildFeatures {
        compose = true
    }
}
