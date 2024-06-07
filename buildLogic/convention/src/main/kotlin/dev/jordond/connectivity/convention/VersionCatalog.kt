package dev.jordond.connectivity.convention

import org.gradle.api.Project
import org.gradle.api.artifacts.VersionCatalog
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.kotlin.dsl.getByType

internal val Project.libs: VersionCatalog
    get() = extensions.getByType<VersionCatalogsExtension>().named("libs")


internal val Project.jvmTargetVersion: Int
    get() = libs.findVersion("jvmTarget").get().toString().toInt()