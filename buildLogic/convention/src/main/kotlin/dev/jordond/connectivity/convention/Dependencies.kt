package dev.jordond.connectivity.convention

import org.gradle.api.Project
import org.gradle.api.plugins.ExtensionAware
import org.gradle.kotlin.dsl.configure
import org.jetbrains.compose.ComposePlugin.Dependencies
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.plugin.KotlinDependencyHandler

fun KotlinMultiplatformExtension.commonDependencies(handler: KotlinDependencyHandler.() -> Unit) {
    sourceSets.commonMain.dependencies(handler)
}

fun KotlinMultiplatformExtension.androidDependencies(handler: KotlinDependencyHandler.() -> Unit) {
    sourceSets.androidMain.dependencies(handler)
}

fun KotlinMultiplatformExtension.iosDependencies(handler: KotlinDependencyHandler.() -> Unit) {
    sourceSets.iosMain.dependencies(handler)
}

fun Project.composeDependencies() {
    extensions.configure<KotlinMultiplatformExtension>() {
        sourceSets.commonMain.dependencies {
            implementation(compose.runtime)
            implementation(compose.runtimeSaveable)
            implementation(compose.foundation)
            implementation(compose.material3)
        }
    }
}

private val KotlinMultiplatformExtension.compose: Dependencies
    get() =
        (this as ExtensionAware).extensions.getByName("compose") as Dependencies