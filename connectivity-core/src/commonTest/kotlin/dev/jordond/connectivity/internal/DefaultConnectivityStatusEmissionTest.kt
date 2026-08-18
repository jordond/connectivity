package dev.jordond.connectivity.internal

import dev.jordond.connectivity.Connectivity
import dev.jordond.connectivity.ConnectivityOptions
import dev.jordond.connectivity.ConnectivityProvider
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test

/**
 * Pins that [DefaultConnectivity.status] publishes its result to [Connectivity.statusUpdates].
 *
 * This lives in its own file because [DefaultConnectivityTest] is frozen for the Phase 2 refactor,
 * where byte-identical existing tests are the proof that reimplementing the hot API on the cold
 * flow changed no behaviour.
 *
 * The behaviour matters more than it looks. `ConnectivityState.forceCheck()` in
 * `connectivity-compose` calls `status()` and reads the result back off `statusUpdates`, so the
 * emission is the only thing connecting the two. It is also the reason the hot API cannot be
 * rebuilt on `shareIn`: a shared flow owns its replay cache, and nothing outside the upstream can
 * push into it.
 */
class DefaultConnectivityStatusEmissionTest {

    private lateinit var sutScope: TestScope

    @BeforeTest
    fun setup() {
        sutScope = TestScope()
    }

    @AfterTest
    fun cleanup() {
        sutScope.cancel()
    }

    @Test
    fun statusPublishesItsResultToStatusUpdates() = runTest {
        val connected = Connectivity.Status.Connected(metered = false)
        val connectivity = createConnectivity(ConnectivityProvider(flowOf(connected)))

        // Nothing has been published yet, so a later non-empty cache can only come from status().
        connectivity.statusUpdates.replayCache.shouldBeEmpty()

        val returned = connectivity.status()

        returned shouldBe connected
        connectivity.statusUpdates.replayCache shouldBe listOf(connected)
    }

    /**
     * `status()` populates the cache without [Connectivity.start], which is the case
     * `ConnectivityState.forceCheck()` actually exercises.
     */
    @Test
    fun statusPublishesWithoutMonitoringBeingStarted() = runTest {
        val connectivity = createConnectivity(
            ConnectivityProvider(flowOf(Connectivity.Status.Disconnected)),
        )

        connectivity.status()

        connectivity.monitoring.value shouldBe false
        connectivity.statusUpdates.replayCache shouldBe listOf(Connectivity.Status.Disconnected)
    }

    private fun createConnectivity(provider: ConnectivityProvider): Connectivity =
        DefaultConnectivity(sutScope, provider, ConnectivityOptions(autoStart = false))
}
