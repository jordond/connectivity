package dev.jordond.connectivity

import kotlinx.coroutines.flow.Flow

/**
 * Creates a cold [Flow] of [Connectivity.Status] for device platforms.
 *
 * Delegates to `androidConnectivityFlow()` on Android and `appleConnectivityFlow()` on Apple
 * platforms.
 *
 * @return A cold [Flow] of [Connectivity.Status].
 */
public expect fun deviceConnectivityFlow(): Flow<Connectivity.Status>
