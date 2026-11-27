package dev.jordond.connectivity

import android.content.Context
import android.net.ConnectivityManager
import dev.jordond.connectivity.tools.ContextProvider
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.unmockkObject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancel
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test

@OptIn(ExperimentalCoroutinesApi::class)
class DeviceConnectivityTest {

    private lateinit var testScope: TestScope
    private lateinit var sutScope: TestScope

    @BeforeTest
    fun setup() {
        testScope = TestScope()
        sutScope = TestScope()
        val contextProvider = mockk<ContextProvider>()
        mockkObject(ContextProvider.Companion)
        every { ContextProvider.getInstance() } returns contextProvider
        every { contextProvider.context } returns mockk {
            // No ConnectivityManager, so the provider emits Disconnected without touching the
            // Android framework, which isn't available in a host test.
            every { getSystemService(Context.CONNECTIVITY_SERVICE) } returns null
            every { getSystemService(ConnectivityManager::class.java) } returns null
        }
    }

    @AfterTest
    fun cleanup() {
        testScope.cancel()
        sutScope.cancel()
        unmockkObject(ContextProvider.Companion)
    }

    @Test
    fun shouldCreateConnectivityWithOptions() = testScope.runTest {
        val connectivity = Connectivity(ConnectivityOptions(autoStart = true), sutScope)
        sutScope.advanceUntilIdle()

        connectivity.monitoring.value shouldBe true
        connectivity.status() shouldBe Connectivity.Status.Disconnected
    }

    @Test
    fun shouldCreateConnectivityWithBuilderBlock() = testScope.runTest {
        val connectivity = Connectivity(scope = sutScope) {
            autoStart = true
        }
        sutScope.advanceUntilIdle()

        connectivity.monitoring.value shouldBe true
    }

    @Test
    fun shouldNotStartMonitoringByDefault() = testScope.runTest {
        val connectivity = Connectivity(scope = sutScope)
        sutScope.advanceUntilIdle()

        connectivity.monitoring.value shouldBe false
    }
}
