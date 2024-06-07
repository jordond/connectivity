package dev.jordond.connectivity.ios

import dev.jordond.connectivity.Connectivity
import dev.jordond.connectivity.ConnectivityOptions
import dev.jordond.connectivity.ios.internal.IosConnectivityProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers

public fun Connectivity(
    scope: CoroutineScope = CoroutineScope(Dispatchers.Default),
    options: ConnectivityOptions.Builder.() -> Unit = {},
): Connectivity {
    val provider = IosConnectivityProvider()
    return Connectivity(provider, scope, options)
}
