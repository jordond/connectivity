package dev.jordond.connectivity

import dev.jordond.connectivity.internal.HttpConnectivity
import io.ktor.client.HttpClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers

/**
 * Creates a [Connectivity] instance for HTTP.
 *
 * @param options The [HttpConnectivityOptions] used to configure the connectivity monitoring.
 * Defaults to a new [HttpConnectivityOptions] instance.
 * @param scope The [CoroutineScope] in which the connectivity monitoring will be launched.
 * Defaults to a new [CoroutineScope] with [Dispatchers.Default].
 * @param httpClient The [HttpClient] used to make HTTP requests. Defaults to a new [HttpClient]
 * instance.
 * @return A [Connectivity] instance.
 */
public fun Connectivity(
    options: HttpConnectivityOptions = HttpConnectivityOptions(),
    scope: CoroutineScope = CoroutineScope(Dispatchers.Default),
    httpClient: HttpClient = HttpClient(),
): Connectivity = HttpConnectivity(scope, options, httpClient)

/**
 * Creates a [Connectivity] instance for HTTP using a builder pattern for the [HttpConnectivityOptions].
 *
 * @param scope The [CoroutineScope] in which the connectivity monitoring will be launched.
 * Defaults to a new [CoroutineScope] with [Dispatchers.Default].
 * @param httpClient The [HttpClient] used to make HTTP requests. Defaults to a new [HttpClient]
 * instance.
 * @param options A lambda function that configures the [HttpConnectivityOptions.Builder].
 * @return A [Connectivity] instance.
 */
public fun Connectivity(
    scope: CoroutineScope = CoroutineScope(Dispatchers.Default),
    httpClient: HttpClient = HttpClient(),
    options: HttpConnectivityOptions.Builder.() -> Unit,
): Connectivity = Connectivity(HttpConnectivityOptions.build(options), scope, httpClient)

/**
 * Forces a poll of the connectivity status.
 *
 * **Note:** If the [Connectivity] instance is not an [HttpConnectivity], this function will
 * do nothing.
 */
public fun Connectivity.force() {
    (this as? HttpConnectivity)?.forcePoll()
}

/**
 * Checks the connectivity status.
 *
 * **Note:** If the [Connectivity] instance is not an [HttpConnectivity], the status will be
 * returned from the [Connectivity] instance.
 *
 * @return The current [Connectivity.Status].
 */
public suspend fun Connectivity.check(): Connectivity.Status {
    return (this as? HttpConnectivity)?.check() ?: status.value
}