package dev.jordond.connectivity.convention

import dev.jordond.connectivity.convention.Platform.Android
import dev.jordond.connectivity.convention.Platform.Ios
import dev.jordond.connectivity.convention.Platform.Js
import dev.jordond.connectivity.convention.Platform.Jvm
import dev.jordond.connectivity.convention.Platform.Linux
import dev.jordond.connectivity.convention.Platform.MacOS
import dev.jordond.connectivity.convention.Platform.NodeJs
import dev.jordond.connectivity.convention.Platform.TvOS
import dev.jordond.connectivity.convention.Platform.Wasm

enum class Platform {
    Android,
    Ios,
    MacOS,
    TvOS,
    Linux,
    Jvm,
    Js,
    Wasm,
    NodeJs,
}

object Platforms {

    val All: Set<Platform> = setOf(Android, Ios, MacOS, TvOS, Linux, Jvm, Js, Wasm, NodeJs)
    val Mobile: Set<Platform> = setOf(Android, Ios)
    val Apple: Set<Platform> = setOf(Ios, MacOS, TvOS)
    val Browser: Set<Platform> = setOf(Js, Wasm)
    val Compose: Set<Platform> = setOf(Android, Ios, Jvm, Js, Wasm)
}