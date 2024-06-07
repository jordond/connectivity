package dev.jordond.connectivity.convention

import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

internal fun KotlinMultiplatformExtension.configureComposeOptIn() {
    sourceSets.all {
        languageSettings {
            optIn("org.jetbrains.compose.resources.ExperimentalResourceApi")
            optIn("androidx.compose.material3.ExperimentalMaterial3Api")
            optIn("androidx.compose.material.ExperimentalMaterialApi")
            optIn("androidx.compose.animation.ExperimentalAnimationApi")
            optIn("androidx.compose.foundation.ExperimentalFoundationApi")
            optIn("androidx.compose.foundation.layout.ExperimentalLayoutApi")
            optIn("androidx.compose.ui.ExperimentalComposeUiApi")
        }
    }
}