package dev.jordond.connectivity

import dev.jordond.connectivity.internal.DefaultConnectivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.StateFlow

/**
 * The Connectivity interface provides a way to monitor the network connectivity status.
 *
 * @property status A [StateFlow] representing the current connectivity status.
 */
public interface Connectivity {

    public val status: StateFlow<Status>

    /**
     * Starts monitoring the connectivity status.
     */
    public fun start()

    /**
     * Stops monitoring the connectivity status.
     */
    public fun stop()

    /**
     * Represents the connectivity status.
     */
    public sealed interface Status {

        /**
         * Represents a connected status.
         *
         * @property metered A Boolean indicating whether the connection is metered.
         */
        public data class Connected(public val metered: Boolean) : Status

        /**
         * Represents a disconnected status.
         */
        public data object Disconnected : Status
    }
}

/**
 * Creates a Connectivity instance.
 *
 * @param provider The [ConnectivityProvider] to use for monitoring connectivity.
 * @param options The [ConnectivityOptions] to use for configuring the connectivity monitoring.
 * @param scope The [CoroutineScope] in which to launch the connectivity monitoring coroutine.
 * @return A [Connectivity] instance.
 */
public fun Connectivity(
    provider: ConnectivityProvider,
    options: ConnectivityOptions = ConnectivityOptions(),
    scope: CoroutineScope = CoroutineScope(Dispatchers.Default),
): Connectivity {
    return DefaultConnectivity(scope, provider, options)
}

/**
 * Creates a [Connectivity] instance with a builder for the [ConnectivityOptions].
 *
 * @param provider The [ConnectivityProvider] to use for monitoring connectivity.
 * @param scope The [CoroutineScope] in which to launch the connectivity monitoring coroutine.
 * @param options A builder function for creating the [ConnectivityOptions].
 * @return A [Connectivity] instance.
 */
public fun Connectivity(
    provider: ConnectivityProvider,
    scope: CoroutineScope = CoroutineScope(Dispatchers.Default),
    options: ConnectivityOptions.Builder.() -> Unit,
): Connectivity {
    return Connectivity(provider, ConnectivityOptions.build(options), scope)
}