package dev.jordond.connectivity.internal

import dev.jordond.connectivity.InternalConnectivityApi
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlin.coroutines.CoroutineContext

@InternalConnectivityApi
public class ConnectivityScope(parentScope: CoroutineScope) : CoroutineScope {

    private val parentJob: Job? = parentScope.coroutineContext[Job]

    override val coroutineContext: CoroutineContext =
        parentScope.coroutineContext + SupervisorJob()

    init {
        parentJob?.invokeOnCompletion { cancelIfParentIsDone() }
    }

    public fun cancelIfParentIsDone() {
        if (parentJob != null && !parentJob.isActive) {
            cancel("The scope provided to Connectivity is no longer active")
        }
    }
}
