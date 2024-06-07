package dev.jordond.connectivity

import dev.jordond.connectivity.android.Connectivity
import kotlinx.coroutines.CoroutineScope

public actual fun Connectivity(
    scope: CoroutineScope,
    options: ConnectivityOptions.Builder.() -> Unit,
): Connectivity = Connectivity(scope, options)