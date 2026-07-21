@file:OptIn(ExperimentalWasmDsl::class)

package dev.jordond.connectivity.convention

import com.android.build.api.dsl.KotlinMultiplatformAndroidLibraryTarget
import com.vanniktech.maven.publish.MavenPublishBaseExtension
import org.gradle.api.Project
import org.gradle.api.plugins.ExtensionAware
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.get
import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget

fun Project.configureMultiplatform(
    platform: Platform,
    name: String = this.name,
) {
    configureMultiplatform(setOf(platform), name)
}

fun Project.configureMultiplatform(
    platforms: Set<Platform> = Platforms.All,
    name: String = this.name,
) {
    extensions.configure<KotlinMultiplatformExtension> {
        configureKotlin()
        configurePlatforms(this@configureMultiplatform, platforms, name)
    }

    runCatching {
        extensions.configure<MavenPublishBaseExtension> {
            coordinates(artifactId = name)
        }
    }
}

internal fun KotlinMultiplatformExtension.configurePlatforms(
    project: Project,
    platforms: Set<Platform> = Platforms.All,
    name: String,
) {
    applyDefaultHierarchyTemplate()

    compilerOptions {
        freeCompilerArgs.add("-Xexpect-actual-classes")
        optIn.add("dev.jordond.connectivity.InternalConnectivityApi")
    }

    if (platforms.contains(Platform.Android)) {
        (this as ExtensionAware)
            .extensions
            .findByType(KotlinMultiplatformAndroidLibraryTarget::class.java)
            ?.apply {
                namespace = project.buildNamespace(name)
                compileSdk = project.libs
                    .findVersion("sdk-compile")
                    .get()
                    .toString()
                    .toInt()

                minSdk = project.libs
                    .findVersion("sdk-min")
                    .get()
                    .toString()
                    .toInt()

                compilerOptions.jvmTarget.set(
                    JvmTarget.fromTarget(project.jvmTargetVersion.toString()),
                )

                withHostTest {}
            }
    }

    if (platforms.contains(Platform.Jvm)) {
        jvm()
    }

    // Intel Apple targets (macosX64, iosX64, tvosX64) are dropped: Kotlin deprecated them and
    // Compose Multiplatform 1.11 no longer publishes artifacts for them.
    if (platforms.contains(Platform.MacOS)) {
        macosArm64()
    }

    if (platforms.contains(Platform.TvOS)) {
        tvosArm64()
        tvosSimulatorArm64()
    }

    if (platforms.contains(Platform.Linux)) {
        linuxX64()
        linuxArm64()
    }

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
    targets.withType(KotlinNativeTarget::class.java) {
        compilations["main"].compileTaskProvider.configure {
            compilerOptions {
                freeCompilerArgs.add("-Xexport-kdoc")
            }
        }
    }

    sourceSets.commonTest.dependencies {
        implementation(kotlin("test"))
        implementation(project.libs.findLibrary("kotlinx-coroutines-test").get())
        implementation(project.libs.findLibrary("kotest-assertions").get())
        implementation(project.libs.findLibrary("turbine").get())
    }

    if (platforms.contains(Platform.Android)) {
        sourceSets.named("androidHostTest").configure {
            dependencies {
                implementation(project.libs.findLibrary("mockk-android").get())
                implementation(project.libs.findLibrary("mockk-agent").get())
            }
        }
    }
}
