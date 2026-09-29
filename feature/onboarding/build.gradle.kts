plugins {
    id("levelchef.kmp.feature")
    alias(libs.plugins.kover)
    alias(libs.plugins.roborazzi)
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(project(":domain"))
            implementation(libs.koin.viewmodel.compose)
        }

        getByName("androidUnitTest").dependencies {
            implementation(kotlin("test"))
            implementation(libs.kotlinx.coroutines.test)
            implementation(libs.turbine)

            implementation(project.dependencies.platform(libs.compose.bom))
            implementation(libs.roborazzi)
            implementation(libs.roborazzi.compose)
            implementation(libs.roborazzi.junit.rule)
            implementation(libs.robolectric)
            implementation(libs.androidx.compose.ui.test.junit4)
            implementation(libs.androidx.compose.ui.test.manifest)
        }
    }
}

compose.resources {
    packageOfResClass = "com.levelchef.feature.onboarding.generated.resources"
}

android {
    namespace = "com.levelchef.feature.onboarding"

    testOptions {
        unitTests {
            isIncludeAndroidResources = true
        }
    }
}

// Screenshot baselines are versioned under source control (not build/).
roborazzi {
    outputDir.set(layout.projectDirectory.dir("src/androidUnitTest/screenshots"))
}
