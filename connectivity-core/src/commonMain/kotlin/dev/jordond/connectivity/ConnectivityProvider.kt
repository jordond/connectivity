package dev.jordond.connectivity

import kotlinx.coroutines.flow.Flow

/**
 * The [ConnectivityProvider] interface provides a way to monitor the network connectivity status.
 *
 * @property monitor A [Flow] representing the current connectivity status.
 */
public interface ConnectivityProvider {

    /**
     * Starts monitoring the connectivity status.
     *
     * @return A [Flow] of [Connectivity.Status] representing the current connectivity status.
     */
    public fun monitor(): Flow<Connectivity.Status>
}

/**
 * Creates a [ConnectivityProvider] instance.
 *
 * @param flow The [Flow] of [Connectivity.Status] to use for monitoring connectivity.
 * @return A [ConnectivityProvider] instance.
 */
public fun ConnectivityProvider(
    flow: Flow<Connectivity.Status>,
): ConnectivityProvider = object : ConnectivityProvider {
    override fun monitor(): Flow<Connectivity.Status> = flow
}

/**
 * Converts a [Flow] of [Connectivity.Status] to a [ConnectivityProvider].
 *
 * @return A [ConnectivityProvider] instance.
 */
public fun Flow<Connectivity.Status>.asProvider(): ConnectivityProvider = ConnectivityProvider(this)