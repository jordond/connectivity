package dev.jordond.connectivity

import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test

class ConnectivityTest {

    private lateinit var testScope: TestScope
    private lateinit var sutScope: CoroutineScope
    private lateinit var provider: ConnectivityProvider

    @BeforeTest
    fun setup() {
        testScope = TestScope()
        sutScope = CoroutineScope(Dispatchers.Default)
        provider = ConnectivityProvider(flowOf(Connectivity.Status.Connected(false)))
    }

    @AfterTest
    fun cleanup() {
        testScope.cancel()
        sutScope.cancel()
    }

    @Test
    fun shouldCreateConnectivityWithDefaultOptions() = testScope.runTest {
        val connectivity = Connectivity(
            provider = provider,
            scope = sutScope,
        )

        connectivity.shouldNotBeNull()
        connectivity.shouldBeInstanceOf<Connectivity>()
        connectivity.monitoring.value shouldBe false
    }

    @Test
    fun shouldCreateConnectivityWithCustomOptions() = testScope.runTest {
        val options = ConnectivityOptions(autoStart = true)

        val connectivity = Connectivity(
            provider = provider,
            options = options,
            scope = sutScope,
        )

        connectivity.shouldNotBeNull()
        connectivity.shouldBeInstanceOf<Connectivity>()
        connectivity.monitoring.value shouldBe true
    }

    @Test
    fun shouldCreateConnectivityWithBuilderBlock() = testScope.runTest {
        val connectivity = Connectivity(
            provider = provider,
            scope = sutScope,
        ) {
            autoStart = true
        }

        connectivity.shouldNotBeNull()
        connectivity.shouldBeInstanceOf<Connectivity>()
        testScheduler.advanceUntilIdle()
        connectivity.monitoring.value shouldBe true
    }

    @Test
    fun shouldUseProvidedCoroutineScope() = testScope.runTest {
        val connectivity = Connectivity(
            provider = provider,
            scope = sutScope,
        )

        connectivity.shouldNotBeNull()
        connectivity.shouldBeInstanceOf<Connectivity>()
    }
}