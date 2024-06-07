<img width="500px" src="art/logo-full.png" alt="logo"/>
<br />

[![Maven Central Version](https://img.shields.io/maven-central/v/dev.jordond.connectivity/core)](https://central.sonatype.com/namespace/dev.jordond.connectivity)
[![Kotlin](https://img.shields.io/badge/kotlin-v1.9.23-blue.svg?logo=kotlin)](http://kotlinlang.org)
[![Build](https://github.com/jordond/connectivity/actions/workflows/ci.yml/badge.svg)](https://github.com/jordond/connectivity/actions/workflows/ci.yml)
[![License](https://img.shields.io/github/license/jordond/connectivity)](https://opensource.org/license/mit/)

Connectivity provides network monitoring capabilities for multiplatform projects. It can monitor
network connectivity using native APIs on Android and Apple devices, or by making HTTP requests to
specified hosts.

You can also
view the generated KDoc at [docs.connectivity.jordond.dev](https://docs.connectivity.jordond.dev)

## Table of Contents

- [Features](#features)
- [Platforms](#platforms)
- [Setup](#setup)
    - [Single Platform](#single-platform)
    - [Multiplatform - Device](#multiplatform---device)
    - [All supported platforms](#all-supported-platforms)
- [Usage](#usage)
    - [HTTP monitoring](#http-monitoring)
- [Demo](#demo)
- [Contributing](#contributing)
- [License](#license)

## Features

Monitor network connectivity:

- Native network monitoring on Android and Apple devices.
- Using HTTP requests and polling to monitor network connectivity on all platforms.

## Platforms

This library is written for Kotlin Multiplatform, and can be used on the following platforms:

| Artifact               | Android | iOS | macOS | tvOS | Desktop | Browser (js/wasm) |
|------------------------|:-------:|:---:|:-----:|------|:-------:|:-----------------:|
| `connectivity-core`    |    ✅    |  ✅  |   ✅   | ✅    |    ✅    |         ✅         |
| `connectivity-device`  |    ✅    |  ✅  |   ✅   | ✅    |    ❌    |         ❌         |
| `connectivity-android` |    ✅    |  ❌  |   ❌   | ❌    |    ❌    |         ❌         |
| `connectivity-apple`   |    ❌    |  ✅  |   ✅   | ✅    |    ❌    |         ❌         |
| `connectivity-http`    |    ✅    |  ✅  |   ✅   | ✅    |    ✅    |         ✅         |

## Setup

Add the following dependencies to your project, depending on the platform you are targeting.

```toml
[versions]
connectivity = "1.0.0"

[libraries]
connectivity-core = { module = "dev.jordond.connectivity:connectivity-core", version.ref = "connectivity" }
connectivity-device = { module = "dev.jordond.connectivity:connectivity-device", version.ref = "connectivity" }
connectivity-android = { module = "dev.jordond.connectivity:connectivity-android", version.ref = "connectivity" }
connectivity-apple = { module = "dev.jordond.connectivity:connectivity-apple", version.ref = "connectivity" }
connectivity-http = { module = "dev.jordond.connectivity:connectivity-http", version.ref = "connectivity" }
```

### Single Platform

Here is an example of how to add the dependencies to a single platform project targeting Android:

```kotlin
dependencies {
    implementation(libs.connectivity.core)
    implementation(libs.connectivity.android)
}
```

### Multiplatform - Device

Here is an example of how to add the dependencies to a multiplatform project that targets both
Android and Apple devices:

```kotlin
kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(libs.connectivity.core)
            implementation(libs.connectivity.device)
        }
    }
}
```

### All supported platforms

Here is an example of how to add the dependencies to a multiplatform project that targets all the
supported platforms.
It uses the `connectivity-device` for mobile targets, and `connectivity-http` for the rest:

```kotlin
kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(libs.connectivity.core)
        }

        val deviceMain by creating {
            dependsOn(commonMain.get())
            androidMain.get().dependsOn(this)
            appleMain.get().dependsOn(this)
            dependencies {
                implementation(libs.connectivity.device)
            }
        }

        val httpMain by creating {
            dependsOn(commonMain.get())
            jvmMain.get().dependsOn(this)
            jsMain.get().dependsOn(this)
            wasmJsMain.get().dependsOn(this)
            dependencies {
                implementation(libs.connectivity.http)
            }
        }
    }
}
```

See the [demo](demo) project for a complete example.

## Usage

Basic usage of Connectivity is simple, you just need an instance of the `Connectivity` object, then
you can observe the network connectivity.

```kotlin
val connectivity = Connectivity()
connectivity.start()
connectivity.status.collect { status ->
    when (status) {
        is NetworkStatus.Connected -> println("Connected to network")
        is NetworkStatus.Disconnected -> println("Disconnected from network")
    }
}
```

By default when you construct a `Connectivity` object, it will not automatically start monitoring
network connectivity. You can enable this by passing in `ConnectivityOptions`():

```kotlin
val connectivity = Connectivity {
    autoStart = true
}
```

You can start or stop monitoring network connectivity manually:

```kotlin
val connectivity = Connectivity()
connectivity.start()

// At some later point
connectivity.stop()
```

The above `Connectivity()` function is a factory function provided by the platform-specific modules.
The `connectivity-core` module provides a factory function defined as:

```kotlin
fun Connectivity(
    provider: ConnectivityProvider,
    options: ConnectivityOptions = ConnectivityOptions(),
    scope: CoroutineScope = CoroutineScope(Dispatchers.Default),
): Connectivity
```

### HTTP monitoring

The `connectivity-http` module provides a way to monitor network connectivity by making HTTP
requests to specified hosts. By default it will check connectivity
to `"google.com"`, `"github.com"`, and `"bing.com"` on port `443`. It will check for the first
successful response, and then update the status.

The network status is updated by polling the hosts at a specified interval. The default interval is
5 minutes.

You can customize the HTTP monitoring like so:

**Note:** This is only available in the `connectivity-http` module.

```kotlin
val connectivity = Connectivity {
  hosts("cloudflare.com", "my-own-domain.com")
  port = 80
  pollingIntervalMs = 10.minutes
  timeoutMs = 5.seconds

  // Callback for when a poll is completed
  onPollResult { result ->
    println("Poll result: $result")
  }
}
```

## Demo

A demo app is available in the `demo` directory. It is a Compose Multiplatform app that runs on
Android, and iOS.

## Contributing

Contributions are always welcome!. If you'd like to contribute, please feel free to create a PR or
open an issue.

## License

See [LICENSE](LICENSE) for more information.
