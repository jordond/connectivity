import dev.jordond.connectivity.convention.Platforms
import dev.jordond.connectivity.convention.configureMultiplatform

plugins {
    alias(libs.plugins.multiplatform)
    alias(libs.plugins.poko)
    alias(libs.plugins.dokka)
    alias(libs.plugins.publish)
    alias(libs.plugins.kotlinx.kover)
    alias(libs.plugins.convention.multiplatform)
}

configureMultiplatform(Platforms.Apple)

kotlin {
    sourceSets {
        appleMain.dependencies {
            implementation(projects.connectivityCore)
            implementation(libs.kotlinx.coroutines.core)
        }
    }
}