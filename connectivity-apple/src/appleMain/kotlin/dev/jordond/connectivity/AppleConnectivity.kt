package dev.jordond.connectivity

import dev.jordond.connectivity.internal.AppleConnectivityProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers

public fun Connectivity(
    options: ConnectivityOptions = ConnectivityOptions(),
    scope: CoroutineScope = CoroutineScope(Dispatchers.Default),
): Connectivity = Connectivity(AppleConnectivityProvider, options, scope)

public fun Connectivity(
    scope: CoroutineScope = CoroutineScope(Dispatchers.Default),
    options: ConnectivityOptions.Builder.() -> Unit = {},
): Connectivity = Connectivity(ConnectivityOptions.build(options), scope)
