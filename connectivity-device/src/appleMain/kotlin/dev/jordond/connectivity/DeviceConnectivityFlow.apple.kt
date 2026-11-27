package dev.jordond.connectivity

import kotlinx.coroutines.flow.Flow

/**
 * Creates a cold [Flow] of [Connectivity.Status] for iOS platforms.
 *
 * This function is platform-specific and its implementation is provided by iOS platform.
 *
 * @return A cold [Flow] of [Connectivity.Status].
 */
public actual fun deviceConnectivityFlow(): Flow<Connectivity.Status> = appleConnectivityFlow()
