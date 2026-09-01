package com.levelchef.buildlogic

import org.gradle.api.Project
import org.gradle.api.artifacts.VersionCatalog
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.kotlin.dsl.getByType

/**
 * Precompiled script plugins don't get generated typesafe `libs.xyz` accessors for the version
 * catalog the way ordinary build scripts do, so convention plugins look it up explicitly instead.
 *
 * Deliberately namespaced (not called `libs`, not in the default package): once build-logic's
 * compiled classes land on a consuming module's buildscript classpath, an unqualified top-level
 * `libs` here would shadow Gradle's own generated typesafe `libs` accessor in *that* module's own
 * build.gradle.kts, silently breaking `libs.foo.bar` everywhere in the consuming build.
 */
val Project.catalogLibs: VersionCatalog
    get() = extensions.getByType<VersionCatalogsExtension>().named("libs")
