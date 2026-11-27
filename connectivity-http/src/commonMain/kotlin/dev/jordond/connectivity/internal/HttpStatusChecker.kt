package dev.jordond.connectivity.internal

import dev.jordond.connectivity.Connectivity
import dev.jordond.connectivity.HttpConnectivityOptions
import dev.jordond.connectivity.PollResult
import io.ktor.client.HttpClient
import io.ktor.client.plugins.timeout
import io.ktor.client.request.request
import io.ktor.http.isSuccess
import kotlinx.coroutines.CancellationException

/**
 * Performs a single HTTP connectivity check against [options]' urls, using [httpClient].
 *
 * Shared by the hot [HttpConnectivity] and the cold `httpConnectivityFlow`, so both paths have
 * identical request/retry/callback semantics.
 */
internal class HttpStatusChecker(
    private val options: HttpConnectivityOptions,
    private val httpClient: HttpClient,
) {

    suspend fun check(): Connectivity.Status {
        var isConnected = false
        for (url in options.urls) {
            isConnected = makeRequest(url, options.port)
            if (isConnected) break
        }

        return if (isConnected) Connectivity.Status.Connected(metered = false)
        else Connectivity.Status.Disconnected
    }

    private suspend fun makeRequest(url: String, port: Int): Boolean {
        val (protocol, host) = getProtocolAndHost(url, port)

        try {
            val response = httpClient.request {
                this.url {
                    this.protocol = protocol
                    this.host = host
                    this.port = port
                    method = options.method
                }

                timeout {
                    requestTimeoutMillis = options.timeoutMs
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
            options.onPollResult?.invoke(result)
        } catch (cause: CancellationException) {
            throw cause
        } catch (_: Throwable) {
            // Ignored, the callback is a notification and its failures are the consumer's problem.
        }
    }
}
