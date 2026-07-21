import com.android.build.api.dsl.KotlinMultiplatformAndroidLibraryTarget
import org.gradle.api.plugins.ExtensionAware
import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.multiplatform)
    alias(libs.plugins.android.library)
    alias(libs.plugins.compose)
    alias(libs.plugins.compose.compiler)
}

kotlin {
    applyDefaultHierarchyTemplate()

    (this as ExtensionAware)
        .extensions
        .findByType(KotlinMultiplatformAndroidLibraryTarget::class.java)
        ?.apply {
            namespace = "dev.jordond.connectivity.demo"
            compileSdk = libs.versions.sdk.compile.get().toInt()
            minSdk = libs.versions.sdk.min.get().toInt()
            compilerOptions.jvmTarget.set(JvmTarget.JVM_17)
        }

    js {
        outputModuleName.set("composeApp")
        browser {
            commonWebpackConfig {
                outputFileName = "composeApp.js"
            }
        }
        binaries.executable()
    }

    @OptIn(ExperimentalWasmDsl::class)
    wasmJs {
        outputModuleName.set("composeApp")
        browser {
            commonWebpackConfig {
                outputFileName = "composeApp.js"
            }
        }
        binaries.executable()
    }

    jvm("desktop")

    listOf(
        iosArm64(),
        iosSimulatorArm64(),
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "ComposeApp"
            isStatic = true
        }
    }

    sourceSets {
        commonMain.dependencies {
            implementation(projects.connectivityCore)
            implementation(projects.connectivityCompose)

            implementation(libs.kotlinx.coroutines.core)
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.materialIconsExtended)
            implementation(compose.ui)
            implementation(compose.components.uiToolingPreview)
            implementation(libs.kermit)
            implementation(libs.bundles.voyager)
            implementation(libs.bundles.stateHolder)
        }

        create("deviceMain") {
            dependsOn(commonMain.get())
            androidMain.get().dependsOn(this)
            appleMain.get().dependsOn(this)
            dependencies {
                implementation(projects.connectivityDevice)
                implementation(projects.connectivityComposeDevice)
            }
        }

        val desktopMain = getByName("desktopMain") {
            dependencies {
                implementation(compose.desktop.currentOs)
                implementation(libs.kotlinx.coroutines.core.jvm)
            }
        }

        create("httpMain") {
            dependsOn(commonMain.get())
            desktopMain.dependsOn(this)
            jsMain.get().dependsOn(this)
            wasmJsMain.get().dependsOn(this)
            dependencies {
                implementation(projects.connectivityHttp)
                implementation(projects.connectivityComposeHttp)
            }
        }
    }

    @OptIn(ExperimentalKotlinGradlePluginApi::class)
    compilerOptions {
        freeCompilerArgs.add("-Xexpect-actual-classes")
    }
}

compose.desktop {
    application {
        mainClass = "MainKt"

        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "dev.jordond.connectivity.demo"
            packageVersion = "1.0.0"
        }
    }
}
