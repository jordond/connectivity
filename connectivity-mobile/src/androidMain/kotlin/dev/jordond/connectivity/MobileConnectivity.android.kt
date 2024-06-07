package dev.jordond.connectivity

import kotlinx.coroutines.CoroutineScope

public actual fun Connectivity(
    options: ConnectivityOptions,
    scope: CoroutineScope,
): Connectivity = Connectivity(options, scope)