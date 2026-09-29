import com.levelchef.buildlogic.catalogLibs

plugins {
    id("levelchef.android.library")
}

dependencies {
    add("implementation", project(":core:ui"))
    add("implementation", project(":core:designsystem"))

    add("implementation", catalogLibs.findLibrary("compose-ui-graphics").get())
    add("implementation", catalogLibs.findLibrary("compose-ui-tooling-preview").get())
    // core:designsystem's Compose Multiplatform material3 stopped transitively pulling in
    // material-icons-core once compose-multiplatform moved past 1.7.3 (see
    // levelchef.kmp.designsystem's own materialIconsExtended comment) -- core:designsystem's fix for
    // that is `implementation`-scoped, invisible to project-dependency consumers like this one, so
    // feature:onboarding (the one feature module still on this plain-Android convention plugin, not
    // levelchef.kmp.feature) needs its own explicit Jetpack Compose equivalent.
    add("implementation", catalogLibs.findLibrary("compose-material-icons-extended").get())

    // core:ui/core:designsystem read Compose Multiplatform resources (fonts, strings) but only
    // `implementation` that dependency, so it isn't visible here transitively; every feature
    // module's screenshot tests need it directly to call PreviewContextConfigurationEffect(),
    // which Robolectric requires to initialize the Android context CMP resources read from.
    add("testImplementation", catalogLibs.findLibrary("compose-multiplatform-resources").get())
}
