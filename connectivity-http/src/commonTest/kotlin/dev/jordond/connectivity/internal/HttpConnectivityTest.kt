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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test

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

    private suspend fun testConnectivity(
        httpClient: HttpClient = this.httpClient,
        configure: HttpConnectivityOptions.Builder.() -> Unit = {},
        test: suspend (Connectivity) -> Unit,
    ) {
        val scope = CoroutineScope(Dispatchers.Default)
        val options = HttpConnectivityOptions.build(configure)
        val connectivity = HttpConnectivity(scope, options, httpClient)
        test(connectivity)
        scope.cancel()
    }
}