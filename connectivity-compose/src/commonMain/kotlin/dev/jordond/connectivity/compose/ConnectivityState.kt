package dev.jordond.connectivity.compose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import dev.drewhamilton.poko.Poko
import dev.jordond.connectivity.Connectivity
import dev.jordond.connectivity.Connectivity.Status.Connected
import dev.jordond.connectivity.Connectivity.Status.Disconnected
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch


/**
 * Create and remember a [ConnectivityState] instance.
 *
 * @param connectivity The [Connectivity] instance to use for monitoring the network status.
 * @param scope The [CoroutineScope] in which to launch the network status monitoring coroutine.
 * @return A [ConnectivityState] instance.
 */
@Composable
public fun rememberConnectivityState(
    connectivity: Connectivity,
    scope: CoroutineScope = rememberCoroutineScope(),
): ConnectivityState {
    return remember(scope, connectivity) {
        ConnectivityState(connectivity, scope)
    }
}

/**
 * Represents the state of network connectivity.
 *
 * This class provides properties and functions for monitoring the network status and checking
 * whether the device is connected to the network or a metered network.
 *
 * @property connectivity The [Connectivity] instance to use for monitoring the network status.
 * @property scope The [CoroutineScope] in which to launch the network status monitoring coroutine.
 */
@Poko
@Stable
public class ConnectivityState(
    private val connectivity: Connectivity,
    private val scope: CoroutineScope,
) {

    public var isMonitoring: Boolean by mutableStateOf(connectivity.isMonitoring.value)
        private set

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
            connectivity.isMonitoring.collect { isMonitoring = it }
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