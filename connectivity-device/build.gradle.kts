import dev.jordond.connectivity.convention.Platforms
import dev.jordond.connectivity.convention.configureMultiplatform

plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.multiplatform)
    alias(libs.plugins.poko)
    alias(libs.plugins.dokka)
    alias(libs.plugins.publish)
    alias(libs.plugins.convention.multiplatform)
}

configureMultiplatform(Platforms.Mobile + Platforms.Apple)

kotlin {
    sourceSets {
        commonMain.dependencies {
            api(projects.connectivityCore)
            implementation(libs.kotlinx.coroutines.core)
        }

        androidMain.dependencies {
            api(projects.connectivityAndroid)
            implementation(libs.kotlinx.coroutines.android)
        }

        appleMain.dependencies {
            api(projects.connectivityApple)
        }
    }
}