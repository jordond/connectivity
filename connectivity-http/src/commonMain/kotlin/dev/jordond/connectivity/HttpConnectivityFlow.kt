package dev.jordond.connectivity

import dev.jordond.connectivity.internal.HttpStatusChecker
import io.ktor.client.HttpClient
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlin.time.Duration.Companion.milliseconds

/**
 * Creates a cold [Flow] of [Connectivity.Status] that polls HTTP endpoints for connectivity.
 *
 * The returned [Flow] is cold: no request is made until it is collected, and cancelling
 * collection stops polling. Every collector runs its own polling loop, so each one adds outbound
 * requests at the configured interval. Use `shareIn`/`stateIn` to share a single loop.
 *
 * **Note:** [HttpConnectivityOptions.options]' `autoStart` is ignored. Collecting the flow starts
 * polling.
 *
 * **Note:** the default [httpClient] is built when this function is called rather than when the
 * flow is collected, and is never closed, which matches how the hot API treats the client. Pass
 * your own [HttpClient] if you call this factory more than once.
 *
 * @param options The [HttpConnectivityOptions] used to configure the connectivity monitoring.
 * Defaults to a new [HttpConnectivityOptions] instance.
 * @param httpClient The [HttpClient] used to make HTTP requests. Defaults to a new [HttpClient]
 * instance.
 * @return A cold [Flow] of [Connectivity.Status].
 */
public fun httpConnectivityFlow(
    options: HttpConnectivityOptions = HttpConnectivityOptions(),
    httpClient: HttpClient = HttpClient(),
): Flow<Connectivity.Status> = flow {
    val statusChecker = HttpStatusChecker(options, httpClient)
    while (true) {
        emit(statusChecker.check())
        delay(options.pollingIntervalMs.milliseconds)
    }
}

/**
 * Creates a cold [Flow] of [Connectivity.Status] that polls HTTP endpoints for connectivity,
 * using a builder for the [HttpConnectivityOptions].
 *
 * @param httpClient The [HttpClient] used to make HTTP requests. Defaults to a new [HttpClient]
 * instance.
 * @param options A builder function for creating the [HttpConnectivityOptions].
 * @return A cold [Flow] of [Connectivity.Status].
 */
public fun httpConnectivityFlow(
    httpClient: HttpClient = HttpClient(),
    options: HttpConnectivityOptions.Builder.() -> Unit,
): Flow<Connectivity.Status> = httpConnectivityFlow(HttpConnectivityOptions.build(options), httpClient)
