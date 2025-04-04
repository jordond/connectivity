import dev.jordond.connectivity.convention.Platform
import dev.jordond.connectivity.convention.configureMultiplatform

plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.multiplatform)
    alias(libs.plugins.dokka)
    alias(libs.plugins.publish)
    alias(libs.plugins.kotlinx.kover)
    alias(libs.plugins.convention.multiplatform)
}

configureMultiplatform(Platform.Android)

kotlin {
    sourceSets {
        androidMain.dependencies {
            implementation(projects.connectivityCore)
            implementation(libs.androidx.startup)
            implementation(libs.androidx.activity)
        }
    }
}