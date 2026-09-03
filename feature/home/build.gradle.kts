plugins {
    id("levelchef.android.feature")
    alias(libs.plugins.kover)
}

android {
    namespace = "com.levelchef.feature.home"
}

dependencies {
    implementation(project(":domain"))

    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.koin.viewmodel.compose)
    implementation(libs.kotlinx.datetime)

    testImplementation(kotlin("test"))
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.turbine)
}
