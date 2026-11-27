package dev.jordond.connectivity

import io.kotest.matchers.shouldBe
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
    }

    @AfterTest
    fun cleanup() {
        testScope.cancel()
        sutScope.cancel()
    }

    @Test
    fun shouldCreateConnectivityWithOptions() = testScope.runTest {
        val connectivity = Connectivity(ConnectivityOptions(autoStart = true), sutScope)
        sutScope.advanceUntilIdle()

        connectivity.monitoring.value shouldBe true
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
