package dev.jordond.connectivity

import dev.jordond.connectivity.tools.ContextProvider
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.unmockkObject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancel
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AndroidConnectivityTest {

    private lateinit var testScope: TestScope
    private lateinit var sutScope: TestScope
    private lateinit var contextProvider: ContextProvider

    @BeforeTest
    fun setup() {
        testScope = TestScope()
        sutScope = TestScope()
        contextProvider = mockk()
        mockkObject(ContextProvider.Companion)
        every { ContextProvider.getInstance() } returns contextProvider
        every { contextProvider.context } returns mockk()
    }

    @AfterTest
    fun cleanup() {
        testScope.cancel()
        sutScope.cancel()
        unmockkObject(ContextProvider.Companion)
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
    fun shouldCreateAndroidConnectivityWithDefaultOptions() = testScope.runTest {
        val connectivity = AndroidConnectivity()

        connectivity.shouldNotBeNull()
        connectivity.shouldBeInstanceOf<Connectivity>()
        sutScope.advanceUntilIdle()
        connectivity.monitoring.value shouldBe false
    }

    @Test
    fun shouldCreateAndroidConnectivityWithCustomOptions() = testScope.runTest {
        val options = ConnectivityOptions(autoStart = true)

        val connectivity = AndroidConnectivity(
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