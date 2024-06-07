package dev.jordond.connectivity

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers

public expect fun Connectivity(
    scope: CoroutineScope = CoroutineScope(Dispatchers.Default),
    options: ConnectivityOptions.Builder.() -> Unit = {},
): Connectivity

