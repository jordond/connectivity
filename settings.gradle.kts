enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

pluginManagement {
    repositories {
        gradlePluginPortal()
        google()
        mavenCentral()
    }

    includeBuild("buildLogic")
}

dependencyResolutionManagement {
    @Suppress("UnstableApiUsage")
    repositories {
        google()
        mavenCentral()
    }
}

plugins {
    id("com.gradle.develocity") version "3.19"
}

develocity {
    buildScan {
        termsOfUseUrl.set("https://gradle.com/help/legal-terms-of-use")
        termsOfUseAgree.set("yes")

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
