package dev.jordond.connectivity.internal

import dev.jordond.connectivity.HttpConnectivityOptions
import io.kotest.assertions.throwables.shouldThrow
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respondOk
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.test.runTest
import kotlin.test.AfterTest
import kotlin.test.Test

/**
 * Pins the two `CancellationException` rethrows in [HttpStatusChecker].
 *
 * Both are load-bearing and neither is covered by [HttpConnectivityTest] or by the cold
 * `HttpConnectivityFlowTest`: deleting either one leaves the whole module green while silently
 * turning "the collector was cancelled mid-request" into a plain `Disconnected` emission, which
 * breaks structured concurrency for both the hot and the cold call sites that now share this class.
 */
class HttpStatusCheckerTest {

    private lateinit var httpClient: HttpClient

    @AfterTest
    fun cleanup() {
        httpClient.close()
    }

    @Test
    fun rethrowsCancellationFromTheRequest() = runTest {
        httpClient = HttpClient(MockEngine { throw CancellationException("cancelled mid-request") })
        val checker = HttpStatusChecker(HttpConnectivityOptions(), httpClient)

        // Without the `if (cause is CancellationException) throw cause` guard in makeRequest, the
        // generic catch reports PollResult.Error and returns Disconnected instead.
        shouldThrow<CancellationException> { checker.check() }
    }

    @Test
    fun rethrowsCancellationFromThePollResultCallback() = runTest {
        httpClient = HttpClient(MockEngine { respondOk() })
        val options = HttpConnectivityOptions.build {
            onPollResult { throw CancellationException("cancelled in callback") }
        }
        val checker = HttpStatusChecker(options, httpClient)

        // Without the CancellationException branch in notifyPollResult, the `catch (_: Throwable)`
        // swallow below it eats the cancellation and check() returns normally.
        shouldThrow<CancellationException> { checker.check() }
    }
}
