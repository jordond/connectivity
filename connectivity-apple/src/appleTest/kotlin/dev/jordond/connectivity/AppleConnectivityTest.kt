package dev.jordond.connectivity

import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancel
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AppleConnectivityTest {

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
    fun shouldCreateConnectivityWithDefaultOptions() = testScope.runTest {
        val connectivity = Connectivity()

        connectivity.shouldNotBeNull()
        connectivity.shouldBeInstanceOf<Connectivity>()
        sutScope.advanceUntilIdle()
        connectivity.monitoring.value shouldBe false
    }

    @Test
    fun shouldCreateConnectivityWithCustomOptions() = testScope.runTest {
        val options = ConnectivityOptions(autoStart = true)

        val connectivity = Connectivity(
            options = options,
            scope = sutScope,
        )

        connectivity.shouldNotBeNull()
        connectivity.shouldBeInstanceOf<Connectivity>()
        sutScope.advanceUntilIdle()
        connectivity.monitoring.value shouldBe true
    }

    @Test
    fun shouldCreateAppleConnectivityWithDefaultOptions() = testScope.runTest {
        val connectivity = AppleConnectivity()

        connectivity.shouldNotBeNull()
        connectivity.shouldBeInstanceOf<Connectivity>()
        sutScope.advanceUntilIdle()
        connectivity.monitoring.value shouldBe false
    }

    @Test
    fun shouldCreateAppleConnectivityWithCustomOptions() = testScope.runTest {
        val options = ConnectivityOptions(autoStart = true)

        val connectivity = AppleConnectivity(
            options = options,
            scope = sutScope,
        )

        connectivity.shouldNotBeNull()
        connectivity.shouldBeInstanceOf<Connectivity>()
        sutScope.advanceUntilIdle()
        connectivity.monitoring.value shouldBe true
    }

    @Test
    fun shouldUseProvidedCoroutineScope() = testScope.runTest {
        val connectivity = Connectivity(scope = sutScope)

        connectivity.shouldNotBeNull()
        connectivity.shouldBeInstanceOf<Connectivity>()
    }
}