package dev.jordond.connectivity

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers

/**
 * Creates a [Connectivity] instance for device platforms.
 *
 * @param options The [ConnectivityOptions] used to configure the connectivity monitoring.
 * Defaults to a new [ConnectivityOptions] instance.
 * @param scope The [CoroutineScope] in which the connectivity monitoring will be launched. Defaults
 * to a new [CoroutineScope] with [Dispatchers.Default].
 * @return A [Connectivity] instance.
 */
public expect fun Connectivity(
    options: ConnectivityOptions = ConnectivityOptions(),
    scope: CoroutineScope = CoroutineScope(Dispatchers.Default),
): Connectivity

/**
 * Creates a [Connectivity] instance for device platforms using a builder pattern for the [ConnectivityOptions].
 *
 * @param scope The [CoroutineScope] in which the connectivity monitoring will be launched. Defaults
 * to a new [CoroutineScope] with [Dispatchers.Default].
 * @param options A lambda function that configures the [ConnectivityOptions.Builder].
 * @return A [Connectivity] instance.
 */
public fun Connectivity(
    scope: CoroutineScope = CoroutineScope(Dispatchers.Default),
    options: ConnectivityOptions.Builder.() -> Unit,
): Connectivity = Connectivity(ConnectivityOptions.build(options), scope)