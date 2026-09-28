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
    // The generated Res accessors are a separate compilation unit from this module's own code, so
    // Kotlin's `internal` (the default) blocks access even from within this same Gradle module —
    // confirmed by CI: `Res` itself resolved but `Res.string.x` members didn't.
    publicResClass = true
}

android {
    namespace = "com.levelchef.core.designsystem"
}
