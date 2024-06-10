import dev.jordond.connectivity.convention.Platforms
import dev.jordond.connectivity.convention.configureMultiplatform

plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.multiplatform)
    alias(libs.plugins.compose)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.poko)
    alias(libs.plugins.dokka)
    alias(libs.plugins.publish)
    alias(libs.plugins.convention.multiplatform)
}

configureMultiplatform(Platforms.Mobile)

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(projects.connectivityCore)
            implementation(projects.connectivityCompose)
            api(projects.connectivityDevice)

            implementation(compose.runtime)
            implementation(compose.ui)
            implementation(libs.kotlinx.coroutines.core)
        }
    }
}