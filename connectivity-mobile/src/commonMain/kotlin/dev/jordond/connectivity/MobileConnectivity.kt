package dev.jordond.connectivity

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers

public expect fun Connectivity(
    options: ConnectivityOptions = ConnectivityOptions(),
    scope: CoroutineScope = CoroutineScope(Dispatchers.Default),
): Connectivity

public fun Connectivity(
    scope: CoroutineScope = CoroutineScope(Dispatchers.Default),
    options: ConnectivityOptions.Builder.() -> Unit = {},
): Connectivity = Connectivity(ConnectivityOptions.build(options), scope)

