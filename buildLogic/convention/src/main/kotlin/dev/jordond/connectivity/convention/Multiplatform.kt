@file:OptIn(ExperimentalWasmDsl::class, ExperimentalKotlinGradlePluginApi::class)

package dev.jordond.connectivity.convention

import com.vanniktech.maven.publish.MavenPublishBaseExtension
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.get
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget
import org.jetbrains.kotlin.gradle.targets.js.dsl.ExperimentalWasmDsl

fun Project.configureMultiplatform(
    platform: Platform,
    name: String = this.name,
) {
    configureMultiplatform(listOf(platform), name)
}

fun Project.configureMultiplatform(
    platforms: List<Platform> = Platforms.All,
    name: String = this.name,
) {
    extensions.configure<KotlinMultiplatformExtension> {
        configureKotlin()
        configurePlatforms(platforms, name)
    }

    if (platforms.contains(Platform.Android)) {
        configureAndroid(name)
    }

    runCatching {
        extensions.configure<MavenPublishBaseExtension> {
            coordinates(artifactId = name)
        }
    }
}

internal fun KotlinMultiplatformExtension.configurePlatforms(
    platforms: List<Platform> = Platforms.All,
    name: String,
) {
    applyDefaultHierarchyTemplate()

    @OptIn(ExperimentalKotlinGradlePluginApi::class)
    compilerOptions {
        freeCompilerArgs.add("-Xexpect-actual-classes")
        optIn.add("dev.jordond.connectivity.InternalConnectivityApi")
    }

    if (platforms.contains(Platform.Android)) {
        androidTarget {
            publishAllLibraryVariants()
        }
    }

    if (platforms.contains(Platform.Jvm)) {
        jvm()
    }

    if (platforms.contains(Platform.MacOS)) {
        macosX64()
        macosArm64()
    }

    // TODO: Waiting on ktor to have stable wasm support
//                if (platforms.contains(Platform.Linux)) {
//                    linuxX64()
//                    linuxArm64()
//                }

    if (platforms.contains(Platform.Js)) {
        js {
            browser()

            if (platforms.contains(Platform.NodeJs)) {
                nodejs()
            }
        }
    }

    if (platforms.contains(Platform.Wasm)) {
        wasmJs {
            browser()

            if (platforms.contains(Platform.NodeJs)) {
                nodejs()
            }
        }
    }

    if (platforms.contains(Platform.Ios)) {
        listOf(
            iosX64(),
            iosArm64(),
            iosSimulatorArm64()
        ).forEach { target ->
            target.binaries.framework {
                baseName = name
                isStatic = true
            }
        }
    }

    // https://kotlinlang.org/docs/native-objc-interop.html#export-of-kdoc-comments-to-generated-objective-c-headers
    this.targets.withType(KotlinNativeTarget::class.java) {
        compilations["main"].compileTaskProvider.configure {
            compilerOptions.freeCompilerArgs.add("-Xexport-kdoc")
        }
    }

    sourceSets.commonTest.dependencies {
        implementation(kotlin("test"))
        implementation(project.libs.findLibrary("kotest-assertions").get())
    }
}