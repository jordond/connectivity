enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

pluginManagement {
    repositories {
        gradlePluginPortal()
        google()
        mavenCentral()
        maven("https://maven.pkg.jetbrains.space/kotlin/p/wasm/experimental")
    }

    includeBuild("buildLogic")
}

dependencyResolutionManagement {
    @Suppress("UnstableApiUsage")
    repositories {
        google()
        mavenCentral()
        maven("https://maven.pkg.jetbrains.space/kotlin/p/wasm/experimental")
    }
}

plugins {
    id("com.gradle.develocity") version "3.17.6"
}

develocity {
    buildScan {
        publishing.onlyIf { context ->
            context.buildResult.failures.isNotEmpty() && !System.getenv("CI").isNullOrEmpty()
        }
    }
}

rootProject.name = "connectivity"

include(
    ":connectivity-core",
    ":connectivity-android",
    ":connectivity-apple",
    ":connectivity-device",
    ":connectivity-http",
    ":connectivity-compose",
    ":connectivity-compose-device",
    ":connectivity-compose-http",
    ":connectivity-tools-android",
)

include(":demo:composeApp")
