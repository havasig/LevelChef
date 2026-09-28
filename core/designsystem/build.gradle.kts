plugins {
    id("levelchef.kmp.designsystem")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(project(":core:ui"))
        }
    }
}

compose.resources {
    packageOfResClass = "com.levelchef.core.designsystem.generated.resources"
}

android {
    namespace = "com.levelchef.core.designsystem"
}
