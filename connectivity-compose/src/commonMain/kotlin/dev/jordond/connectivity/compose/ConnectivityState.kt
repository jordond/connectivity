package dev.jordond.connectivity.compose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import dev.jordond.connectivity.Connectivity
import dev.jordond.connectivity.Connectivity.Status.Connected
import dev.jordond.connectivity.Connectivity.Status.Disconnected
import dev.jordond.connectivity.ConnectivityOptions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch


/**
 * Create and remember a [ConnectivityState] instance.
 *
 * @param options The [ConnectivityOptions] to use for configuring the network status monitoring.
 * @param scope The [CoroutineScope] in which to launch the network status monitoring coroutine.
 * @param factory The lambda to use for creating the [Connectivity] instance.
 * @return A [ConnectivityState] instance.
 */
@Composable
public fun rememberConnectivityState(
    options: ConnectivityOptions = ConnectivityOptions(),
    scope: CoroutineScope = rememberCoroutineScope(),
    factory: () -> Connectivity,
): ConnectivityState {
    return remember(options, scope, factory) {
        ConnectivityState(options, scope, factory)
    }
}

/**
 * Represents the state of network connectivity.
 *
 * This class provides properties and functions for monitoring the network status and checking
 * whether the device is connected to the network or a metered network.
 *
 * @property options The [ConnectivityOptions] to use for configuring the network status monitoring.
 * @property scope The [CoroutineScope] in which to launch the network status monitoring coroutine.
 * @param factory The lambda to use for creating the [Connectivity] instance.
 */
@Stable
public open class ConnectivityState(
    private val options: ConnectivityOptions = ConnectivityOptions(),
    private val scope: CoroutineScope,
    factory: () -> Connectivity,
) {

    private val connectivity: Connectivity = factory()

    private var isMonitoring: Boolean by mutableStateOf(options.autoStart)

    /**
     * The current network status.
     *
     * This value will be `null` if the network status has not been checked yet.
     * Call [startMonitoring] to start monitoring the status, or [forceCheck] to force a check.
     */
    public var status: Connectivity.Status? by mutableStateOf(null)
        private set

    /**
     * A Boolean indicating whether the device is connected to the network.
     */
    public val isConnected: Boolean
        get() = status is Connected

    /**
     * A Boolean indicating whether the device is connected to a metered network.
     */
    public val isMetered: Boolean
        get() = status?.let { it is Connected && it.isMetered } ?: false

    /**
     * A Boolean indicating whether the device is disconnected from the network.
     */
    public val isDisconnected: Boolean
        get() = status is Disconnected

    init {
        scope.launch {
            connectivity.isActive.collect { isMonitoring = it }
        }

        scope.launch {
            connectivity.statusUpdates.collect { status = it }
        }
    }

    /**
     * Force a check of the current network status.
     */
    public fun forceCheck() {
        scope.launch { connectivity.status() }
    }

    /**
     * Start monitoring the network status.
     */
    public fun startMonitoring() {
        connectivity.start()
    }

    /**
     * Stop monitoring the network status.
     */
    public fun stopMonitoring() {
        connectivity.stop()
    }
}