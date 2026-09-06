import com.levelchef.buildlogic.catalogLibs
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
}

android {
    compileSdk = 36

    defaultConfig {
        minSdk = 26
        targetSdk = 35
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }
}

kotlin {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_11)
    }
}

dependencies {
    add("implementation", platform(catalogLibs.findLibrary("compose-bom").get()))
    add("implementation", catalogLibs.findLibrary("compose-ui").get())
    add("implementation", catalogLibs.findLibrary("compose-material3").get())
    add("implementation", catalogLibs.findLibrary("kermit").get())

    // Renders @Preview / @LevelChefPreview in the IDE preview pane (ui-tooling-preview only supplies
    // the annotations). Debug-only so it never ships in release.
    add("debugImplementation", catalogLibs.findLibrary("compose-ui-tooling").get())
}
