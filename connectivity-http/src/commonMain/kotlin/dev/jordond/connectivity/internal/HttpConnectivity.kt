package dev.jordond.connectivity.internal

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

            httpOptions.onPollResult?.invoke(PollResult.Response(response))

            return response.status.isSuccess()
        } catch (cause: Throwable) {
            if (cause is CancellationException) throw cause

            httpOptions.onPollResult?.invoke(PollResult.Error(cause))
            return false
        }
    }

    fun getProtocolAndHost(url: String, port: Int): Pair<URLProtocol, String> {
        val protocol = when {
            url.startsWith("http://") -> URLProtocol.HTTP
            url.startsWith("https://") -> URLProtocol.HTTPS
            port == 443 -> URLProtocol.HTTPS
            else -> URLProtocol.HTTP
        }

        val host = url.removePrefix("http://").removePrefix("https://")
        return protocol to host
    }
}