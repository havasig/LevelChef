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
