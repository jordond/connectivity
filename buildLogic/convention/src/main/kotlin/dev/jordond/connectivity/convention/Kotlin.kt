package dev.jordond.connectivity.convention

import org.gradle.api.Project
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.api.tasks.compile.JavaCompile
import org.gradle.jvm.toolchain.JavaLanguageVersion
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.withType
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinBaseExtension
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmCompilerOptions
import org.jetbrains.kotlin.gradle.tasks.KotlinCompilationTask

internal fun Project.buildNamespace(name: String): String {
    val group = libs.findVersion("group").get().toString()
    if (name.isBlank()) return group
    return group + "." + name.replace("-", ".")
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
        sourceCompatibility = jvmTargetVersion.toString()
        targetCompatibility = jvmTargetVersion.toString()
    }

    tasks.withType<KotlinCompilationTask<*>>().configureEach {
        compilerOptions {
            if (this is KotlinJvmCompilerOptions) {
                jvmTarget.set(JvmTarget.fromTarget(jvmTargetVersion.toString()))
            }
        }
    }
}
