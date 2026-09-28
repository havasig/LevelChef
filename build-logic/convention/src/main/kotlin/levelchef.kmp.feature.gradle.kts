plugins {
    id("levelchef.kmp.designsystem")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(project(":core:ui"))
            implementation(project(":core:designsystem"))
        }
    }
}
