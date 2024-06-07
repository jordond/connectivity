import dev.jordond.connectivity.convention.Platform
import dev.jordond.connectivity.convention.configureMultiplatform

plugins {
    alias(libs.plugins.multiplatform)
    alias(libs.plugins.poko)
    alias(libs.plugins.dokka)
    alias(libs.plugins.publish)
    alias(libs.plugins.convention.multiplatform)
}

configureMultiplatform(Platform.Ios)

kotlin {
    sourceSets {
        iosMain.dependencies {
            implementation(projects.connectivityCore)
            implementation(libs.kotlinx.coroutines.core)
        }
    }
}