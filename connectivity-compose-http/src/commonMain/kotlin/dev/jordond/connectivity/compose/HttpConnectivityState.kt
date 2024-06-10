package dev.jordond.connectivity.compose

import androidx.compose.runtime.Composable
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
 * @return A [ConnectivityState] instance.
 */
@Composable
public fun rememberConnectivityState(
    options: HttpConnectivityOptions = HttpConnectivityOptions(),
    scope: CoroutineScope = rememberCoroutineScope(),
    httpClient: HttpClient = HttpClient()
): ConnectivityState {
    val connectivity = remember(options, scope, httpClient) {
        Connectivity(options, scope, httpClient)
    }

    return remember(connectivity, scope) {
        ConnectivityState(connectivity, scope)
    }
}

/**
 * Create and remember a [ConnectivityState] instance for Android and iOS platforms.
 *
 * @param scope The [CoroutineScope] in which to launch the network status monitoring coroutine.
 * @param httpClient The [HttpClient] instance to use for network requests.
 * @param block A lambda function to configure the [HttpConnectivityOptions] instance.
 * @return A [ConnectivityState] instance.
 */
@Composable
public fun rememberConnectivityState(
    scope: CoroutineScope = rememberCoroutineScope(),
    httpClient: HttpClient = HttpClient(),
    block: HttpConnectivityOptions.Builder.() -> Unit,
): ConnectivityState {
    val options = remember(block) {
        HttpConnectivityOptions.build(block)
    }

    return rememberConnectivityState(options = options, scope = scope, httpClient = httpClient)
}