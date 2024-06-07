plugins {
    `kotlin-dsl`
}

kotlin {
    jvmToolchain(17)
}

dependencies {
    compileOnly(libs.bundles.logic.plugins)
}

gradlePlugin {
    plugins {
        register("multiplatformConvention") {
            id = "convention.multiplatform"
            implementationClass = "dev.jordond.connectivity.convention.plugin.MultiplatformConventionPlugin"
        }
    }
}
