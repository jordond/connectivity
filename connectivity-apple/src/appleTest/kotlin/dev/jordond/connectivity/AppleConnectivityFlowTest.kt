package dev.jordond.connectivity

import io.kotest.matchers.nulls.shouldNotBeNull
import kotlinx.coroutines.flow.Flow
import kotlin.test.Test

class AppleConnectivityFlowTest {

    /**
     * Smoke test only — see §5.4's honesty clause. `AppleConnectivityProvider` drives a real
     * `NWPathMonitor` with no seam to observe or fake it, matching the ceiling of the existing
     * `AppleConnectivityTest`. There is no assertion here that can meaningfully fail against a
     * flow-returning implementation; it exists to note the ceiling, not to pretend otherwise.
     *
     * `startsNoMonitorUntilCollected` (5.4/2) is intentionally not written: constructing a fake
     * `NWPathMonitor` seam is out of Phase 1 scope, and without one there is no way to observe
     * whether the monitor started. The real coldness guarantee for this module comes from core
     * test `ConnectivityFlowTest.isColdAndDoesNotSubscribeUntilCollected`, since
     * `appleConnectivityFlow()` is a one-line delegation to `connectivityFlow(...)`.
     */
    @Test
    fun createsAFlow() {
        val flow: Flow<Connectivity.Status> = appleConnectivityFlow()

        flow.shouldNotBeNull()
    }
}
