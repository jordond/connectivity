package dev.jordond.connectivity

import dev.jordond.connectivity.internal.DefaultConnectivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.StateFlow

public interface Connectivity {

    public val status: StateFlow<Status>

    public fun start()

    public fun stop()

    public sealed interface Status {

        public data class Connected(public val metered: Boolean) : Status
        public data object Disconnected : Status
    }
}

public fun Connectivity(
    provider: ConnectivityProvider,
    options: ConnectivityOptions = ConnectivityOptions(),
    scope: CoroutineScope = CoroutineScope(Dispatchers.Default),
): Connectivity {
    return DefaultConnectivity(scope, provider, options)
}

public fun Connectivity(
    provider: ConnectivityProvider,
    scope: CoroutineScope = CoroutineScope(Dispatchers.Default),
    options: ConnectivityOptions.Builder.() -> Unit = {},
): Connectivity {
    return Connectivity(provider, ConnectivityOptions.build(options), scope)
}