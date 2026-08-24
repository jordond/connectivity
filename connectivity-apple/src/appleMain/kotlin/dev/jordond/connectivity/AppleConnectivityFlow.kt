package dev.jordond.connectivity

import dev.jordond.connectivity.internal.AppleConnectivityProvider
import kotlinx.coroutines.flow.Flow

/**
 * Creates a cold [Flow] of [Connectivity.Status] for Apple platforms.
 *
 * @return A cold [Flow] of [Connectivity.Status].
 */
public fun appleConnectivityFlow(): Flow<Connectivity.Status> =
    connectivityFlow(AppleConnectivityProvider)
