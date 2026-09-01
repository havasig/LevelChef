plugins {
    id("levelchef.kmp.library")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            api(project(":domain"))
            implementation(project(":core:model"))
            implementation(project(":core:database"))
            implementation(libs.sqldelight.coroutines)
            implementation(libs.ktor.client.core)
            implementation(libs.ktor.client.content.negotiation)
            implementation(libs.ktor.serialization.json)
            implementation(libs.kotlinx.serialization.json)
            implementation(libs.kotlinx.datetime)
            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.koin.core)
        }
        androidMain.dependencies {
            implementation(libs.sqldelight.android.driver)
            implementation(libs.ktor.client.android)
            implementation(libs.koin.android)
        }
    }
}

android {
    namespace = "com.levelchef.data"
}
