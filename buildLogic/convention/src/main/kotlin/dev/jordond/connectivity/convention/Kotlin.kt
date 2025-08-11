package dev.jordond.connectivity.convention

import com.android.build.api.dsl.CommonExtension
import com.android.build.api.dsl.LibraryExtension
import org.gradle.api.JavaVersion
import org.gradle.api.Project
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.api.tasks.compile.JavaCompile
import org.gradle.jvm.toolchain.JavaLanguageVersion
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.get
import org.gradle.kotlin.dsl.withType
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinBaseExtension
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmCompilerOptions
import org.jetbrains.kotlin.gradle.tasks.KotlinCompilationTask

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

internal fun Project.setNamespace(name: String) {
    extensions.configure<LibraryExtension> {
        val packageName = libs.findVersion("group").get().toString()
        namespace = "$packageName.${name.replace("-", ".")}"
    }
}

internal fun Project.configureKotlin() {
    extensions.configure<KotlinBaseExtension> {
        explicitApi()
    }

    extensions.configure<JavaPluginExtension> {
        toolchain {
            languageVersion.set(JavaLanguageVersion.of(jvmTargetVersion))
        }
    }

    tasks.withType<JavaCompile>().configureEach {
        sourceCompatibility = "11"
        targetCompatibility = "11"
    }

    tasks.withType<KotlinCompilationTask<*>>().configureEach {
        compilerOptions {
            if (this is KotlinJvmCompilerOptions) {
                jvmTarget.set(JvmTarget.JVM_11)
            }
        }
    }
}
