package dev.jordond.connectivity

import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ConnectivityTest {

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
    fun shouldCreateConnectivityWithDefaultOptions() = testScope.runTest {
        val connectivity = Connectivity(
            provider = provider,
            scope = sutScope,
        )

        connectivity.shouldNotBeNull()
        connectivity.shouldBeInstanceOf<Connectivity>()
        sutScope.advanceUntilIdle()
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
        sutScope.advanceUntilIdle()
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
        sutScope.advanceUntilIdle()
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