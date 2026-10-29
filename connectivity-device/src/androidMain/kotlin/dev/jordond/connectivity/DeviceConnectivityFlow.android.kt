package dev.jordond.connectivity

import kotlinx.coroutines.flow.Flow

/**
 * Creates a cold [Flow] of [Connectivity.Status] for Android platforms.
 *
 * This function is platform-specific and its implementation is provided by Android platform.
 *
 * @return A cold [Flow] of [Connectivity.Status].
 */
public actual fun deviceConnectivityFlow(): Flow<Connectivity.Status> = androidConnectivityFlow()
