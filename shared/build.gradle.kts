plugins {
    id("levelchef.kmp.feature")
}

kotlin {
    // No `ios()` shortcut exists in this project (see levelchef.kmp.designsystem) — the three
    // targets are declared individually there. Re-invoking each accessor here returns the
    // already-configured target rather than recreating it, so this just adds the framework export
    // on top of what the convention plugin already set up.
    listOf(iosX64(), iosArm64(), iosSimulatorArm64()).forEach { target ->
        target.binaries.framework {
            baseName = "LevelChefShared"
            isStatic = true
        }
    }

    sourceSets {
        commonMain.dependencies {
            implementation(project(":domain"))
            implementation(project(":data"))
            implementation(project(":feature:home"))

            implementation(libs.koin.core)
            implementation(libs.koin.viewmodel.compose)
        }
    }
}

compose.resources {
    packageOfResClass = "com.levelchef.shared.generated.resources"
}

android {
    namespace = "com.levelchef.shared"
}
