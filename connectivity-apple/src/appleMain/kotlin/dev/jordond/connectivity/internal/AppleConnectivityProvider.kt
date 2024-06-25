package dev.jordond.connectivity.internal

import dev.jordond.connectivity.Connectivity
import dev.jordond.connectivity.ConnectivityProvider
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import platform.Network.nw_interface_type_wifi
import platform.Network.nw_path_get_status
import platform.Network.nw_path_is_constrained
import platform.Network.nw_path_is_expensive
import platform.Network.nw_path_monitor_cancel
import platform.Network.nw_path_monitor_create
import platform.Network.nw_path_monitor_set_queue
import platform.Network.nw_path_monitor_set_update_handler
import platform.Network.nw_path_monitor_start
import platform.Network.nw_path_status_satisfied
import platform.Network.nw_path_uses_interface_type
import platform.darwin.DISPATCH_QUEUE_SERIAL_WITH_AUTORELEASE_POOL
import platform.darwin.dispatch_queue_create

internal object AppleConnectivityProvider : ConnectivityProvider {

    override fun monitor(): Flow<Connectivity.Status> {
        val monitor = nw_path_monitor_create()
        val queue = dispatch_queue_create(
            label = "dev.jordond.connectivity.monitor",
            attr = DISPATCH_QUEUE_SERIAL_WITH_AUTORELEASE_POOL,
        )

        return callbackFlow {
            nw_path_monitor_set_update_handler(monitor) { path ->
                val status = nw_path_get_status(path)
                when {
                    status == nw_path_status_satisfied -> {
                        val isWifi = nw_path_uses_interface_type(path, nw_interface_type_wifi)
                        val isExpensive = nw_path_is_expensive(path)
                        val isConstrained = nw_path_is_constrained(path)
                        val isMetered = !isWifi && (isExpensive || isConstrained)

                        trySend(Connectivity.Status.Connected(isMetered))
                    }
                    else -> trySend(Connectivity.Status.Disconnected)
                }
            }

            nw_path_monitor_set_queue(monitor, queue)
            nw_path_monitor_start(monitor)

            awaitClose {
                nw_path_monitor_cancel(monitor)
            }
        }
    }
}