package dev.jordond.connectivity.android

import dev.jordond.connectivity.Connectivity
import dev.jordond.connectivity.ConnectivityOptions
import dev.jordond.connectivity.android.internal.AndroidConnectivityProvider
import dev.jordond.connectivity.tools.ContextProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers

public fun Connectivity(
    scope: CoroutineScope = CoroutineScope(Dispatchers.Default),
    options: ConnectivityOptions.Builder.() -> Unit = {},
): Connectivity {
    val context = ContextProvider.getInstance().context
    val provider = AndroidConnectivityProvider(context)
    return Connectivity(provider, scope, options)
}
