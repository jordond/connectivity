package dev.jordondev.connectivity

import dev.jordond.connectivity.Connectivity
import dev.jordondev.connectivity.internal.HttpConnectivity
import io.ktor.client.HttpClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers

public fun Connectivity(
    options: HttpConnectivityOptions = HttpConnectivityOptions(),
    scope: CoroutineScope = CoroutineScope(Dispatchers.Default),
    httpClient: HttpClient = HttpClient(),
): Connectivity {
    return HttpConnectivity(scope, options, httpClient)
}

public fun Connectivity(
    scope: CoroutineScope = CoroutineScope(Dispatchers.Default),
    httpClient: HttpClient = HttpClient(),
    options: HttpConnectivityOptions.Builder.() -> Unit = {},
): Connectivity = Connectivity(HttpConnectivityOptions.build(options), scope, httpClient)

public fun Connectivity.force() {
    (this as? HttpConnectivity)?.forcePoll()
}

public suspend fun Connectivity.check(): Connectivity.Status {
    return (this as? HttpConnectivity)?.check() ?: status.value
}