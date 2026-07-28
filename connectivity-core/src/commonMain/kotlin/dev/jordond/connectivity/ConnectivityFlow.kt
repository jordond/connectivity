package dev.jordond.connectivity

import kotlinx.coroutines.flow.Flow

/**
 * Creates a cold [Flow] of [Connectivity.Status] backed by the given [provider].
 *
 * The returned [Flow] is cold: no platform listener is registered until it is collected, and
 * cancelling collection stops it. Each collector starts its own, independent subscription — see
 * `shareIn`/`stateIn` if a single shared listener is needed across multiple collectors.
 *
 * @param provider The [ConnectivityProvider] to use for monitoring connectivity.
 * @return A cold [Flow] of [Connectivity.Status].
 */
public fun connectivityFlow(provider: ConnectivityProvider): Flow<Connectivity.Status> =
    provider.monitor()
