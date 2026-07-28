package dev.jordond.connectivity.internal

import dev.jordond.connectivity.InternalConnectivityApi
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.DisposableHandle
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

@InternalConnectivityApi
public class ConnectivityScope(parentScope: CoroutineScope) {

    private val parentJob: Job? = parentScope.coroutineContext[Job]

    private val scope = CoroutineScope(parentScope.coroutineContext + SupervisorJob())

    private var parentHandle: DisposableHandle? = null

    init {
        parentHandle = parentJob?.invokeOnCompletion { cancelIfParentIsDone() }
    }

    public fun launch(block: suspend CoroutineScope.() -> Unit): Job {
        cancelIfParentIsDone()
        return scope.launch(block = block)
    }

    public fun close() {
        parentHandle?.dispose()
        parentHandle = null
        scope.cancel("Connectivity was closed")
    }

    private fun cancelIfParentIsDone() {
        if (parentJob != null && !parentJob.isActive) {
            scope.cancel("The scope provided to Connectivity is no longer active")
        }
    }
}
