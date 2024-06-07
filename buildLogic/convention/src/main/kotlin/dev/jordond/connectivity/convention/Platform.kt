package dev.jordond.connectivity.convention

import dev.jordond.connectivity.convention.Platform.Android
import dev.jordond.connectivity.convention.Platform.Ios
import dev.jordond.connectivity.convention.Platform.Js
import dev.jordond.connectivity.convention.Platform.Jvm
import dev.jordond.connectivity.convention.Platform.Linux
import dev.jordond.connectivity.convention.Platform.MacOS
import dev.jordond.connectivity.convention.Platform.NodeJs
import dev.jordond.connectivity.convention.Platform.Wasm

enum class Platform {
    Android,
    Ios,
    MacOS,
    Linux,
    Jvm,
    Js,
    Wasm,
    NodeJs,
}

object Platforms {

    val All: List<Platform> = listOf(Android, Ios, MacOS, Linux, Jvm, Js, Wasm, NodeJs)
    val Mobile: List<Platform> = listOf(Android, Ios)
    val Browser: List<Platform> = listOf(Js, Wasm)
    val Compose: List<Platform> = listOf(Android, Ios, Jvm, Js, Wasm)
}