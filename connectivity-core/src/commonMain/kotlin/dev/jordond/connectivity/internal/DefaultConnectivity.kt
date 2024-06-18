package dev.jordond.connectivity.internal

import dev.drewhamilton.poko.Poko
import dev.jordond.connectivity.Connectivity
import dev.jordond.connectivity.Connectivity.Update
import dev.jordond.connectivity.ConnectivityOptions
import dev.jordond.connectivity.ConnectivityProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@Poko
internal class DefaultConnectivity(
    scope: CoroutineScope,
    private val provider: ConnectivityProvider,
    options: ConnectivityOptions,
) : Connectivity, CoroutineScope by scope {

    private var job: Job? = null

    private val _statusUpdates = MutableSharedFlow<Connectivity.Status>(
        replay = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST,
    )
    override val statusUpdates: SharedFlow<Connectivity.Status> = _statusUpdates.asSharedFlow()

    private val _isMonitoring = MutableStateFlow(false)
    override val isMonitoring: StateFlow<Boolean> = _isMonitoring.asStateFlow()

    @Deprecated("Use statusUpdates instead", ReplaceWith("statusUpdates"))
    override val updates: StateFlow<Update> =
        combine(statusUpdates, isMonitoring) { status, isMonitoring ->
            Update(isMonitoring, status)
        }.stateIn(
            scope = scope,
            started = SharingStarted.WhileSubscribed(),
            initialValue = Update(isMonitoring = false, Connectivity.Status.Disconnected)
        )

    init {
        if (options.autoStart) {
            start()
        }
    }

    override suspend fun status(): Connectivity.Status {
        return provider.monitor().first().also { status ->
            _statusUpdates.emit(status)
        }
    }

    override fun start() {
        job?.cancel()
        job = launch {
            _isMonitoring.update { true }
            provider.monitor().collect { status ->
                _statusUpdates.emit(status)
            }
        }
    }

    override fun stop() {
        job?.cancel()
        job = null
        _isMonitoring.update { false }
    }
}