package dev.jordond.connectivity

import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respondError
import io.ktor.client.engine.mock.respondOk
import io.ktor.http.HttpStatusCode.Companion.InternalServerError
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.time.Duration.Companion.seconds

class HttpConnectivityFlowTest {

    private lateinit var mockEngine: MockEngine
    private lateinit var httpClient: HttpClient

    @BeforeTest
    fun setup() {
        mockEngine = MockEngine { respondOk() }
        httpClient = HttpClient(mockEngine)
    }

    @AfterTest
    fun cleanup() {
        httpClient.close()
        mockEngine.close()
    }

    @Test
    fun emitsConnectedOnSuccessfulResponse() = runTest {
        val status = httpConnectivityFlow(httpClient = httpClient).first()

        status.shouldBeInstanceOf<Connectivity.Status.Connected>()
    }

    @Test
    fun emitsDisconnectedOnErrorResponse() = runTest {
        mockEngine = MockEngine { respondError(InternalServerError) }
        httpClient = HttpClient(mockEngine)

        val status = httpConnectivityFlow(httpClient = httpClient).first()

        status.shouldBeInstanceOf<Connectivity.Status.Disconnected>()
    }

    @Test
    fun emitsDisconnectedWhenRequestThrows() = runTest {
        mockEngine = MockEngine { throw IllegalStateException("Test exception") }
        httpClient = HttpClient(mockEngine)

        val status = httpConnectivityFlow(httpClient = httpClient).first()

        status.shouldBeInstanceOf<Connectivity.Status.Disconnected>()
    }

    @Test
    fun makesNoRequestUntilCollected() = runTest {
        httpConnectivityFlow(httpClient = httpClient)

        mockEngine.requestHistory.shouldBeEmpty()
    }

    /**
     * The loop emits before it delays, matching the existing `HttpConnectivity.poll()`. With an
     * hour-long interval, `.first()` would hang if that order were reversed.
     *
     * This runs on a real dispatcher with a real timeout because `runTest`'s virtual clock skips
     * `delay()`, which makes both orderings return instantly and the test useless.
     */
    @Test
    fun emitsBeforeTheFirstDelay() = runTest {
        withContext(Dispatchers.Default.limitedParallelism(1)) {
            withTimeout(3.seconds) {
                val options = HttpConnectivityOptions.build { pollingIntervalMs = 60 * 60_000L }

                val status = httpConnectivityFlow(options, httpClient).first()

                status.shouldBeInstanceOf<Connectivity.Status.Connected>()
            }
        }
    }

    /**
     * Real dispatcher and real timeout, the same workaround `HttpConnectivityTest` already uses
     * for its MockEngine tests, since `runTest`'s virtual clock does not coordinate with
     * MockEngine's dispatch.
     */
    @Test
    fun pollsRepeatedlyAtTheConfiguredInterval() = runTest {
        withContext(Dispatchers.Default.limitedParallelism(1)) {
            withTimeout(5.seconds) {
                val options = HttpConnectivityOptions.build { pollingIntervalMs = 10 }

                httpConnectivityFlow(options, httpClient).take(3).toList()

                mockEngine.requestHistory.size shouldBe 3
            }
        }
    }

    @Test
    fun invokesOnPollResultPerRequest() = runTest {
        var callbackCount = 0
        val options = HttpConnectivityOptions.build { onPollResult { callbackCount++ } }

        httpConnectivityFlow(options, httpClient).first()

        // Concrete counts, not one counter against the other, which would pass at 0 == 0.
        mockEngine.requestHistory.size shouldBe 1
        callbackCount shouldBe 1
    }

    @Test
    fun throwingOnPollResultDoesNotStopTheFlow() = runTest {
        withContext(Dispatchers.Default.limitedParallelism(1)) {
            withTimeout(5.seconds) {
                val options = HttpConnectivityOptions.build {
                    pollingIntervalMs = 10
                    onPollResult { error("Consumer callback blew up") }
                }

                val statuses = httpConnectivityFlow(options, httpClient).take(2).toList()

                statuses.size shouldBe 2
            }
        }
    }

    @Test
    fun runsAnIndependentLoopPerCollector() = runTest {
        val flow = httpConnectivityFlow(httpClient = httpClient)

        flow.first()
        flow.first()

        mockEngine.requestHistory.size shouldBe 2
    }

    @Test
    fun builderOverloadConfiguresTheSameFlow() = runTest {
        httpConnectivityFlow(httpClient) { url("example.com") }.first()

        mockEngine.requestHistory.first().url.host shouldBe "example.com"
    }
}
