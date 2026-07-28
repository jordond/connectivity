package dev.jordond.connectivity

import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlin.test.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ConnectivityFlowTest {

    @Test
    fun emitsProviderStatusesInOrder() = runTest {
        val provider = ConnectivityProvider(
            flowOf(Connectivity.Status.Connected(false), Connectivity.Status.Disconnected),
        )

        val result = connectivityFlow(provider).toList()

        result shouldBe listOf(Connectivity.Status.Connected(false), Connectivity.Status.Disconnected)
    }

    @Test
    fun isColdAndDoesNotSubscribeUntilCollected() = runTest {
        val provider = CountingProvider()

        val flow = connectivityFlow(provider)
        provider.monitorCalls shouldBe 0

        flow.first()
        provider.monitorCalls shouldBe 1
    }

    @Test
    fun subscribesOncePerCollector() = runTest {
        val provider = CountingProvider()

        val flow = connectivityFlow(provider)
        flow.first()
        flow.first()

        provider.monitorCalls shouldBe 2
    }

    @Test
    fun unsubscribesSourceWhenCollectionCancelled() = runTest {
        var finallyRuns = 0
        val provider = object : ConnectivityProvider {
            override fun monitor(): Flow<Connectivity.Status> = flow {
                try {
                    emit(Connectivity.Status.Connected(false))
                    awaitCancellation()
                } finally {
                    finallyRuns++
                }
            }
        }

        // A bare TestScope Job does not fire invokeOnCompletion, so the collecting job needs a
        // real Job on an unconfined dispatcher for cancellation to actually propagate here.
        val scope = CoroutineScope(Job() + UnconfinedTestDispatcher(testScheduler))
        val job = scope.launch {
            connectivityFlow(provider).collect { }
        }

        job.cancel()
        job.join()

        finallyRuns shouldBe 1
    }

    @Test
    fun firstReturnsOneOffStatusAndUnsubscribes() = runTest {
        var finallyRuns = 0
        val provider = ConnectivityProvider(
            flow {
                try {
                    emit(Connectivity.Status.Connected(false))
                    awaitCancellation()
                } finally {
                    finallyRuns++
                }
            },
        )

        val status = connectivityFlow(provider).first()

        status shouldBe Connectivity.Status.Connected(false)
        finallyRuns shouldBe 1
    }

    /**
     * #262 regression pin. A hot implementation that attaches a permanent child job to the
     * caller's scope (the pre-#262-fix `DefaultConnectivity` shape) would hang this job forever.
     */
    @Test
    fun doesNotOutliveOrBlockTheCallingScope() = runTest {
        val provider = ConnectivityProvider(
            flow {
                emit(Connectivity.Status.Connected(false))
                awaitCancellation()
            },
        )

        val scope = CoroutineScope(Job() + UnconfinedTestDispatcher(testScheduler))
        val job = scope.launch {
            connectivityFlow(provider).first()
        }
        job.join()

        job.isCompleted.shouldBeTrue()
        scope.isActive.shouldBeTrue()

        var launchedAfter = false
        scope.launch { launchedAfter = true }.join()
        launchedAfter.shouldBeTrue()
    }

    /**
     * Pins decision E (no dedupe) so Phase 2 — which shares this flow behind `shareIn` to
     * reimplement the hot API — cannot silently drift from today's hot behaviour, where
     * Android's `onCapabilitiesChanged` fires repeated, non-distinct `Connected` values.
     */
    @Test
    fun doesNotDeduplicateRepeatedStatuses() = runTest {
        val provider = ConnectivityProvider(
            flowOf(Connectivity.Status.Connected(false), Connectivity.Status.Connected(false)),
        )

        val result = connectivityFlow(provider).toList()

        result shouldBe listOf(
            Connectivity.Status.Connected(false),
            Connectivity.Status.Connected(false),
        )
    }

    /**
     * A provider that counts calls to [monitor] itself, not just collections of a pre-built flow.
     *
     * [ConnectivityProvider.monitor]'s contract is to *start* monitoring, and real implementations
     * allocate platform resources in it — `AppleConnectivityProvider` calls `nw_path_monitor_create`
     * before returning its `callbackFlow`. A fixture built with `ConnectivityProvider(someFlow)`
     * cannot see when `monitor()` runs, so it cannot tell a cold factory from an eager one.
     */
    private class CountingProvider : ConnectivityProvider {
        var monitorCalls = 0
            private set

        override fun monitor(): Flow<Connectivity.Status> {
            monitorCalls++
            return flowOf(Connectivity.Status.Connected(false))
        }
    }
}
