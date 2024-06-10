<img width="500px" src="art/logo.png" alt="logo"/>
<br />

[![Maven Central Version](https://img.shields.io/maven-central/v/dev.jordond.connectivity/connectivity-core)](https://central.sonatype.com/namespace/dev.jordond.connectivity)
[![Kotlin](https://img.shields.io/badge/kotlin-v2.0.0-blue.svg?logo=kotlin)](http://kotlinlang.org)
[![Build](https://github.com/jordond/connectivity/actions/workflows/ci.yml/badge.svg)](https://github.com/jordond/connectivity/actions/workflows/ci.yml)
[![License](https://img.shields.io/github/license/jordond/connectivity)](https://opensource.org/license/mit/)

Connectivity provides network monitoring capabilities for multiplatform projects. It can monitor
network connectivity using native APIs on Android and Apple devices, or by making HTTP requests to
specified hosts.

You can also
view the generated KDoc at [connectivity.jordond.dev](https://connectivity.jordond.dev)

## Table of Contents

- [Features](#features)
- [Platforms](#platforms)
- [Setup](#setup)
    - [Single Platform](#single-platform)
    - [Multiplatform - Device](#multiplatform---device)
    - [All supported platforms](#all-supported-platforms)
- [Usage](#usage)
  - [Options](#options)
    - [HTTP monitoring](#http-monitoring)
- [Compose](#compose)
  - [Multiple Targets](#multiple-targets)
- [Demo](#demo)
- [Contributing](#contributing)
- [License](#license)

## Features

Monitor network connectivity:

- Native network monitoring on Android and Apple devices.
- Using HTTP requests and polling to monitor network connectivity on all platforms.

## Platforms

This library is written for Kotlin Multiplatform, and can be used on the following platforms:

| Artifact                      | Android | iOS | macOS | tvOS | JVM | Browser (js/wasm) |
|-------------------------------|:-------:|:---:|:-----:|------|:---:|:-----------------:|
| `connectivity-core`           |    ✅    |  ✅  |   ✅   | ✅    |  ✅  |         ✅         |
| `connectivity-device`         |    ✅    |  ✅  |   ✅   | ✅    |  ❌  |         ❌         |
| `connectivity-android`        |    ✅    |  ❌  |   ❌   | ❌    |  ❌  |         ❌         |
| `connectivity-apple`          |    ❌    |  ✅  |   ✅   | ✅    |  ❌  |         ❌         |
| `connectivity-http`           |    ✅    |  ✅  |   ✅   | ✅    |  ✅  |         ✅         |
| `connectivity-compose`        |    ✅    |  ✅  |   ❌   | ❌    |  ✅  |         ✅         |
| `connectivity-compose-device` |    ✅    |  ✅  |   ❌   | ❌    |  ✅  |         ✅         |
| `connectivity-compose-http`   |    ✅    |  ✅  |   ❌   | ❌    |  ✅  |         ✅         |

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
connectivity-compose = { module = "dev.jordond.connectivity:connectivity-compose", version.ref = "connectivity" }
connectivity-compose-device = { module = "dev.jordond.connectivity:connectivity-compose-device", version.ref = "connectivity" }
connectivity-compose-http = { module = "dev.jordond.connectivity:connectivity-compose-http", version.ref = "connectivity" }
```

### Single Platform

Here is an example of how to add the dependencies to a single platform project targeting Android:

```kotlin
dependencies {
  implementation(libs.connectivity.core)
  implementation(libs.connectivity.android)

  // For compose support
  implementation(libs.connectivity.compose.device)
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

          // For compose support
          implementation(libs.connectivity.compose.device)
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

          // For compose support
          implementation(libs.connectivity.compose)
        }

        val deviceMain by creating {
            dependsOn(commonMain.get())
            androidMain.get().dependsOn(this)
            appleMain.get().dependsOn(this)
            dependencies {
              implementation(libs.connectivity.device)

              // For compose support
              implementation(libs.connectivity.compose.device)
            }
        }

        val httpMain by creating {
            dependsOn(commonMain.get())
            jvmMain.get().dependsOn(this)
            jsMain.get().dependsOn(this)
            wasmJsMain.get().dependsOn(this)
            dependencies {
              implementation(libs.connectivity.http)

              // For compose support
              implementation(libs.connectivity.compose.http)
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
coroutineScope.launch {
  connectivity.statusUpdates.collect { status ->
    when (status) {
      is NetworkStatus.Connected -> println("Connected to network")
      is NetworkStatus.Disconnected -> println("Disconnected from network")
    }
  }
}
```

You can also get the current connectivity status by invoking the suspended `status()` function:

```kotlin
val connectivity = Connectivity()
coroutineScope.launch {
    val status = connectivity.status()
    when (status) {
        is NetworkStatus.Connected -> println("Connected to network")
        is NetworkStatus.Disconnected -> println("Disconnected from network")
    }
}
```

Or you can use the `Connectivity.updates` property, which is a `StateFlow<Connectivity.Update` that
emits a `Connectivity.Update` which contains the network status as well as whether the network
monitoring is active.

```kotlin
val connectivity = Connectivity()
connectivity.start()
coroutineScope.launch {
  connectivity.updates.collect { update ->
    println("Monitoring is active: ${update.isMonitoring}")

    when (update.status) {
      is NetworkStatus.Connected -> println("Connected to network")
      is NetworkStatus.Disconnected -> println("Disconnected from network")
    }
  }
}
```

**Note:** Because the backing field for `Connectivity.updates` is a `StateFlow`, it requires a
initial value to be emitted. This means that the first value emitted will be the *real* current
network status. This is why the `isMonitoring` property is available on the `Connectivity.Update`.

By default when you construct a `Connectivity` object, it will not automatically start monitoring
network connectivity. You can enable this by passing in `ConnectivityOptions`():

### Options

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
requests to specified urls. By default it will check connectivity
to `"google.com"`, `"github.com"`, and `"bing.com"` on port `443`. It will check for the first
successful response, and then update the status.

The network status is updated by polling the urls at a specified interval. The default interval is
5 minutes.

You can customize the HTTP monitoring like so:

**Note:** This is only available in the `connectivity-http` module.

```kotlin
val connectivity = Connectivity {
  urls("cloudflare.com", "my-own-domain.com") // Defaults to ["google.com", "github.com", "bing.com"]
  port = 80 // Defaults to 443
  pollingIntervalMs = 10.minutes // Defaults to 5 minutes
  timeoutMs = 5.seconds // Defaults to 2 seconds

  // Callback for when a poll is completed
  onPollResult { result ->
    when (result) {
      is PollResult.Error -> println("Poll error: ${result.error}")
      is PollResult.Response -> println("Poll http response: ${result.response}")
    }
  }
}
```

## Compose

Connectivity also provides support for Compose Multiplatform. To use it you will have to make sure
you add the dependencies for the `connectivity-compose-x` modules.

Then you can use it like so:

**Note:** This composable is provided by either `connectivity-compose-device`
or `connectivity-compose-http` artifact.

```kotlin
@Composable
fun MyApp() {
  val state = rememberConnectivityState {
    // Optional configurator for ConnectivityOptions
    autoStart = true
  }

  when (state.status) {
    is Connectivity.Status.Connected -> Text("Connected to network")
    is Connectivity.Status.Disconnected -> Text("Disconnected from network")
    else -> {}
  }
}
```

### Multiple Targets

If you need to support both Device and HTTP monitoring in the same project, you will have to do
something similar to [this](#all-supported-platforms).

Example:

```kotlin
// commonMain/Platform.kt
expect fun createConnectivity(): Connectivity
```

Then define the `actual` functions:

```kotlin
// deviceMain/Platform.device.kt
actual fun createConnectivityState(): Connectivity {
  return Connectivity {
    autoStart = true
  }
}

// httpMain/Platform.http.kt
actual fun createConnectivityState(): Connectivity {
  return Connectivity {
    autoStart = true
    urls("cloudflare.com", "my-own-domain.com")
    port = 80
    pollingIntervalMs = 10.minutes
    timeoutMs = 5.seconds
  }
}
```

Then it can be used like so:

```kotlin
@Composable
fun MyApp() {
  val state = createConnectivityState()

  when (state.status) {
    is Connectivity.Status.Connected -> Text("Connected to network")
    is Connectivity.Status.Disconnected -> Text("Disconnected from network")
    else -> {}
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
