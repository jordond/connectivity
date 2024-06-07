package dev.jordond.connectivity

import kotlinx.coroutines.CoroutineScope

/**
 * Creates a [Connectivity] instance for Android platforms.
 *
 * This function is platform-specific and its implementation is provided by Android platform.
 *
 * @param options The [ConnectivityOptions] used to configure the connectivity monitoring.
 * @param scope The [CoroutineScope] in which the connectivity monitoring will be launched.
 * @return A [Connectivity] instance.
 */
public actual fun Connectivity(
    options: ConnectivityOptions,
    scope: CoroutineScope,
): Connectivity = Connectivity(options, scope)