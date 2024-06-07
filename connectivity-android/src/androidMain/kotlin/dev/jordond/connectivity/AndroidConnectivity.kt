package dev.jordond.connectivity

import dev.jordond.connectivity.internal.AndroidConnectivityProvider
import dev.jordond.connectivity.tools.ContextProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers

public fun Connectivity(
    options: ConnectivityOptions = ConnectivityOptions(),
    scope: CoroutineScope = CoroutineScope(Dispatchers.Default),
): Connectivity {
    val context = ContextProvider.getInstance().context
    val provider = AndroidConnectivityProvider(context)
    return Connectivity(provider, options, scope)
}

public fun Connectivity(
    scope: CoroutineScope = CoroutineScope(Dispatchers.Default),
    options: ConnectivityOptions.Builder.() -> Unit = {},
): Connectivity = Connectivity(ConnectivityOptions.build(options), scope)
