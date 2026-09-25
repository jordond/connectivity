package dev.jordond.connectivity.internal

import dev.drewhamilton.poko.Poko
import dev.jordond.connectivity.Connectivity
import dev.jordond.connectivity.HttpConnectivityOptions
import io.ktor.client.HttpClient
import io.ktor.http.URLProtocol
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.milliseconds

@Poko
internal class HttpConnectivity(
    parentScope: CoroutineScope,
    private val httpOptions: HttpConnectivityOptions,
    private val httpClient: HttpClient,
) : Connectivity {
    private val scope = ConnectivityScope(parentScope)
    private val statusChecker = HttpStatusChecker(httpOptions, httpClient)

    private var job: Job? = null

    private val _statusUpdates = MutableSharedFlow<Connectivity.Status>(
        replay = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST,
    )
    override val statusUpdates: SharedFlow<Connectivity.Status> = _statusUpdates.asSharedFlow()

    private val _monitoring = MutableStateFlow(value = false)
    override val monitoring: StateFlow<Boolean> = _monitoring.asStateFlow()

    init {
        if (httpOptions.options.autoStart) {
            start()
        }
    }

    override suspend fun status(): Connectivity.Status {
        return statusChecker.check().also { status ->
            _statusUpdates.emit(status)
        }
    }

    override fun start() {
        if (job?.isActive == true) return
        poll()
        _monitoring.value = job?.isActive == true
    }

    override fun stop() {
        job?.cancel()
        job = null
        _monitoring.update { false }
    }

    override fun close() {
        stop()
        scope.close()
    }

    internal fun forcePoll() {
        scope.launch {
            statusChecker.check().also { status ->
                _statusUpdates.emit(status)
            }
        }
    }

    private fun poll() {
        job = scope.launch {
            while (isActive) {
                val status = statusChecker.check()
                _statusUpdates.emit(status)
                delay(httpOptions.pollingIntervalMs.milliseconds)
            }
        }
    }
}

internal fun getProtocolAndHost(url: String, port: Int): Pair<URLProtocol, String> {
    val protocol = when {
        url.startsWith("http://") -> URLProtocol.HTTP
        url.startsWith("https://") -> URLProtocol.HTTPS
        port == 443 -> URLProtocol.HTTPS
        else -> URLProtocol.HTTP
    }

    val host = url.removePrefix("http://").removePrefix("https://")
    return protocol to host
}