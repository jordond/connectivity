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
     * Pins decision F (emit, then delay) — parity with the existing `HttpConnectivity.poll()`
     * loop. An hour-long interval would make `.first()` hang if the loop delayed before its
     * first emission.
     *
     * Runs on a real dispatcher with a real-time bound: `runTest`'s virtual clock auto-skips
     * `delay()`, so a virtual-time version of this test cannot distinguish "emit then delay"
     * from "delay then emit" — both return instantly regardless of ordering.
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
     * The riskiest test in the plan (§7.1) — real dispatcher + real-time bound, matching the
     * mitigation the existing `HttpConnectivityTest` already uses for MockEngine-backed tests,
     * since `runTest`'s virtual clock does not reliably coordinate with MockEngine's dispatch.
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

        callbackCount shouldBe mockEngine.requestHistory.size
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
