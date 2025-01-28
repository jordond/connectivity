package dev.jordond.connectivity

import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kotlin.test.Test

class ConnectivityProviderTest {

    @Test
    fun factoryFunctionCreatesProvider() {
        val flow = flowOf(Connectivity.Status.Connected(false))
        val provider = ConnectivityProvider(flow)

        provider.shouldBeInstanceOf<ConnectivityProvider>()
    }

    @Test
    fun extensionFunctionCreatesProvider() {
        val flow = flowOf(Connectivity.Status.Connected(false))
        val provider = flow.asProvider()

        provider.shouldBeInstanceOf<ConnectivityProvider>()
    }

    @Test
    fun monitorReturnsCorrectFlow() = runTest {
        val status = Connectivity.Status.Connected(false)
        val flow = flowOf(status)
        val provider = ConnectivityProvider(flow)

        val result = provider.monitor().first()

        result shouldBe status
    }

    @Test
    fun extensionFunctionMonitorReturnsCorrectFlow() = runTest {
        val status = Connectivity.Status.Connected(false)
        val flow = flowOf(status)
        val provider = flow.asProvider()

        val result = provider.monitor().first()

        result shouldBe status
    }

    @Test
    fun disconnectedStatusIsEmittedCorrectly() = runTest {
        val status = Connectivity.Status.Disconnected
        val flow = flowOf(status)
        val provider = ConnectivityProvider(flow)

        val result = provider.monitor().first()

        result shouldBe status
    }

    @Test
    fun connectedStatusWithMeteredIsEmittedCorrectly() = runTest {
        val status = Connectivity.Status.Connected(metered = true)
        val flow = flowOf(status)
        val provider = ConnectivityProvider(flow)

        val result = provider.monitor().first()

        result shouldBe status
    }
}