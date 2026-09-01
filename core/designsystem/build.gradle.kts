plugins {
    id("levelchef.android.library")
}

android {
    namespace = "com.levelchef.core.designsystem"
}

dependencies {
    implementation(project(":core:ui"))
    implementation(libs.compose.ui.graphics)
}
