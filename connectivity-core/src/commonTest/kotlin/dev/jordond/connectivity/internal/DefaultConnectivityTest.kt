package dev.jordond.connectivity.internal

import dev.jordond.connectivity.Connectivity
import dev.jordond.connectivity.ConnectivityOptions
import dev.jordond.connectivity.ConnectivityProvider
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.types.shouldBeInstanceOf
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test

@OptIn(ExperimentalCoroutinesApi::class)
class DefaultConnectivityTest {

    private lateinit var testScope: TestScope
    private lateinit var sutScope: TestScope
    private lateinit var provider: ConnectivityProvider

    @BeforeTest
    fun setup() {
        testScope = TestScope()
        sutScope = TestScope()
        provider = ConnectivityProvider(flowOf(Connectivity.Status.Connected(false)))
    }

    @AfterTest
    fun cleanup() {
        testScope.cancel()
        sutScope.cancel()
    }

    @Test
    fun shouldStartMonitoringWhenAutoStartIsTrue() = testScope.runTest {
        val connectivity = createConnectivity(autoStart = true)
        sutScope.advanceUntilIdle()
        connectivity.monitoring.value.shouldBeTrue()
    }

    @Test
    fun shouldNotStartMonitoringWhenAutoStartIsFalse() = testScope.runTest {
        val connectivity = createConnectivity(autoStart = false)
        sutScope.advanceUntilIdle()
        connectivity.monitoring.value.shouldBeFalse()
    }

    @Test
    fun shouldEmitConnectedStatusWhenStarted() = testScope.runTest {
        val connectivity = createConnectivity()
        connectivity.start()
        sutScope.advanceUntilIdle()

        val status = connectivity.statusUpdates.first()
        status.shouldBeInstanceOf<Connectivity.Status.Connected>()
        status.isConnected.shouldBeTrue()
    }

    @Test
    fun shouldStopMonitoringWhenStopIsCalled() = testScope.runTest {
        val connectivity = createConnectivity(autoStart = true)
        sutScope.advanceUntilIdle()
        connectivity.monitoring.value.shouldBeTrue()

        connectivity.stop()
        sutScope.advanceUntilIdle()
        connectivity.monitoring.value.shouldBeFalse()
    }

    @Test
    fun shouldReturnCurrentStatusWhenRequested() = testScope.runTest {
        val connectivity = createConnectivity()
        val status = connectivity.status()
        sutScope.advanceUntilIdle()

        status.shouldNotBeNull()
        status.shouldBeInstanceOf<Connectivity.Status.Connected>()
        status.isConnected.shouldBeTrue()
    }

    @Test
    fun shouldEmitDisconnectedStatus() = testScope.runTest {
        provider = ConnectivityProvider(flowOf(Connectivity.Status.Disconnected))
        val connectivity = createConnectivity(autoStart = true)
        sutScope.advanceUntilIdle()

        val status = connectivity.statusUpdates.first()
        status.shouldBeInstanceOf<Connectivity.Status.Disconnected>()
        status.isDisconnected.shouldBeTrue()
    }

    @Test
    fun shouldEmitMeteredStatus() = testScope.runTest {
        provider = ConnectivityProvider(flowOf(Connectivity.Status.Connected(metered = true)))
        val connectivity = createConnectivity()
        connectivity.start()
        sutScope.advanceUntilIdle()

        val status = connectivity.statusUpdates.first()
        status.shouldBeInstanceOf<Connectivity.Status.Connected>()
        status.isMetered.shouldBeTrue()
    }

    private fun createConnectivity(autoStart: Boolean = false): Connectivity {
        val options = ConnectivityOptions(autoStart = autoStart)
        return DefaultConnectivity(sutScope, provider, options)
    }
}