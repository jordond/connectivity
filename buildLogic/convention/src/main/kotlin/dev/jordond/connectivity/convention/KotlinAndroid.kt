package dev.jordond.connectivity.convention

import com.android.build.api.dsl.CommonExtension
import com.android.build.api.dsl.LibraryExtension
import org.gradle.api.JavaVersion
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.get
import org.jetbrains.kotlin.gradle.dsl.ExplicitApiMode
import org.jetbrains.kotlin.gradle.dsl.KotlinTopLevelExtension

internal fun Project.configureAndroid(name: String = this.name) {
    setNamespace(name)
    extensions.configure<LibraryExtension> {
        configureKotlinAndroid(this)
    }
}

internal fun Project.configureKotlinAndroid(commonExtension: CommonExtension<*, *, *, *, *, *>) {
    commonExtension.apply {
        compileSdk = libs.findVersion("sdk-compile").get().toString().toInt()

        defaultConfig {
            minSdk = libs.findVersion("sdk-min").get().toString().toInt()
        }

        buildTypes {
            getByName("release") {
                isMinifyEnabled = false
            }
        }

        compileOptions {
            sourceCompatibility = JavaVersion.VERSION_17
            targetCompatibility = JavaVersion.VERSION_17
        }

        sourceSets["main"].apply {
            manifest.srcFile("src/androidMain/AndroidManifest.xml")
        }
    }
}

fun Project.configureAndroidCompose(commonExtension: CommonExtension<*, *, *, *, *, *>) {
    commonExtension.apply {
        buildFeatures {
            compose = true
        }

        composeOptions {
            kotlinCompilerExtensionVersion = libs.findVersion("compose-compiler").get().toString()
        }
    }
}

internal fun Project.setNamespace(name: String) {
    extensions.configure<LibraryExtension> {
        val packageName = libs.findVersion("group").get().toString()
        namespace = "$packageName.${name.replace("-", ".")}"
    }
}

fun Project.disableExplicitApi() {
    extensions.configure<KotlinTopLevelExtension> {
        explicitApi = ExplicitApiMode.Disabled
    }
}

internal fun Project.configureKotlin() {
    extensions.configure<KotlinTopLevelExtension> {
        explicitApi()
        jvmToolchain(jvmTargetVersion)
    }
}
