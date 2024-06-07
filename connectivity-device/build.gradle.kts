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
        }

        androidMain.dependencies {
            api(projects.connectivityAndroid)
        }

        appleMain.dependencies {
            api(projects.connectivityApple)
        }
    }
}