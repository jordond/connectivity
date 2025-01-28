package dev.jordond.connectivity

import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respondOk
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test

class HttpConnectivityFactoryTest {

    private lateinit var testScope: TestScope
    private lateinit var sutScope: CoroutineScope
    private lateinit var mockEngine: MockEngine
    private lateinit var httpClient: HttpClient

    @BeforeTest
    fun setup() {
        testScope = TestScope()
        sutScope = CoroutineScope(Dispatchers.Default)
        mockEngine = MockEngine { respondOk() }
        httpClient = HttpClient(mockEngine)
    }

    @AfterTest
    fun cleanup() {
        httpClient.close()
        mockEngine.close()
        testScope.cancel()
    }

    @Test
    fun canCreateConnectivityWithDefaultOptions() = testScope.runTest {
        val connectivity = Connectivity(
            scope = sutScope,
            httpClient = httpClient,
        )

        connectivity.shouldNotBeNull()
        connectivity.shouldBeInstanceOf<Connectivity>()
        connectivity.monitoring.value shouldBe false
        sutScope.cancel()
    }

    @Test
    fun canCreateConnectivityWithCustomOptions() = testScope.runTest {
        val options = HttpConnectivityOptions.build {
            autoStart = true
            url("test.com")
            port = 8080
        }

        val connectivity = Connectivity(
            options = options,
            scope = sutScope,
            httpClient = httpClient,
        )

        connectivity.shouldNotBeNull()
        connectivity.shouldBeInstanceOf<Connectivity>()
        connectivity.monitoring.value shouldBe true
        sutScope.cancel()
    }

    @Test
    fun canCreateConnectivityWithBuilderBlock() = testScope.runTest {
        val connectivity = Connectivity(
            scope = sutScope,
            httpClient = httpClient,
        ) {
            autoStart = true
            url("test.com")
            port = 8080
        }

        connectivity.shouldNotBeNull()
        connectivity.shouldBeInstanceOf<Connectivity>()
        connectivity.monitoring.value shouldBe true
        sutScope.cancel()
    }

    @Test
    fun canForcePollUsingExtensionFunction() = testScope.runTest {
        val connectivity = Connectivity(
            scope = sutScope,
            httpClient = httpClient,
        )

        connectivity.start()
        connectivity.force()

        val status = connectivity.statusUpdates.first()
        status.shouldBeInstanceOf<Connectivity.Status.Connected>()
        sutScope.cancel()
    }
}