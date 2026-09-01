pluginManagement {
    includeBuild("build-logic")
    repositories {
        google()
        gradlePluginPortal()
        mavenCentral()
    }
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

include(":feature:home")
include(":feature:recipedetail")
include(":feature:mealreview")
include(":feature:trophyroom")
include(":feature:cookinglog")
include(":feature:onboarding")
