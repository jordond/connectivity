package dev.jordond.connectivity.internal

import dev.jordond.connectivity.Connectivity
import dev.jordond.connectivity.HttpConnectivityOptions
import dev.jordond.connectivity.PollResult
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.engine.mock.respondError
import io.ktor.client.engine.mock.respondOk
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.http.HttpStatusCode.Companion.InternalServerError
import io.ktor.http.URLProtocol
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.time.Duration.Companion.seconds

class HttpConnectivityTest {

    private lateinit var scope: TestScope
    private lateinit var mockEngine: MockEngine
    private lateinit var httpClient: HttpClient

    @BeforeTest
    fun setup() {
        scope = TestScope()
        mockEngine = MockEngine { respondOk() }
        httpClient = HttpClient(mockEngine)
    }

    @AfterTest
    fun cleanup() {
        httpClient.close()
        mockEngine.close()
        scope.cancel()
    }

    @Test
    fun shouldStartMonitoringWhenAutoStartIsTrue() = scope.runTest {
        testConnectivity(configure = { autoStart = true }) { connectivity ->
            connectivity.monitoring.value.shouldBeTrue()
        }
    }

    @Test
    fun shouldNotStartMonitoringWhenAutoStartIsFalse() = scope.runTest {
        testConnectivity(configure = { autoStart = false }) { connectivity ->
            connectivity.monitoring.value.shouldBeFalse()
        }
    }

    @Test
    fun shouldEmitConnectedStatusWhenRequestSucceeds() = scope.runTest {
        testConnectivity { connectivity ->
            connectivity.start()

            val status = connectivity.statusUpdates.first()
            status.shouldBeInstanceOf<Connectivity.Status.Connected>()
        }
    }

    @Test
    fun shouldEmitDisconnectedStatusWhenRequestFails() = scope.runTest {
        mockEngine = MockEngine { _ ->
            respondError(InternalServerError)
        }
        httpClient = HttpClient(mockEngine)

        testConnectivity { connectivity ->
            connectivity.start()

            val status = connectivity.statusUpdates.first()
            status.shouldBeInstanceOf<Connectivity.Status.Disconnected>()
        }
    }

    @Test
    fun shouldTryMultipleUrlsUntilSuccess() = scope.runTest {
        mockEngine = MockEngine { request ->
            if (request.url.host.contains("failed")) {
                respondError(InternalServerError)
            } else {
                respond(content = "OK", status = HttpStatusCode.OK)
            }
        }
        httpClient = HttpClient(mockEngine)

        testConnectivity(
            configure = { urls("failed.com", "failed2.com", "success.com") },
        ) { connectivity ->
            val status = connectivity.status()
            status.shouldBeInstanceOf<Connectivity.Status.Connected>()
        }
    }

    @Test
    fun shouldTryMultipleUrlsUntilFailure() = scope.runTest {
        mockEngine = MockEngine {
            respond(content = "Err", status = InternalServerError)
        }
        httpClient = HttpClient(mockEngine)

        testConnectivity(
            configure = { urls("failed.com", "failed2.com", "failed3.com") },
        ) { connectivity ->
            val status = connectivity.status()
            status.shouldBeInstanceOf<Connectivity.Status.Disconnected>()
        }
    }

    @Test
    fun shouldInvokeCallbackWithSuccessfulResponse() = scope.runTest {
        var lastResult: PollResult? = null
        testConnectivity(
            configure = {
                onPollResult { lastResult = it }
            }
        ) { connectivity ->
            connectivity.status()
            lastResult.shouldNotBeNull()
            lastResult.shouldBeInstanceOf<PollResult.Response>()

            val response = lastResult as PollResult.Response
            response.response.status shouldBe HttpStatusCode.OK
            response.isSuccess.shouldBeTrue()
        }
    }

    @Test
    fun shouldInvokeCallbackWithFailureResponse() = scope.runTest {
        mockEngine = MockEngine { _ ->
            respondError(InternalServerError)
        }
        httpClient = HttpClient(mockEngine)

        var lastResult: PollResult? = null
        testConnectivity(
            configure = {
                onPollResult { lastResult = it }
            }
        ) { connectivity ->
            connectivity.status()
            lastResult.shouldNotBeNull()
            lastResult.shouldBeInstanceOf<PollResult.Response>()

            val response = lastResult as PollResult.Response
            response.response.status shouldBe InternalServerError
            response.isFailure.shouldBeTrue()
        }
    }

    @Test
    fun shouldHandleExceptionInMakeRequest() = scope.runTest(timeout = 3.seconds) {
        mockEngine = MockEngine {
            throw IllegalStateException("Test exception")
        }
        httpClient = HttpClient(mockEngine)

        var lastResult: PollResult? = null
        testConnectivity(
            configure = {
                onPollResult { lastResult = it }
            }
        ) { connectivity ->
            val status = connectivity.status()

            status.shouldBeInstanceOf<Connectivity.Status.Disconnected>()
            lastResult.shouldNotBeNull()
            lastResult.shouldBeInstanceOf<PollResult.Error>()

            val error = lastResult as PollResult.Error
            error.throwable.shouldBeInstanceOf<IllegalStateException>()
            error.throwable.message shouldBe "Test exception"
        }
    }

    @Test
    fun shouldUseSpecifiedHttpMethod() = scope.runTest {
        var lastMethod: HttpMethod? = null
        mockEngine = MockEngine { request ->
            lastMethod = request.method
            respond(content = "OK", status = HttpStatusCode.OK)
        }
        httpClient = HttpClient(mockEngine)

        testConnectivity(configure = { method = HttpMethod.Post }) { connectivity ->
            connectivity.status()

            lastMethod shouldBe HttpMethod.Post
        }
    }

    @Test
    fun shouldParseUrlWithHttpPrefix() {
        val (protocol, host) = getProtocolAndHost("http://example.com", 80)

        protocol shouldBe URLProtocol.HTTP
        host shouldBe "example.com"
    }

    @Test
    fun shouldParseUrlWithHttpsPrefix() {
        val (protocol, host) = getProtocolAndHost("https://example.com", 80)

        protocol shouldBe URLProtocol.HTTPS
        host shouldBe "example.com"
    }

    @Test
    fun shouldDefaultToHttpsWhenPort443() {
        val (protocol, host) = getProtocolAndHost("example.com", 443)

        protocol shouldBe URLProtocol.HTTPS
        host shouldBe "example.com"
    }

    @Test
    fun shouldDefaultToHttpWhenNonSecurePort() {
        val (protocol, host) = getProtocolAndHost("example.com", 8080)

        protocol shouldBe URLProtocol.HTTP
        host shouldBe "example.com"
    }

    @Test
    fun shouldStopMonitoringWhenStopIsCalled() = scope.runTest {
        testConnectivity { connectivity ->
            connectivity.start()
            connectivity.monitoring.value.shouldBeTrue()

            connectivity.stop()
            connectivity.monitoring.value.shouldBeFalse()
        }
    }

    @Test
    fun shouldHandleStopWhenNotMonitoring() = scope.runTest {
        testConnectivity { connectivity ->
            // Verify initial state
            connectivity.monitoring.value.shouldBeFalse()

            // Should not throw or change state
            connectivity.stop()
            connectivity.monitoring.value.shouldBeFalse()
        }
    }

    @Test
    fun shouldNotStartNewJobWhenAlreadyMonitoring() = scope.runTest {
        testConnectivity { connectivity ->
            connectivity.start()
            connectivity.monitoring.value.shouldBeTrue()

            // Should not affect monitoring state
            connectivity.start()
            connectivity.monitoring.value.shouldBeTrue()
        }
    }

    private suspend fun testConnectivity(
        httpClient: HttpClient = this.httpClient,
        scope: CoroutineScope = CoroutineScope(Dispatchers.Default),
        configure: HttpConnectivityOptions.Builder.() -> Unit = {},
        test: suspend (Connectivity) -> Unit,
    ) {
        val options = HttpConnectivityOptions.build(configure)
        val connectivity = HttpConnectivity(scope, options, httpClient)
        test(connectivity)
        scope.cancel()
    }
}