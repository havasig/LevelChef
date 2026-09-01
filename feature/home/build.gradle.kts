plugins {
    id("levelchef.android.feature")
}

android {
    namespace = "com.levelchef.feature.home"
}

dependencies {
    implementation(project(":domain"))

    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.koin.viewmodel.compose)
    implementation(libs.kotlinx.datetime)
}
