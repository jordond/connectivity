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

    private var job: Job? = null

    private val _updates = MutableStateFlow(Connectivity.Update.default)
    override val updates: StateFlow<Connectivity.Update> = _updates.asStateFlow()

    init {
        if (options.autoStart) {
            start()
        }
    }

    override fun start() {
        job?.cancel()
        job = launch {
            _updates.update { update ->
                Connectivity.Update(isActive = true, status = update.status)
            }

            provider.monitor().collect { status ->
                _updates.update { Connectivity.Update(isActive = true, status) }
            }
        }
    }

    override fun stop() {
        job?.cancel()
        job = null
        _updates.update { update ->
            Connectivity.Update(isActive = false, status = update.status)
        }
    }
}