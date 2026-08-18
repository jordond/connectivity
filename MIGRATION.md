# Migration Guide

Starting in 2.7.0, the hot `Connectivity` API (`start()`, `stop()`, `close()`, `monitoring`, and the
factory functions that take a `CoroutineScope`) is deprecated in favor of the cold `Flow` API added
in 2.6.0. This document covers what's changing, why, and how to migrate.

## Timeline

- **2.7.0**: the hot API is deprecated at `WARNING`. Nothing changes at runtime; every call still
  behaves exactly as it did in 2.6.0. The warnings are advance notice.
- **2.8.0**: the same deprecations move to `ERROR`. Code that still calls the hot API stops
  compiling until you migrate or suppress the warning yourself.
- **3.0.0**: the deprecated surface is deleted. `Connectivity` stops implementing `AutoCloseable`.

## Why

`Connectivity` used to take a `CoroutineScope` it did not own, and use it to turn a cold source into
a hot `SharedFlow`. That one decision produced most of the library's lifecycle machinery
(`start()`, `stop()`, `close()`, `monitoring`) and two production issues:
[#208](https://github.com/jordond/connectivity/issues/208), where `stop()` cancelled the caller's
own scope, and [#262](https://github.com/jordond/connectivity/issues/262), where an internal scope
added to fix #208 ended up hanging `status()` forever. A cold `Flow` has no lifecycle of its own to
get wrong: the listener starts when you collect and stops when you cancel, the same as any other
`Flow`. Those cold factories (`connectivityFlow`, `httpConnectivityFlow`, `deviceConnectivityFlow`,
`androidConnectivityFlow`, `appleConnectivityFlow`) shipped in 2.6.0. 2.7.0 marks the hot API
deprecated instead of removing it outright, so you have two full minor versions to move over.

## What's changing

### `connectivity-core`

| Deprecated | Replacement |
|---|---|
| `Connectivity(provider, options, scope)` | `connectivityFlow(provider)` |
| `Connectivity(provider, scope) { ... }` (options builder) | `connectivityFlow(provider)` |
| `Connectivity.start()` | collect `connectivityFlow(provider)` |
| `Connectivity.stop()` | cancel the collecting coroutine |
| `Connectivity.close()` | nothing to close, remove the call |
| `Connectivity.monitoring` / `isMonitoring` | the collector already knows whether it's collecting; use `SharingStarted.WhileSubscribed` if you need shared "is anyone listening" semantics |

`close()` only shipped in 2.6.0, to make releasable the internal scope added for the #262 fix. The
cold API doesn't allocate anything that needs releasing, so it's deprecated here too, one minor
after it was added. That's intentional, not an oversight.

Not deprecated: `statusUpdates`, `status()`, `Status` and its subtypes, `ConnectivityProvider`,
`ConnectivityProvider(flow)`, `asProvider()`, `ConnectivityOptions` and its builder.

`statusUpdates` keeps its name all the way into 3.0, but its type changes from `SharedFlow<Status>`
to `Flow<Status>` once the hot implementation is deleted. That change ships in 3.0.0, not 2.7.0, so
there's no 2.x deprecation warning for it, only the type-change itself when 3.0.0 lands.

### `connectivity-http`

| Deprecated | Replacement |
|---|---|
| `Connectivity(options, scope, httpClient)` | `httpConnectivityFlow(options, httpClient)` |
| `Connectivity(scope, httpClient) { ... }` (options builder) | `httpConnectivityFlow(httpClient) { ... }` |
| `Connectivity.force()` | `status()`, or start a new collection |

### `connectivity-device`

| Deprecated | Replacement |
|---|---|
| `Connectivity(options, scope)` | `deviceConnectivityFlow()` |
| `Connectivity(scope) { ... }` (options builder) | `deviceConnectivityFlow()` |

Not deprecated: `deviceConnectivityFlow()`.

### `connectivity-android`

| Deprecated | Replacement |
|---|---|
| `Connectivity(options, scope)` | `androidConnectivityFlow()` |
| `AndroidConnectivity(options, scope)` | `androidConnectivityFlow()` |
| `Connectivity(scope) { ... }` (options builder) | `androidConnectivityFlow()` |

### `connectivity-apple`

| Deprecated | Replacement |
|---|---|
| `Connectivity(options, scope)` | `appleConnectivityFlow()` |
| `AppleConnectivity(options, scope)` | `appleConnectivityFlow()` |
| `Connectivity(scope) { ... }` (options builder) | `appleConnectivityFlow()` |

### `connectivity-compose`

| Deprecated | Replacement |
|---|---|
| `rememberConnectivityState(connectivity, scope)` | `connectivityFlow(provider).collectAsStateWithLifecycle(null)` |
| `ConnectivityState.isMonitoring` | no longer meaningful, collection state is now the caller's |
| `ConnectivityState.forceCheck()` | call `status()` on your `Connectivity`, or start a new collection |
| `ConnectivityState.startMonitoring()` | collection starts the listener |
| `ConnectivityState.stopMonitoring()` | cancel the collection |

Not deprecated: `ConnectivityState` itself, `status`, `isConnected`, `isMetered`, `isDisconnected`.

### `connectivity-compose-device` and `connectivity-compose-http`

| Deprecated | Replacement |
|---|---|
| `rememberConnectivityState(options, scope)` | `deviceConnectivityFlow()` / `httpConnectivityFlow(...)` + `collectAsStateWithLifecycle(null)` |
| `rememberConnectivityState(scope) { ... }` (options builder) | same |

## Migrating your code

### Plain collection

Before:

```kotlin
val connectivity = Connectivity()
connectivity.start()
coroutineScope.launch {
  connectivity.statusUpdates.collect { status ->
    when (status) {
      is Connectivity.Status.Connected -> println("Connected to network")
      is Connectivity.Status.Disconnected -> println("Disconnected from network")
    }
  }
}
```

After:

```kotlin
coroutineScope.launch {
  deviceConnectivityFlow().collect { status ->
    when (status) {
      is Connectivity.Status.Connected -> println("Connected to network")
      is Connectivity.Status.Disconnected -> println("Disconnected from network")
    }
  }
}
```

There's no `Connectivity` instance to build or `start()`. Collecting the flow starts the listener,
and cancelling `coroutineScope` stops it.

### Shared collection

The old hot API's `statusUpdates` was already a shared `SharedFlow`. Every collector on the same
`Connectivity` instance saw the same emissions without you doing anything. The cold flow doesn't
share by default; each collector gets its own subscription and its own platform listener. If you
want sharing back, opt into it explicitly.

Before:

```kotlin
val connectivity = Connectivity(scope = viewModelScope) {
  autoStart = true
}

// Both collectors share the same underlying listener automatically.
coroutineScope.launch { connectivity.statusUpdates.collect { /* ... */ } }
coroutineScope.launch { connectivity.statusUpdates.collect { /* ... */ } }
```

After:

```kotlin
val status = deviceConnectivityFlow()
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), initialValue = null)

// Both collectors now share status, backed by one listener.
coroutineScope.launch { status.collect { /* ... */ } }
coroutineScope.launch { status.collect { /* ... */ } }
```

### One-off check

Before:

```kotlin
val connectivity = Connectivity()
val status = connectivity.status()
```

After:

```kotlin
val status = deviceConnectivityFlow().first()
```

`status()` isn't going anywhere and still works. But if the only thing you ever did with a
`Connectivity` instance was call `status()` once, you don't need to construct one, or a
`CoroutineScope`, at all.

### Compose

Before:

```kotlin
@Composable
fun MyApp() {
  val state = rememberConnectivityState {
    autoStart = true
  }

  when (state.status) {
    is Connectivity.Status.Connected -> Text("Connected to network")
    is Connectivity.Status.Disconnected -> Text("Disconnected from network")
    else -> {}
  }
}
```

After:

```kotlin
@Composable
fun MyApp() {
  val flow = remember { deviceConnectivityFlow() }
  val status by flow.collectAsStateWithLifecycle(null)

  when (status) {
    is Connectivity.Status.Connected -> Text("Connected to network")
    is Connectivity.Status.Disconnected -> Text("Disconnected from network")
    else -> {}
  }
}
```

Wrap the flow itself in `remember`, not just the collection. `collectAsStateWithLifecycle` keys on
the flow instance, so calling `deviceConnectivityFlow()` directly inside the composable would tear
down and re-register the platform listener on every recomposition. `collectAsStateWithLifecycle`
comes from `androidx.lifecycle:lifecycle-runtime-compose`, which this library doesn't pull in for
you, so add it to your own project.

### HTTP polling

Before:

```kotlin
val connectivity = Connectivity(httpClient = myHttpClient) {
  urls("cloudflare.com", "my-own-domain.com")
  pollingIntervalMs = 10.minutes
}
connectivity.start()

coroutineScope.launch {
  connectivity.statusUpdates.collect { status -> /* ... */ }
}
```

After:

```kotlin
val statusFlow = httpConnectivityFlow(httpClient = myHttpClient) {
  urls("cloudflare.com", "my-own-domain.com")
  pollingIntervalMs = 10.minutes
}.shareIn(coroutineScope, SharingStarted.WhileSubscribed(5_000), replay = 1)

coroutineScope.launch {
  statusFlow.collect { status -> /* ... */ }
}
```

Do this one deliberately. `httpConnectivityFlow()` is cold, so every collector runs its own polling
loop against `myHttpClient`. Two collectors on the plain flow means two sets of outbound requests
every `pollingIntervalMs`, not one. `shareIn` (or `stateIn`, if you only need the latest status)
guarantees there's exactly one loop no matter how many collectors you have.

`httpConnectivityFlow` still accepts `HttpConnectivityOptions`, but it ignores `autoStart`. Setting
it has no effect either way, because collecting the flow is what starts polling. Drop it from your
builder block when you migrate.

## What is not changing

- `Connectivity.Status`, `Status.Connected`, `Status.Disconnected`, and the properties on them
  (`isConnected`, `isMetered`, `isDisconnected`).
- `ConnectivityProvider`, `ConnectivityProvider(flow)`, and `Flow<Status>.asProvider()`.
- `statusUpdates` and `status()` as names. `status()` keeps its signature. `statusUpdates`'s type
  changes from `SharedFlow<Status>` to `Flow<Status>` in 3.0.0, not before.
- `ConnectivityOptions` and its builder.
- `ConnectivityState`, and its `status`, `isConnected`, `isMetered`, and `isDisconnected` properties.
- The platform behavior behind all of the above: the same native network callbacks on Android and
  Apple platforms, and the same HTTP polling logic, just reachable through a `Flow` instead of a
  `SharedFlow`.
- The cold flow factories added in 2.6.0 (`connectivityFlow`, `httpConnectivityFlow`,
  `deviceConnectivityFlow`, `androidConnectivityFlow`, `appleConnectivityFlow`). This guide is about
  migrating onto them, not away from them.
