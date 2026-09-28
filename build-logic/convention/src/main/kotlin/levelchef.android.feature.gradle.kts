import com.levelchef.buildlogic.catalogLibs

plugins {
    id("levelchef.android.library")
}

dependencies {
    add("implementation", project(":core:ui"))
    add("implementation", project(":core:designsystem"))

    add("implementation", catalogLibs.findLibrary("compose-ui-graphics").get())
    add("implementation", catalogLibs.findLibrary("compose-ui-tooling-preview").get())

    // core:ui/core:designsystem read Compose Multiplatform resources (fonts, strings) but only
    // `implementation` that dependency, so it isn't visible here transitively; every feature
    // module's screenshot tests need it directly to call PreviewContextConfigurationEffect(),
    // which Robolectric requires to initialize the Android context CMP resources read from.
    add("testImplementation", catalogLibs.findLibrary("compose-multiplatform-resources").get())
}
