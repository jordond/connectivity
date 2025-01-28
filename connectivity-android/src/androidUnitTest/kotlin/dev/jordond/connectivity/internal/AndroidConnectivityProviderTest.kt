@file:Suppress("DEPRECATION")

package dev.jordond.connectivity.internal

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkInfo
import android.net.NetworkRequest
import android.os.Build
import app.cash.turbine.test
import dev.jordond.connectivity.Connectivity
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkConstructor
import io.mockk.slot
import io.mockk.verify
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test

class AndroidConnectivityProviderTest {

    private lateinit var context: Context
    private lateinit var connectivityManager: ConnectivityManager
    private lateinit var provider: AndroidConnectivityProvider
    private lateinit var networkCallback: ConnectivityManager.NetworkCallback
    private lateinit var versionCodeProvider: VersionCodeProvider
    private val callbackSlot = slot<ConnectivityManager.NetworkCallback>()

    @BeforeTest
    fun setup() {
        // Version to use newer network callback
        versionCodeProvider = versionCodeProvider(Build.VERSION_CODES.N)

        mockkConstructor(NetworkRequest.Builder::class)
        every { anyConstructed<NetworkRequest.Builder>().build() } returns mockk(relaxed = true)

        connectivityManager = mockk(relaxed = true)
        context = mockk {
            every { getSystemService(Context.CONNECTIVITY_SERVICE) } returns connectivityManager
        }
        provider = AndroidConnectivityProvider(context, versionCodeProvider)
    }

    @Test
    fun shouldReturnDisconnectedWhenNoConnectivityManager() = runTest {
        val contextWithoutManager = mockk<Context> {
            every { getSystemService(Context.CONNECTIVITY_SERVICE) } returns null
        }
        val provider = AndroidConnectivityProvider(contextWithoutManager)
        val status = provider.monitor().first()
        status.shouldBeInstanceOf<Connectivity.Status.Disconnected>()
    }

    @Test
    fun shouldEmitConnectedStatusWhenNetworkIsAvailable() = runTest {
        val network = mockk<Network>()
        val capabilities = mockk<NetworkCapabilities> {
            every { hasTransport(NetworkCapabilities.TRANSPORT_WIFI) } returns true
            every { hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) } returns false
        }
        every { connectivityManager.getNetworkCapabilities(network) } returns capabilities
        every { connectivityManager.isActiveNetworkMetered } returns false

        provider.monitor().test {
            triggerNetworkCallback()

            // Initial status
            skipItems(1)

            networkCallback.onAvailable(network)
            val status = awaitItem()
            status.shouldBeInstanceOf<Connectivity.Status.Connected>()
            status.metered shouldBe false
        }
    }

    @Test
    fun shouldEmitDisconnectedStatusWhenNetworkIsLost() = runTest {
        val network = mockk<Network>()
        provider.monitor().test {
            triggerNetworkCallback()

            // Initial status
            skipItems(1)

            networkCallback.onLost(network)
            val status = awaitItem()
            status.shouldBeInstanceOf<Connectivity.Status.Disconnected>()
        }
    }

    @Test
    fun shouldEmitDisconnectedStatusWhenNetworkIsLostOnOlderAndroid() = runTest {
        versionCodeProvider = versionCodeProvider(Build.VERSION_CODES.LOLLIPOP)
        provider = AndroidConnectivityProvider(context, versionCodeProvider)

        val network = mockk<Network>()
        provider.monitor().test {
            triggerNetworkCallback()

            // Initial status
            skipItems(1)

            networkCallback.onLost(network)
            val status = awaitItem()
            status.shouldBeInstanceOf<Connectivity.Status.Disconnected>()
        }
    }

    @Test
    fun shouldEmitMeteredStatusWhenOnCellularNetwork() = runTest {
        val network = mockk<Network>()
        val capabilities = mockk<NetworkCapabilities> {
            every { hasTransport(NetworkCapabilities.TRANSPORT_WIFI) } returns false
            every { hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) } returns true
        }
        every { connectivityManager.getNetworkCapabilities(network) } returns capabilities
        every { connectivityManager.isActiveNetworkMetered } returns true

        provider.monitor().test {
            triggerNetworkCallback()

            // Initial status
            skipItems(1)

            networkCallback.onAvailable(network)
            val status = awaitItem()
            status.shouldBeInstanceOf<Connectivity.Status.Connected>()
            status.metered shouldBe true
        }
    }

    @Test
    fun shouldUpdateStatusWhenCapabilitiesChange() = runTest {
        val network = mockk<Network>()
        val capabilities = mockk<NetworkCapabilities> {
            every { hasTransport(NetworkCapabilities.TRANSPORT_WIFI) } returns true
            every { hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) } returns false
        }
        every { connectivityManager.getNetworkCapabilities(network) } returns capabilities
        every { connectivityManager.isActiveNetworkMetered } returns false

        provider.monitor().test {
            triggerNetworkCallback()

            // Initial status
            skipItems(1)

            val updatedCapabilities = mockk<NetworkCapabilities> {
                every { hasTransport(NetworkCapabilities.TRANSPORT_WIFI) } returns false
                every { hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) } returns true
            }
            networkCallback.onCapabilitiesChanged(network, updatedCapabilities)
            val status = awaitItem()
            status.shouldBeInstanceOf<Connectivity.Status.Connected>()
            status.metered shouldBe true
        }
    }

    @Test
    fun shouldReturnConnectedWithMeteredFalseWhenOnUnmeteredWifi() = runTest {
        val (network, _) = setupNetworkCapabilities(
            isWifi = true,
            isCellular = false,
            isMetered = false,
        )
        verifyNetworkStatus(network, expectedMetered = false)
    }

    @Test
    fun shouldReturnConnectedWithMeteredTrueWhenOnCellular() = runTest {
        val (network, _) = setupNetworkCapabilities(
            isWifi = false,
            isCellular = true,
            isMetered = true,
        )
        verifyNetworkStatus(network, expectedMetered = true)
    }

    @Test
    fun shouldReturnConnectedWithMeteredTrueWhenOnMeteredWifi() = runTest {
        val (network, _) = setupNetworkCapabilities(
            isWifi = true,
            isCellular = false,
            isMetered = true,
        )
        verifyNetworkStatus(network, expectedMetered = true)
    }

    @Test
    fun shouldHandleNullCapabilities() = runTest {
        val network = mockk<Network>()
        every { connectivityManager.getNetworkCapabilities(network) } returns null
        every { connectivityManager.isActiveNetworkMetered } returns false
        verifyNetworkStatus(network, expectedMetered = true)
    }

    @Test
    fun shouldReturnMeteredTrueWhenBothWifiAndCellularAreFalse() = runTest {
        val (network, _) = setupNetworkCapabilities(
            isWifi = false,
            isCellular = false,
            isMetered = false,
        )
        verifyNetworkStatus(network, expectedMetered = true)
    }

    @Test
    fun shouldReturnConnectedStatusWhenVersionIsNewerAndHasActiveNetwork() = runTest {
        versionCodeProvider = versionCodeProvider(Build.VERSION_CODES.M)
        provider = AndroidConnectivityProvider(context, versionCodeProvider)

        val network = mockk<Network>()
        val capabilities = mockk<NetworkCapabilities> {
            every { hasTransport(NetworkCapabilities.TRANSPORT_WIFI) } returns true
            every { hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) } returns false
        }
        every { connectivityManager.activeNetwork } returns network
        every { connectivityManager.getNetworkCapabilities(network) } returns capabilities
        every { connectivityManager.isActiveNetworkMetered } returns false

        provider.monitor().test {
            triggerNetworkCallback()
            val status = awaitItem()

            status.shouldBeInstanceOf<Connectivity.Status.Connected>()
            status.metered shouldBe false
        }
    }

    @Test
    fun shouldReturnDisconnectedStatusWhenVersionIsNewerButNoActiveNetwork() = runTest {
        versionCodeProvider = versionCodeProvider(Build.VERSION_CODES.M)
        provider = AndroidConnectivityProvider(context, versionCodeProvider)

        every { connectivityManager.activeNetwork } returns null

        provider.monitor().test {
            triggerNetworkCallback()
            val status = awaitItem()

            status.shouldBeInstanceOf<Connectivity.Status.Disconnected>()
        }
    }

    @Test
    fun shouldReturnDisconnectedStatusWhenVersionIsNewerButNoNetworkCapabilities() = runTest {
        versionCodeProvider = versionCodeProvider(Build.VERSION_CODES.M)
        provider = AndroidConnectivityProvider(context, versionCodeProvider)

        val network = mockk<Network>()
        every { connectivityManager.activeNetwork } returns network
        every { connectivityManager.getNetworkCapabilities(network) } returns null

        provider.monitor().test {
            triggerNetworkCallback()
            val status = awaitItem()

            status.shouldBeInstanceOf<Connectivity.Status.Disconnected>()
        }
    }

    @Test
    fun shouldReturnConnectedStatusWhenVersionIsOlderAndNetworkIsConnected() = runTest {
        versionCodeProvider = versionCodeProvider(Build.VERSION_CODES.LOLLIPOP)
        provider = AndroidConnectivityProvider(context, versionCodeProvider)

        val networkInfo = mockk<NetworkInfo> {
            every { isConnected } returns true
        }

        every { connectivityManager.activeNetworkInfo } returns networkInfo
        every { connectivityManager.isActiveNetworkMetered } returns true

        provider.monitor().test {
            triggerNetworkCallback()
            val status = awaitItem()

            status.shouldBeInstanceOf<Connectivity.Status.Connected>()
            status.metered shouldBe true
        }
    }

    @Test
    fun shouldReturnDisconnectedStatusWhenVersionIsOlderAndNetworkIsDisconnected() = runTest {
        versionCodeProvider = versionCodeProvider(Build.VERSION_CODES.LOLLIPOP)
        provider = AndroidConnectivityProvider(context, versionCodeProvider)

        val networkInfo = mockk<NetworkInfo> {
            every { isConnected } returns false
        }

        every { connectivityManager.activeNetworkInfo } returns networkInfo

        provider.monitor().test {
            triggerNetworkCallback()
            val status = awaitItem()

            status.shouldBeInstanceOf<Connectivity.Status.Disconnected>()
        }
    }

    @Test
    fun shouldReturnDisconnectedStatusWhenVersionIsOlderAndNetworkInfoIsNull() = runTest {
        versionCodeProvider = versionCodeProvider(Build.VERSION_CODES.LOLLIPOP)
        provider = AndroidConnectivityProvider(context, versionCodeProvider)
        every { connectivityManager.activeNetworkInfo } returns null

        provider.monitor().test {
            triggerNetworkCallback()
            val status = awaitItem()

            status.shouldBeInstanceOf<Connectivity.Status.Disconnected>()
        }
    }

    private fun triggerNetworkCallback() {
        if (versionCodeProvider.code >= Build.VERSION_CODES.N) {
            verify { connectivityManager.registerDefaultNetworkCallback(capture(callbackSlot)) }
        } else {
            verify { connectivityManager.registerNetworkCallback(any(), capture(callbackSlot)) }
        }
        networkCallback = callbackSlot.captured
    }

    private fun setupNetworkCapabilities(
        isWifi: Boolean = false,
        isCellular: Boolean = false,
        isMetered: Boolean = false,
    ): Pair<Network, NetworkCapabilities> {
        val network = mockk<Network>()
        val capabilities = mockk<NetworkCapabilities> {
            every { hasTransport(NetworkCapabilities.TRANSPORT_WIFI) } returns isWifi
            every { hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) } returns isCellular
        }
        every { connectivityManager.getNetworkCapabilities(network) } returns capabilities
        every { connectivityManager.isActiveNetworkMetered } returns isMetered
        return network to capabilities
    }

    private suspend fun verifyNetworkStatus(
        network: Network,
        expectedMetered: Boolean,
    ) {
        provider.monitor().test {
            triggerNetworkCallback()
            skipItems(1) // Initial status

            networkCallback.onAvailable(network)
            val status = awaitItem()
            status.shouldBeInstanceOf<Connectivity.Status.Connected>()
            status.metered shouldBe expectedMetered
        }
    }
}

internal fun versionCodeProvider(code: Int): VersionCodeProvider = object : VersionCodeProvider {
    override val code: Int = code
}