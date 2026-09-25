package dev.jordond.connectivity

import dev.jordond.connectivity.internal.AndroidConnectivityProvider
import dev.jordond.connectivity.tools.ContextProvider
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow

/**
 * Creates a cold [Flow] of [Connectivity.Status] for Android.
 *
 * The [android.content.Context] is resolved from [dev.jordond.connectivity.tools.ContextProvider]
 * when the flow is collected, not when it is created. Building this flow does not require an
 * initialised `ContextProvider`, only collecting it does.
 *
 * @return A cold [Flow] of [Connectivity.Status].
 */
public fun androidConnectivityFlow(): Flow<Connectivity.Status> = flow {
    val context = ContextProvider.getInstance().context
    emitAll(connectivityFlow(AndroidConnectivityProvider(context)))
}
