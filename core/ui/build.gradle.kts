plugins {
    id("levelchef.kmp.designsystem")
}

compose.resources {
    packageOfResClass = "com.levelchef.core.ui.generated.resources"
    // See core:designsystem/build.gradle.kts for why this is needed even for same-module access.
    publicResClass = true
}

android {
    namespace = "com.levelchef.core.ui"
}
