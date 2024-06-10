package dev.jordond.connectivity.compose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import dev.jordond.connectivity.Connectivity
import dev.jordond.connectivity.ConnectivityOptions
import kotlinx.coroutines.CoroutineScope

/**
 * Create and remember a [ConnectivityState] instance for Android and iOS platforms.
 *
 * @param options The [ConnectivityOptions] to use for configuring the network status monitoring.
 * @param scope The [CoroutineScope] in which to launch the network status monitoring coroutine.
 * @return A [ConnectivityState] instance.
 */
@Composable
public fun rememberConnectivityState(
    options: ConnectivityOptions = ConnectivityOptions(),
    scope: CoroutineScope = rememberCoroutineScope(),
): ConnectivityState {
    val connectivity = remember(options, scope) {
        Connectivity(options, scope)
    }

    return remember(options, scope, connectivity) {
        ConnectivityState(connectivity, scope)
    }
}

/**
 * Create and remember a [ConnectivityState] instance for Android and iOS platforms.
 *
 * @param scope The [CoroutineScope] in which to launch the network status monitoring coroutine.
 * @param block A lambda function to configure the [ConnectivityOptions] for the network status monitoring.
 * @return A [ConnectivityState] instance.
 */
@Composable
public fun rememberConnectivityState(
    scope: CoroutineScope = rememberCoroutineScope(),
    block: ConnectivityOptions.Builder.() -> Unit,
): ConnectivityState {
    val options = remember(block) {
        ConnectivityOptions.build(block)
    }

    return rememberConnectivityState(options = options, scope = scope)
}