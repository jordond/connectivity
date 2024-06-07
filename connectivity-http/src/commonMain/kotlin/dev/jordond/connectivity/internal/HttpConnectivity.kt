package dev.jordond.connectivity.internal

import dev.jordond.connectivity.Connectivity
import dev.jordond.connectivity.HttpConnectivityOptions
import io.ktor.client.HttpClient
import io.ktor.client.plugins.timeout
import io.ktor.client.request.host
import io.ktor.client.request.port
import io.ktor.client.request.request
import io.ktor.http.isSuccess
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

internal class HttpConnectivity(
    scope: CoroutineScope,
    private val httpOptions: HttpConnectivityOptions,
    private val httpClient: HttpClient,
) : Connectivity, CoroutineScope by scope {

    private val _status = MutableStateFlow<Connectivity.Status>(Connectivity.Status.Disconnected)
    override val status: StateFlow<Connectivity.Status> = _status.asStateFlow()

    private var job: Job? = null

    init {
        if (httpOptions.options.autoStart) {
            start()
        }
    }

    override fun start() {
        if (job != null) return
        poll()
    }

    override fun stop() {
        job?.cancel()
        job = null
    }

    internal fun forcePoll() {
        stop()
        start()
    }

    internal suspend fun check(): Connectivity.Status {
        return checkConnection().also { _status.update { it } }
    }

    private fun poll() {
        job = launch {
            while (isActive) {
                val status = checkConnection()
                _status.update { status }
                delay(httpOptions.pollingIntervalMs)
            }
        }
    }

    private suspend fun checkConnection(): Connectivity.Status {
        val isConnected = httpOptions.hosts.any { host ->
            makeRequest(host, httpOptions.port)
        }

        return if (isConnected) Connectivity.Status.Connected(metered = false)
        else Connectivity.Status.Disconnected
    }

    private suspend fun makeRequest(host: String, port: Int): Boolean {
        try {
            val response = httpClient.request {
                this.host = host
                this.port = port
                method = httpOptions.method
                timeout {
                    requestTimeoutMillis = httpOptions.timeoutMs
                }
            }

            httpOptions.onHttpResponse?.invoke(response)

            return response.status.isSuccess()
        } catch (cause: Throwable) {
            if (cause is CancellationException) throw cause

            return false
        }
    }
}