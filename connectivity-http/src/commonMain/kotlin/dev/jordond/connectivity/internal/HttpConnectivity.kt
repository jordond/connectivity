package dev.jordond.connectivity.internal

import dev.drewhamilton.poko.Poko
import dev.jordond.connectivity.Connectivity
import dev.jordond.connectivity.HttpConnectivityOptions
import dev.jordond.connectivity.PollResult
import io.ktor.client.HttpClient
import io.ktor.client.plugins.timeout
import io.ktor.client.request.request
import io.ktor.http.URLProtocol
import io.ktor.http.isSuccess
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
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

@Poko
internal class HttpConnectivity(
    parentScope: CoroutineScope,
    private val httpOptions: HttpConnectivityOptions,
    private val httpClient: HttpClient,
) : Connectivity {

    /**
     * A child of the provided scope, so cancelling the parent still stops polling, but neither
     * [stop] nor a failure while polling can cancel the caller's scope.
     */
    private val scope = CoroutineScope(
        parentScope.coroutineContext + SupervisorJob(parentScope.coroutineContext[Job]),
    )

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
        return checkConnection().also { status ->
            _statusUpdates.emit(status)
        }
    }

    override fun start() {
        if (job?.isActive == true) return
        poll()
        _monitoring.update { true }
    }

    override fun stop() {
        job?.cancel()
        job = null
        _monitoring.update { false }
    }

    internal fun forcePoll() {
        scope.launch {
            checkConnection().also { status ->
                _statusUpdates.emit(status)
            }
        }
    }

    private fun poll() {
        job = scope.launch {
            while (isActive) {
                val status = checkConnection()
                _statusUpdates.emit(status)
                delay(httpOptions.pollingIntervalMs)
            }
        }
    }

    private suspend fun checkConnection(): Connectivity.Status {
        var isConnected = false
        for (url in httpOptions.urls) {
            isConnected = makeRequest(url, httpOptions.port)
            if (isConnected) break
        }

        return if (isConnected) Connectivity.Status.Connected(metered = false)
        else Connectivity.Status.Disconnected
    }

    private suspend fun makeRequest(url: String, port: Int): Boolean {
        val (protocol, host) = getProtocolAndHost(url, port)

        try {
            val response = httpClient.request {
                url {
                    this.protocol = protocol
                    this.host = host
                    this.port = port
                    method = httpOptions.method
                }

                timeout {
                    requestTimeoutMillis = httpOptions.timeoutMs
                }
            }

            notifyPollResult(PollResult.Response(response))

            return response.status.isSuccess()
        } catch (cause: Throwable) {
            if (cause is CancellationException) throw cause

            notifyPollResult(PollResult.Error(cause))
            return false
        }
    }

    /**
     * Invokes the consumer's [HttpConnectivityOptions.onPollResult] callback, a throwing callback
     * must not stop the polling or bring down the scope it is running in.
     */
    private fun notifyPollResult(result: PollResult) {
        try {
            httpOptions.onPollResult?.invoke(result)
        } catch (cause: CancellationException) {
            throw cause
        } catch (_: Throwable) {
            // Ignored, the callback is a notification and its failures are the consumer's problem.
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