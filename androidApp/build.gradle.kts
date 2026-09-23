import java.util.Properties

plugins {
    id("levelchef.android.application")
}

// Read from local.properties (gitignored) rather than committing a real key. Empty when unset —
// RecipeRepositoryImpl treats a blank key as "no live recommender configured" and falls back to
// its bundled sample recipes instead of failing.
val geminiApiKey = Properties().apply {
    val file = rootProject.file("local.properties")
    if (file.exists()) file.inputStream().use(::load)
}.getProperty("GEMINI_API_KEY", "")

android {
    namespace = "com.levelchef.android"

    defaultConfig {
        applicationId = "com.levelchef.android"
        versionCode = 1
        versionName = "0.1.0"
        buildConfigField("String", "GEMINI_API_KEY", "\"$geminiApiKey\"")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
        }
    }

    // Auto-generates a locale config from the values-*/ dirs present (en, hu), so Android 13+
    // lists LevelChef in Settings > Apps > Language with a per-app override — no custom UI needed.
    androidResources {
        generateLocaleConfig = true
    }
}

dependencies {
    implementation(project(":core:model"))
    implementation(project(":core:database"))
    implementation(project(":core:ui"))
    implementation(project(":core:designsystem"))

    implementation(project(":domain"))
    implementation(project(":data"))

    implementation(project(":feature:home"))
    implementation(project(":feature:recipedetail"))
    implementation(project(":feature:mealreview"))
    implementation(project(":feature:trophyroom"))
    implementation(project(":feature:cookinglog"))
    implementation(project(":feature:onboarding"))
    implementation(project(":feature:settings"))
    implementation(project(":feature:ingredients"))

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)

    implementation(libs.compose.ui.graphics)
    implementation(libs.compose.ui.tooling.preview)
    implementation(libs.compose.navigation)

    implementation(libs.koin.android)
    implementation(libs.koin.compose)
    implementation(libs.vico.compose)

    testImplementation(kotlin("test"))
    testImplementation(libs.koin.test)
}
