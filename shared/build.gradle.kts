plugins {
    id("levelchef.kmp.feature")
}

kotlin {
    // No `ios()` shortcut exists in this project (see levelchef.kmp.designsystem) — the two
    // targets (iosArm64, iosSimulatorArm64 — no iosX64, see levelchef.kmp.designsystem's comment)
    // are declared individually there. Re-invoking each accessor here returns the already-configured
    // target rather than recreating it, so this just adds the framework export on top of what the
    // convention plugin already set up.
    listOf(iosArm64(), iosSimulatorArm64()).forEach { target ->
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
            implementation(project(":feature:onboarding"))
            implementation(project(":feature:settings"))
            implementation(project(":feature:ingredients"))
            implementation(project(":feature:recipedetail"))
            implementation(project(":feature:mealreview"))
            implementation(project(":feature:trophyroom"))
            implementation(project(":feature:cookinglog"))

            implementation(libs.koin.core)
            implementation(libs.koin.viewmodel.compose)
            // JetBrains' multiplatform fork of navigation-compose (see libs.versions.toml) — the
            // only navigation-compose artifact with an iOS klib; androidApp keeps using Google's.
            implementation(libs.compose.navigation.multiplatform)
        }
    }
}

compose.resources {
    packageOfResClass = "com.levelchef.shared.generated.resources"
}

android {
    namespace = "com.levelchef.shared"
}
