package dev.jordond.connectivity

import dev.drewhamilton.poko.Poko
import dev.jordond.connectivity.Connectivity.Status.Connected
import dev.jordond.connectivity.Connectivity.Status.Disconnected
import dev.jordond.connectivity.internal.DefaultConnectivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * The Connectivity interface provides a way to monitor the network connectivity status.
 *
 * @property statusUpdates A [SharedFlow] representing the current connectivity status.
 * @property monitoring A [StateFlow] representing whether the connectivity monitoring is active.
 * @property updates A [StateFlow] representing the current connectivity status and whether
 * the monitoring is active.
 */
public interface Connectivity {

    public val statusUpdates: SharedFlow<Status>

    public val monitoring: StateFlow<Boolean>

    public val isMonitoring: Boolean
        get() = monitoring.value

    /**
     * Gets the current connectivity status.
     *
     * @return The current [Status] of the connectivity.
     */
    public suspend fun status(): Status

    /**
     * Starts monitoring the connectivity status.
     */
    public fun start()

    /**
     * Stops monitoring the connectivity status.
     */
    public fun stop()

    /**
     * Represents an update to the connectivity status.
     *
     * @property isMonitoring A Boolean indicating whether the connectivity monitoring is active.
     * @property status The [Status] of the connectivity.
     * @constructor Creates an update to the connectivity status.
     */
    @Deprecated(
        message = "This current usage of this class does not provide any additional value. Will be removed in a future release.",
        level = DeprecationLevel.WARNING,
    )
    @Poko
    public class Update(
        public val isMonitoring: Boolean,
        public val status: Status,
    ) {

        /**
         * A Boolean indicating whether the connectivity monitoring is active.
         */
        @Deprecated(
            message = "Use isMonitoring instead. Will be removed in a future release.",
            replaceWith = ReplaceWith("isMonitoring"),
            level = DeprecationLevel.WARNING,
        )
        public val isActive: Boolean = isMonitoring

        /**
         * A Boolean indicating whether the device is connected to the network.
         */
        public val isConnected: Boolean
            get() = status is Connected

        /**
         * A Boolean indicating whether the device is connected to a metered network.
         */
        public val isMetered: Boolean
            get() = status is Connected && status.metered

        /**
         * A Boolean indicating whether the device is disconnected from the network.
         */
        public val isDisconnected: Boolean
            get() = status is Disconnected

        @InternalConnectivityApi
        public companion object {

            @InternalConnectivityApi
            public val default: Update = Update(isMonitoring = false, Disconnected)
        }
    }

    /**
     * Represents the connectivity status.
     */
    public sealed interface Status {

        /**
         * A Boolean indicating whether the device is connected to the network.
         */
        public val isConnected: Boolean
            get() = this is Connected

        /**
         * A Boolean indicating whether the device is connected to a metered network.
         */
        public val isMetered: Boolean
            get() = this is Connected && metered

        /**
         * A Boolean indicating whether the device is disconnected from the network.
         */
        public val isDisconnected: Boolean
            get() = this is Disconnected

        /**
         * Represents a connected status.
         *
         * @property metered A Boolean indicating whether the connection is metered.
         */
        public data class Connected(public val metered: Boolean) : Status

        /**
         * Represents a disconnected status.
         */
        public data object Disconnected : Status
    }
}

/**
 * Creates a Connectivity instance.
 *
 * @param provider The [ConnectivityProvider] to use for monitoring connectivity.
 * @param options The [ConnectivityOptions] to use for configuring the connectivity monitoring.
 * @param scope The [CoroutineScope] in which to launch the connectivity monitoring coroutine.
 * @return A [Connectivity] instance.
 */
public fun Connectivity(
    provider: ConnectivityProvider,
    options: ConnectivityOptions = ConnectivityOptions(),
    scope: CoroutineScope = CoroutineScope(Dispatchers.Default),
): Connectivity {
    return DefaultConnectivity(scope, provider, options)
}

/**
 * Creates a [Connectivity] instance with a builder for the [ConnectivityOptions].
 *
 * @param provider The [ConnectivityProvider] to use for monitoring connectivity.
 * @param scope The [CoroutineScope] in which to launch the connectivity monitoring coroutine.
 * @param options A builder function for creating the [ConnectivityOptions].
 * @return A [Connectivity] instance.
 */
public fun Connectivity(
    provider: ConnectivityProvider,
    scope: CoroutineScope = CoroutineScope(Dispatchers.Default),
    options: ConnectivityOptions.Builder.() -> Unit,
): Connectivity {
    return Connectivity(provider, ConnectivityOptions.build(options), scope)
}