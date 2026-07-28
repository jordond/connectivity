package dev.jordond.connectivity

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow

/**
 * Creates a cold [Flow] of [Connectivity.Status] backed by the given [provider].
 *
 * The returned [Flow] is cold: no platform listener is registered until it is collected, and
 * cancelling collection stops it. Each collector starts its own, independent subscription — see
 * `shareIn`/`stateIn` if a single shared listener is needed across multiple collectors.
 *
 * [ConnectivityProvider.monitor] is invoked once per collection rather than once per call to this
 * function. Its contract is to *start* monitoring, and implementations may allocate platform
 * resources before returning their flow, so calling it eagerly would both leak those resources for
 * a flow that is never collected and hand every collector the same underlying listener.
 *
 * @param provider The [ConnectivityProvider] to use for monitoring connectivity.
 * @return A cold [Flow] of [Connectivity.Status].
 */
public fun connectivityFlow(provider: ConnectivityProvider): Flow<Connectivity.Status> = flow {
    emitAll(provider.monitor())
}
