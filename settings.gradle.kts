pluginManagement {
    includeBuild("build-logic")
    repositories {
        google()
        gradlePluginPortal()
        mavenCentral()
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.9.0"
}

dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "LevelChef"

include(":androidApp")

include(":core:model")
include(":core:database")
include(":core:ui")
include(":core:designsystem")

include(":domain")
include(":data")

include(":konsist")

include(":feature:home")
include(":feature:recipedetail")
include(":feature:mealreview")
include(":feature:trophyroom")
include(":feature:cookinglog")
include(":feature:onboarding")
