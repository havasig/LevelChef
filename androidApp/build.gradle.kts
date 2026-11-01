import java.util.Properties

plugins {
    id("levelchef.android.application")
}

val localProperties = Properties().apply {
    val file = rootProject.file("local.properties")
    if (file.exists()) file.inputStream().use(::load)
}

// Release signing: RELEASE_STORE_FILE / RELEASE_STORE_PASSWORD / RELEASE_KEY_ALIAS /
// RELEASE_KEY_PASSWORD, from local.properties (a release manager's machine) or identically named
// env vars (a CI/CD release job) — see README's "Signing a release build". The keystore itself is
// never committed. Any property missing means "no release keystore available", and `release`
// falls back to debug signing so `./gradlew build`/CI keep working without one; a build meant for
// the Play Store must supply all four.
fun releaseSigningProperty(key: String): String? =
    localProperties.getProperty(key)?.takeIf { it.isNotBlank() }
        ?: System.getenv(key)?.takeIf { it.isNotBlank() }

// The recipe proxy's URL (the Cloudflare Worker in /proxy) — NOT a secret; the Gemini key lives only in
// the Worker. From local.properties or the identically named env var (the release job). Empty when
// unset — RecipeRepositoryImpl treats a blank URL as "no live recommender configured" and falls back
// to its bundled sample recipes instead of failing.
val recipeProxyUrl = releaseSigningProperty("RECIPE_PROXY_URL") ?: ""

val releaseStoreFile = releaseSigningProperty("RELEASE_STORE_FILE")
val releaseStorePassword = releaseSigningProperty("RELEASE_STORE_PASSWORD")
val releaseKeyAlias = releaseSigningProperty("RELEASE_KEY_ALIAS")
val releaseKeyPassword = releaseSigningProperty("RELEASE_KEY_PASSWORD")
val hasReleaseKeystore =
    releaseStoreFile != null && releaseStorePassword != null &&
        releaseKeyAlias != null && releaseKeyPassword != null

android {
    namespace = "com.levelchef.android"

    defaultConfig {
        applicationId = "com.levelchef.android"
        versionCode = 1
        versionName = "0.1.0"
        buildConfigField("String", "RECIPE_PROXY_URL", "\"$recipeProxyUrl\"")
    }

    signingConfigs {
        if (hasReleaseKeystore) {
            create("release") {
                storeFile = rootProject.file(releaseStoreFile!!)
                storePassword = releaseStorePassword
                keyAlias = releaseKeyAlias
                keyPassword = releaseKeyPassword
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            signingConfig = if (hasReleaseKeystore) {
                signingConfigs.getByName("release")
            } else {
                signingConfigs.getByName("debug")
            }
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

    testImplementation(kotlin("test"))
    testImplementation(libs.koin.test)
    // For KoinModulesVerifyTest's HttpClientEngine reference (see databaseModule's HttpClient single).
    testImplementation(libs.ktor.client.core)
}
