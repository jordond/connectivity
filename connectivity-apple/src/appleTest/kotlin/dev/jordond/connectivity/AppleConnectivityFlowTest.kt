package dev.jordond.connectivity

import io.kotest.matchers.nulls.shouldNotBeNull
import kotlinx.coroutines.flow.Flow
import kotlin.test.Test

class AppleConnectivityFlowTest {

    /**
     * Smoke test only. `AppleConnectivityProvider` drives a real `NWPathMonitor` with no seam to
     * observe or fake it, so there is no assertion here that can fail against any implementation
     * that returns a flow. This is the same ceiling the existing `AppleConnectivityTest` hits.
     *
     * There is deliberately no test that the monitor stays unstarted until collection, because
     * without a fake `NWPathMonitor` there is no way to observe it. That guarantee is covered in
     * core by `ConnectivityFlowTest.isColdAndDoesNotSubscribeUntilCollected`, since
     * `appleConnectivityFlow()` just delegates to `connectivityFlow(...)`.
     */
    @Test
    fun createsAFlow() {
        val flow: Flow<Connectivity.Status> = appleConnectivityFlow()

        flow.shouldNotBeNull()
    }
}
