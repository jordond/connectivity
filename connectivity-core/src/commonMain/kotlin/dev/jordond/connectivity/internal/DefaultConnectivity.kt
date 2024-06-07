package dev.jordond.connectivity.internal

import dev.jordond.connectivity.Connectivity
import dev.jordond.connectivity.ConnectivityOptions
import dev.jordond.connectivity.ConnectivityProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

internal class DefaultConnectivity(
    scope: CoroutineScope,
    private val provider: ConnectivityProvider,
    options: ConnectivityOptions,
) : Connectivity, CoroutineScope by scope {

    private val _status = MutableStateFlow<Connectivity.Status>(Connectivity.Status.Disconnected)
    override val status: StateFlow<Connectivity.Status> = _status.asStateFlow()

    private var job: Job? = null

    init {
        if (options.autoStart) {
            start()
        }
    }

    override fun start() {
        job?.cancel()
        job = launch {
            provider.monitor().collect { status ->
                _status.update { status }
            }
        }
    }

    override fun stop() {
        job?.cancel()
        job = null
    }
}