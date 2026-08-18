package dev.jordond.connectivity.internal

import dev.drewhamilton.poko.Poko
import dev.jordond.connectivity.Connectivity
import dev.jordond.connectivity.ConnectivityOptions
import dev.jordond.connectivity.ConnectivityProvider
import dev.jordond.connectivity.connectivityFlow
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

@Poko
@Suppress("OVERRIDE_DEPRECATION")
internal class DefaultConnectivity(
    parentScope: CoroutineScope,
    private val provider: ConnectivityProvider,
    options: ConnectivityOptions,
) : Connectivity {
    private val scope = ConnectivityScope(parentScope)

    private var job: Job? = null

    private val _statusUpdates = MutableSharedFlow<Connectivity.Status>(
        replay = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST,
    )
    override val statusUpdates: SharedFlow<Connectivity.Status> = _statusUpdates.asSharedFlow()

    private val _monitoring = MutableStateFlow(false)
    override val monitoring: StateFlow<Boolean> = _monitoring.asStateFlow()

    init {
        if (options.autoStart) {
            start()
        }
    }

    override suspend fun status(): Connectivity.Status {
        return connectivityFlow(provider).first().also { status ->
            _statusUpdates.emit(status)
        }
    }

    override fun start() {
        job?.cancel()
        val started = scope.launch {
            connectivityFlow(provider).collect { status ->
                _statusUpdates.emit(status)
            }
        }
        job = started
        _monitoring.value = started.isActive
    }

    override fun stop() {
        job?.cancel()
        job = null
        _monitoring.value = false
    }

    override fun close() {
        stop()
        scope.close()
    }
}
