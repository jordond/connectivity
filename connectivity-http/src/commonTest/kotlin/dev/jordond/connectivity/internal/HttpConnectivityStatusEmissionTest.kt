package dev.jordond.connectivity.internal

import dev.jordond.connectivity.Connectivity
import dev.jordond.connectivity.HttpConnectivityOptions
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respondOk
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.test.runTest
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test

/**
 * Pins that [HttpConnectivity.status] publishes its result to [Connectivity.statusUpdates].
 *
 * Separate file for the same reason as `DefaultConnectivityStatusEmissionTest`: the existing
 * [HttpConnectivityTest] is frozen so that its byte-identity proves the Phase 2 refactor changed
 * no behaviour.
 */
class HttpConnectivityStatusEmissionTest {

    private lateinit var sutScope: CoroutineScope
    private lateinit var mockEngine: MockEngine
    private lateinit var httpClient: HttpClient

    @BeforeTest
    fun setup() {
        sutScope = CoroutineScope(Dispatchers.Default)
        mockEngine = MockEngine { respondOk() }
        httpClient = HttpClient(mockEngine)
    }

    @AfterTest
    fun cleanup() {
        httpClient.close()
        mockEngine.close()
        sutScope.cancel()
    }

    @Test
    fun statusPublishesItsResultToStatusUpdates() = runTest {
        val connectivity = createConnectivity()

        // Nothing has been published yet, so a later non-empty cache can only come from status().
        connectivity.statusUpdates.replayCache.shouldBeEmpty()

        val returned = connectivity.status()

        returned.shouldBeInstanceOf<Connectivity.Status.Connected>()
        connectivity.statusUpdates.replayCache shouldBe listOf(returned)
    }

    /**
     * `status()` populates the cache without [Connectivity.start], which is the case
     * `ConnectivityState.forceCheck()` actually exercises.
     */
    @Test
    fun statusPublishesWithoutPollingBeingStarted() = runTest {
        val connectivity = createConnectivity()

        connectivity.status()

        connectivity.monitoring.value shouldBe false
        connectivity.statusUpdates.replayCache.size shouldBe 1
        mockEngine.requestHistory.size shouldBe 1
    }

    private fun createConnectivity(): Connectivity = HttpConnectivity(
        parentScope = sutScope,
        httpOptions = HttpConnectivityOptions.build { autoStart = false },
        httpClient = httpClient,
    )
}
