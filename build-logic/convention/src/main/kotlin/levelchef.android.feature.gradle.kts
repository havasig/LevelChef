import com.levelchef.buildlogic.catalogLibs

plugins {
    id("levelchef.android.library")
}

dependencies {
    add("implementation", project(":core:ui"))
    add("implementation", project(":core:designsystem"))

    add("implementation", catalogLibs.findLibrary("compose-ui-graphics").get())
    add("implementation", catalogLibs.findLibrary("compose-ui-tooling-preview").get())
}
