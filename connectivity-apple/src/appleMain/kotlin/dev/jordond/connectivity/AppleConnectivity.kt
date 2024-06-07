package dev.jordond.connectivity

import dev.jordond.connectivity.internal.AppleConnectivityProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers

/**
 * Creates a [Connectivity] instance for Apple platforms.
 *
 * @param options The [ConnectivityOptions] used to configure the connectivity monitoring.
 * Defaults to a new [ConnectivityOptions] instance.
 * @param scope The [CoroutineScope] in which the connectivity monitoring will be launched.
 * Defaults to a new [CoroutineScope] with [Dispatchers.Default].
 * @return A [Connectivity] instance.
 */
public fun Connectivity(
    options: ConnectivityOptions = ConnectivityOptions(),
    scope: CoroutineScope = CoroutineScope(Dispatchers.Default),
): Connectivity = Connectivity(AppleConnectivityProvider, options, scope)

/**
 * Creates a [Connectivity] instance for Apple platforms using a builder pattern for the [ConnectivityOptions].
 *
 * @param scope The [CoroutineScope] in which the connectivity monitoring will be launched.
 * Defaults to a new [CoroutineScope] with [Dispatchers.Default].
 * @param options A lambda function that configures the [ConnectivityOptions.Builder].
 * @return A [Connectivity] instance.
 */
public fun Connectivity(
    scope: CoroutineScope = CoroutineScope(Dispatchers.Default),
    options: ConnectivityOptions.Builder.() -> Unit = {},
): Connectivity = Connectivity(ConnectivityOptions.build(options), scope)