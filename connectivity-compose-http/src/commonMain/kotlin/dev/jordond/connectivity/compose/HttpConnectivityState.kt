package dev.jordond.connectivity.compose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import dev.jordond.connectivity.Connectivity
import dev.jordond.connectivity.ConnectivityOptions
import dev.jordond.connectivity.HttpConnectivityOptions
import io.ktor.client.HttpClient
import kotlinx.coroutines.CoroutineScope

/**
 * Create and remember a [ConnectivityState] instance for Android and iOS platforms.
 *
 * @param options The [ConnectivityOptions] to use for configuring the network status monitoring.
 * @param scope The [CoroutineScope] in which to launch the network status monitoring coroutine.
 * @param httpClient The [HttpClient] instance to use for network requests.
 * @return A [HttpConnectivityState] instance.
 */
@Composable
public fun rememberConnectivityState(
    options: HttpConnectivityOptions = HttpConnectivityOptions(),
    scope: CoroutineScope = rememberCoroutineScope(),
    httpClient: HttpClient = HttpClient()
): HttpConnectivityState {
    val factory = remember(options, scope, httpClient) {
        { Connectivity(options, scope, httpClient) }
    }

    return remember(options, scope, factory) {
        HttpConnectivityState(options, scope, factory)
    }
}

/**
 * Create and remember a [ConnectivityState] instance for Android and iOS platforms.
 *
 * @param scope The [CoroutineScope] in which to launch the network status monitoring coroutine.
 * @param httpClient The [HttpClient] instance to use for network requests.
 * @param block A lambda function to configure the [HttpConnectivityOptions] instance.
 * @return A [HttpConnectivityState] instance.
 */
@Composable
public fun rememberConnectivityState(
    scope: CoroutineScope = rememberCoroutineScope(),
    httpClient: HttpClient = HttpClient(),
    block: HttpConnectivityOptions.Builder.() -> Unit,
): HttpConnectivityState {
    val options = remember(block) {
        HttpConnectivityOptions.build(block)
    }

    return rememberConnectivityState(options = options, scope = scope, httpClient = httpClient)
}

/**
 * Create and remember a [ConnectivityState] instance for Android and iOS platforms.
 *
 * @param autoStart A Boolean indicating whether to start monitoring the network status automatically.
 * @param scope The [CoroutineScope] in which to launch the network status monitoring coroutine.
 * @param httpClient The [HttpClient] instance to use for network requests.
 * @return A [HttpConnectivityState] instance.
 */
@Composable
public fun rememberConnectivityState(
    autoStart: Boolean = false,
    scope: CoroutineScope = rememberCoroutineScope(),
    httpClient: HttpClient = HttpClient()
): HttpConnectivityState {
    return rememberConnectivityState(
        scope = scope,
        httpClient = httpClient,
        block = { this.autoStart = autoStart },
    )
}

/**
 * Represents the state of network connectivity.
 *
 * This class provides properties and functions for monitoring the network status and checking
 * whether the device is connected to the network or a metered network.
 *
 * @property options The [HttpConnectivityOptions] to use for configuring the network status monitoring.
 * @property scope The [CoroutineScope] in which to launch the network status monitoring coroutine.
 * @param factory The lambda function to create a new [Connectivity] instance.
 */
@Stable
public class HttpConnectivityState(
    options: HttpConnectivityOptions,
    scope: CoroutineScope,
    factory: () -> Connectivity,
) : ConnectivityState(options.options, scope, factory) {

    /**
     * The [HttpConnectivityOptions] instance used for configuring the network status monitoring.
     */
    public val httpOptions: HttpConnectivityOptions = options
}