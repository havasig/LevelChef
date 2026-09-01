plugins {
    id("levelchef.kmp.library")
    alias(libs.plugins.sqldelight)
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(libs.sqldelight.coroutines)
        }
        androidMain.dependencies {
            implementation(libs.sqldelight.android.driver)
        }
    }
}

android {
    namespace = "com.levelchef.core.database"
}

sqldelight {
    databases {
        create("LevelChefDatabase") {
            packageName.set("com.levelchef.core.database.db")
        }
    }
}
