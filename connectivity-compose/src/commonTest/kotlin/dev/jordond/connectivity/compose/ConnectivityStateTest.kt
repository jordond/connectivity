package dev.jordond.connectivity.compose

import dev.jordond.connectivity.Connectivity
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlin.test.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ConnectivityStateTest {

    @Test
    fun shouldHaveNoStatusBeforeTheFirstUpdate() = runTest {
        val state = ConnectivityState(FakeConnectivity(), backgroundScope)
        runCurrent()

        state.status.shouldBeNull()
        state.isConnected.shouldBeFalse()
        state.isMetered.shouldBeFalse()
        state.isDisconnected.shouldBeFalse()
    }

    @Test
    fun shouldReadInitialMonitoringFromConnectivity() = runTest {
        val state = ConnectivityState(FakeConnectivity(monitoring = true), backgroundScope)

        state.isMonitoring.shouldBeTrue()
    }

    @Test
    fun shouldReflectConnectedStatus() = runTest {
        val connectivity = FakeConnectivity()
        val state = ConnectivityState(connectivity, backgroundScope)

        connectivity.updates.emit(Connectivity.Status.Connected(metered = false))
        runCurrent()

        state.status shouldBe Connectivity.Status.Connected(metered = false)
        state.isConnected.shouldBeTrue()
        state.isMetered.shouldBeFalse()
        state.isDisconnected.shouldBeFalse()
    }

    @Test
    fun shouldReflectMeteredStatus() = runTest {
        val connectivity = FakeConnectivity()
        val state = ConnectivityState(connectivity, backgroundScope)

        connectivity.updates.emit(Connectivity.Status.Connected(metered = true))
        runCurrent()

        state.isConnected.shouldBeTrue()
        state.isMetered.shouldBeTrue()
    }

    @Test
    fun shouldReflectDisconnectedStatus() = runTest {
        val connectivity = FakeConnectivity()
        val state = ConnectivityState(connectivity, backgroundScope)

        connectivity.updates.emit(Connectivity.Status.Disconnected)
        runCurrent()

        state.status shouldBe Connectivity.Status.Disconnected
        state.isConnected.shouldBeFalse()
        state.isMetered.shouldBeFalse()
        state.isDisconnected.shouldBeTrue()
    }

    @Test
    fun shouldFollowStatusChanges() = runTest {
        val connectivity = FakeConnectivity()
        val state = ConnectivityState(connectivity, backgroundScope)

        connectivity.updates.emit(Connectivity.Status.Connected(metered = false))
        runCurrent()
        connectivity.updates.emit(Connectivity.Status.Disconnected)
        runCurrent()

        state.status shouldBe Connectivity.Status.Disconnected
    }

    @Test
    fun shouldStartAndStopMonitoring() = runTest {
        val connectivity = FakeConnectivity()
        val state = ConnectivityState(connectivity, backgroundScope)
        runCurrent()

        state.startMonitoring()
        runCurrent()
        state.isMonitoring.shouldBeTrue()
        connectivity.isMonitoring.shouldBeTrue()

        state.stopMonitoring()
        runCurrent()
        state.isMonitoring.shouldBeFalse()
        connectivity.isMonitoring.shouldBeFalse()
    }

    @Test
    fun shouldUpdateStatusWhenForceChecked() = runTest {
        val connectivity = FakeConnectivity(current = Connectivity.Status.Connected(metered = true))
        val state = ConnectivityState(connectivity, backgroundScope)
        runCurrent()

        state.forceCheck()
        runCurrent()

        connectivity.statusCalls shouldBe 1
        state.status shouldBe Connectivity.Status.Connected(metered = true)
        state.isMonitoring.shouldBeFalse()
    }

    private class FakeConnectivity(
        monitoring: Boolean = false,
        private val current: Connectivity.Status = Connectivity.Status.Disconnected,
    ) : Connectivity {

        val updates = MutableSharedFlow<Connectivity.Status>(replay = 1)
        var statusCalls = 0

        override val statusUpdates = updates
        override val monitoring = MutableStateFlow(monitoring)

        override suspend fun status(): Connectivity.Status {
            statusCalls++
            updates.emit(current)
            return current
        }

        override fun start() {
            monitoring.value = true
        }

        override fun stop() {
            monitoring.value = false
        }

        override fun close() {
            stop()
        }
    }
}
